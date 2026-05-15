package com.splitup.service;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.User;
import com.splitup.repository.impl.ExpenseDao;
import com.splitup.repository.impl.ExpenseShareDao;
import com.splitup.repository.impl.GroupMemberDao;
import com.splitup.service.dto.Settlement;
import com.splitup.service.dto.UserBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Servicio de dominio para el cálculo de balances y liquidaciones de deudas.
 *
 * ── Cálculo de balance ──────────────────────────────────────────────────────
 * Para cada miembro se calcula su balance neto dentro del grupo:
 *
 *   balance = Σ(gastos que él pagó) − Σ(shares asignados a él)
 *
 *   balance > 0  →  acreedor: el grupo le debe dinero.
 *   balance < 0  →  deudor:   él debe dinero al grupo.
 *   balance = 0  →  saldado.
 *
 * ── Algoritmo de minimización de transferencias ─────────────────────────────
 * El problema naive sería que cada deudor pagase directamente a cada acreedor
 * la parte correspondiente de cada gasto → O(gastos × miembros) transferencias.
 *
 * La clave es que solo importa el BALANCE NETO de cada persona, no el origen
 * de cada transacción. Con eso, el problema se reduce a:
 *   "dadas n personas con saldos que suman cero, ¿cómo liquidarlos con el
 *    menor número de transferencias posible?"
 *
 * Algoritmo voraz (greedy):
 *   1. Separar en acreedores (balance > 0) y deudores (balance < 0).
 *   2. Repetir mientras haya deudas pendientes:
 *      a. Tomar el mayor acreedor (max heap) y el mayor deudor (min heap).
 *      b. amount = min(acreedor.balance, |deudor.balance|).
 *      c. Registrar transferencia: deudor → acreedor, amount.
 *      d. Actualizar balances y eliminar los que llegan a cero.
 *   3. Resultado: lista de transferencias, a lo sumo n−1 para n miembros.
 *
 * Complejidad: O(n log n) gracias a las colas de prioridad.
 *
 * Nota: el algoritmo voraz no siempre produce el mínimo absoluto teórico
 * (problema NP-difícil en el caso general), pero para los tamaños reales
 * de grupos (< 30 personas) produce resultados óptimos o muy cercanos, y
 * es la estrategia que usan Splitwise y Tricount.
 */
public class BalanceService {

    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);

    private final ExpenseDao expenseDao;
    private final ExpenseShareDao shareDao;
    private final GroupMemberDao memberDao;

    public BalanceService() {
        this.expenseDao = new ExpenseDao();
        this.shareDao   = new ExpenseShareDao();
        this.memberDao  = new GroupMemberDao();
    }

    /** Constructor para tests: permite inyectar DAOs mockeados. */
    public BalanceService(ExpenseDao expenseDao, ExpenseShareDao shareDao, GroupMemberDao memberDao) {
        this.expenseDao = expenseDao;
        this.shareDao   = shareDao;
        this.memberDao  = memberDao;
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Calcula el balance neto de cada miembro del grupo.
     *
     * @param group Grupo sobre el que calcular
     * @return Lista de balances, uno por miembro (orden no garantizado)
     */
    public List<UserBalance> getGroupBalances(ExpenseGroup group) {
        // JOIN FETCH para acceder a user.getName() sin sesión abierta
        List<GroupMember> members = memberDao.findByGroupFetchingUsers(group);
        List<UserBalance> balances = new ArrayList<>(members.size());

        for (GroupMember gm : members) {
            User user = gm.getUser();

            // Total adelantado por este usuario como pagador de gastos del grupo
            BigDecimal paid = expenseDao.findByPayerAndGroup(user, group).stream()
                    .map(e -> e.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total que le corresponde pagar según sus shares en el grupo
            BigDecimal owed = shareDao.findByUserAndGroup(user, group.getId()).stream()
                    .map(s -> s.getAmountAssigned() != null ? s.getAmountAssigned() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            balances.add(new UserBalance(user, paid.subtract(owed)));
        }

        log.debug("Balances calculados para groupId={}: {} miembros", group.getId(), balances.size());
        return balances;
    }

    /**
     * Calcula el balance neto de un único usuario en el grupo.
     *
     * @param user  Usuario
     * @param group Grupo
     * @return Importe: positivo si el grupo le debe, negativo si él debe
     */
    public BigDecimal getUserBalance(User user, ExpenseGroup group) {
        BigDecimal paid = expenseDao.findByPayerAndGroup(user, group).stream()
                .map(e -> e.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal owed = shareDao.findByUserAndGroup(user, group.getId()).stream()
                .map(s -> s.getAmountAssigned() != null ? s.getAmountAssigned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return paid.subtract(owed);
    }

    /**
     * Calcula la lista mínima de transferencias para saldar todas las deudas del grupo.
     *
     * Ver la documentación de clase para la explicación completa del algoritmo.
     *
     * @param group Grupo sobre el que calcular
     * @return Lista de transferencias sugeridas, ordenadas de mayor a menor importe
     */
    public List<Settlement> getSettlements(ExpenseGroup group) {
        List<UserBalance> balances = getGroupBalances(group);

        // Max-heap: el acreedor con más saldo positivo va primero
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>(
                Comparator.comparing(UserBalance::balance).reversed()
        );

        // Min-heap: el deudor con más deuda (balance más negativo) va primero
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(
                Comparator.comparing(UserBalance::balance)
        );

        for (UserBalance ub : balances) {
            int sign = ub.balance().compareTo(BigDecimal.ZERO);
            if      (sign > 0) creditors.add(ub);
            else if (sign < 0) debtors.add(ub);
            // balance == 0 → ya saldado, se ignora
        }

        List<Settlement> settlements = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            UserBalance creditor = creditors.poll();
            UserBalance debtor   = debtors.poll();

            // El deudor debe |debtor.balance| y el acreedor espera creditor.balance.
            // La transferencia es el menor de los dos: así al menos uno queda a cero.
            BigDecimal amount = creditor.balance().min(debtor.balance().negate());

            settlements.add(new Settlement(debtor.user(), creditor.user(), amount));

            BigDecimal newCreditorBalance = creditor.balance().subtract(amount);
            BigDecimal newDebtorBalance   = debtor.balance().add(amount);

            // Reinsertar solo si el saldo residual es distinto de cero
            if (newCreditorBalance.compareTo(BigDecimal.ZERO) > 0)
                creditors.add(new UserBalance(creditor.user(), newCreditorBalance));

            if (newDebtorBalance.compareTo(BigDecimal.ZERO) < 0)
                debtors.add(new UserBalance(debtor.user(), newDebtorBalance));
        }

        // Ordenar de mayor a menor importe para facilitar la presentación
        settlements.sort(Comparator.comparing(Settlement::amount).reversed());

        log.debug("Liquidaciones para groupId={}: {} transferencias (máximo {})",
                group.getId(), settlements.size(),
                balances.isEmpty() ? 0 : balances.size() - 1);

        return settlements;
    }
}
