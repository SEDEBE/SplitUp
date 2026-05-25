package com.splitup.service;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.SettlementRecord;
import com.splitup.model.User;
import com.splitup.model.enums.GroupRole;
import com.splitup.repository.impl.ExpenseDao;
import com.splitup.repository.impl.ExpenseShareDao;
import com.splitup.repository.impl.GroupMemberDao;
import com.splitup.repository.impl.SettlementRecordDao;
import com.splitup.service.dto.Settlement;
import com.splitup.service.dto.UserBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Servicio de dominio para el cálculo de balances y liquidaciones de deudas.
 *
 * ── Cálculo de balance ──────────────────────────────────────────────────────
 * Para cada miembro se calcula su balance neto dentro del grupo:
 *
 *   balance = Σ(gastos que él pagó) − Σ(shares asignados a él)
 *           + Σ(liquidaciones donde él es fromUser)   ← pagó deuda
 *           − Σ(liquidaciones donde él es toUser)     ← cobró lo que se le debía
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

    private final ExpenseDao          expenseDao;
    private final ExpenseShareDao     shareDao;
    private final GroupMemberDao      memberDao;
    private final SettlementRecordDao settlementRecordDao;

    public BalanceService() {
        this.expenseDao          = new ExpenseDao();
        this.shareDao            = new ExpenseShareDao();
        this.memberDao           = new GroupMemberDao();
        this.settlementRecordDao = new SettlementRecordDao();
    }

    /** Constructor para tests: permite inyectar DAOs mockeados. */
    public BalanceService(ExpenseDao expenseDao, ExpenseShareDao shareDao,
                          GroupMemberDao memberDao, SettlementRecordDao settlementRecordDao) {
        this.expenseDao          = expenseDao;
        this.shareDao            = shareDao;
        this.memberDao           = memberDao;
        this.settlementRecordDao = settlementRecordDao;
    }

    // -----------------------------------------------------------------------
    // API pública — balances
    // -----------------------------------------------------------------------

    /**
     * Calcula el balance neto de cada miembro del grupo, incluyendo el efecto
     * de los pagos ya confirmados (SettlementRecord).
     *
     * @param group Grupo sobre el que calcular
     * @return Lista de balances, uno por miembro (orden no garantizado)
     */
    public List<UserBalance> getGroupBalances(ExpenseGroup group) {
        List<GroupMember> members = memberDao.findByGroupFetchingUsers(group);

        // Delta acumulado de liquidaciones confirmadas: userId → importe neto.
        // fromUser pagó → su delta sube (cubre su deuda).
        // toUser cobró → su delta baja (ya no se le debe tanto).
        Map<Long, BigDecimal> settlementDelta = new HashMap<>();
        for (SettlementRecord r : settlementRecordDao.findByGroupFetchingUsers(group)) {
            Long fromId = r.getFromUser().getId();
            Long toId   = r.getToUser().getId();
            settlementDelta.merge(fromId, r.getAmount(),          BigDecimal::add);
            settlementDelta.merge(toId,   r.getAmount().negate(), BigDecimal::add);
        }

        List<UserBalance> balances = new ArrayList<>(members.size());
        for (GroupMember gm : members) {
            User user = gm.getUser();

            BigDecimal paid = expenseDao.findByPayerAndGroup(user, group).stream()
                    .map(e -> e.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal owed = shareDao.findByUserAndGroup(user, group.getId()).stream()
                    .map(s -> s.getAmountAssigned() != null ? s.getAmountAssigned() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal delta = settlementDelta.getOrDefault(user.getId(), BigDecimal.ZERO);
            balances.add(new UserBalance(user, paid.subtract(owed).add(delta)));
        }

        log.debug("Balances calculados para groupId={}: {} miembros", group.getId(), balances.size());
        return balances;
    }

    /**
     * Calcula el balance neto de un único usuario en el grupo.
     * Delega en {@link #getGroupBalances} para no duplicar la lógica de deltas.
     *
     * @param user  Usuario
     * @param group Grupo
     * @return Importe: positivo si el grupo le debe, negativo si él debe
     */
    public BigDecimal getUserBalance(User user, ExpenseGroup group) {
        return getGroupBalances(group).stream()
                .filter(ub -> ub.user().getId().equals(user.getId()))
                .findFirst()
                .map(UserBalance::balance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Calcula la lista mínima de transferencias para saldar todas las deudas del grupo.
     *
     * Ver la documentación de clase para la explicación completa del algoritmo.
     * El input son los balances de {@link #getGroupBalances}, que ya incorporan
     * el efecto de los pagos confirmados.
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

            BigDecimal amount = creditor.balance().min(debtor.balance().negate());

            settlements.add(new Settlement(debtor.user(), creditor.user(), amount));

            BigDecimal newCreditorBalance = creditor.balance().subtract(amount);
            BigDecimal newDebtorBalance   = debtor.balance().add(amount);

            if (newCreditorBalance.compareTo(BigDecimal.ZERO) > 0)
                creditors.add(new UserBalance(creditor.user(), newCreditorBalance));

            if (newDebtorBalance.compareTo(BigDecimal.ZERO) < 0)
                debtors.add(new UserBalance(debtor.user(), newDebtorBalance));
        }

        settlements.sort(Comparator.comparing(Settlement::amount).reversed());

        log.debug("Liquidaciones para groupId={}: {} transferencias (máximo {})",
                group.getId(), settlements.size(),
                balances.isEmpty() ? 0 : balances.size() - 1);

        return settlements;
    }

    // -----------------------------------------------------------------------
    // API pública — confirmación de pagos
    // -----------------------------------------------------------------------

    /**
     * Confirma que {@code from} ha pagado {@code amount} euros a {@code to}.
     * Solo ADMIN u OWNER del grupo pueden llamar a este método.
     *
     * <p>Tras el persist se hace un reload con JOIN FETCH para que el record
     * devuelto tenga {@code settledAt} (generado por la BD) y las relaciones
     * de usuario ya inicializadas.
     *
     * @param group     Grupo donde se produce el pago
     * @param from      Usuario que paga
     * @param to        Usuario que recibe el pago
     * @param amount    Importe (debe ser &gt; 0)
     * @param requester Usuario que confirma (debe ser ADMIN u OWNER)
     * @return SettlementRecord persistido y completamente cargado
     */
    public SettlementRecord confirmSettlement(ExpenseGroup group, User from, User to,
                                              BigDecimal amount, User requester)
            throws BusinessException {
        validateAdmin(group, requester, "confirmar liquidación");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("El importe debe ser mayor que cero");
        if (from.getId().equals(to.getId()))
            throw new BusinessException("El pagador y el receptor no pueden ser el mismo usuario");
        if (!memberDao.isMember(from, group))
            throw new BusinessException("El pagador no es miembro del grupo");
        if (!memberDao.isMember(to, group))
            throw new BusinessException("El receptor no es miembro del grupo");

        SettlementRecord record = new SettlementRecord(group, from, to, amount, requester);
        settlementRecordDao.save(record);

        // Reload para obtener settledAt (DEFAULT CURRENT_TIMESTAMP de BD)
        // y las relaciones fromUser/toUser/group inicializadas.
        return settlementRecordDao.findByIdFetchingAll(record.getId())
                .orElseThrow(() -> new RuntimeException("Error al recuperar liquidación persistida"));
    }

    /**
     * Deshace un pago confirmado borrando físicamente el registro.
     * Solo ADMIN u OWNER del grupo al que pertenece la liquidación pueden
     * llamar a este método.
     *
     * @param settlementId PK de la liquidación a eliminar
     * @param requester    Usuario que solicita la operación
     */
    public void unconfirmSettlement(Long settlementId, User requester)
            throws BusinessException {
        SettlementRecord record = settlementRecordDao.findById(settlementId)
                .orElseThrow(() -> new BusinessException("Liquidación no encontrada"));
        // group es un proxy Hibernate; getId() es accesible sin inicializar la asociación.
        validateAdmin(record.getGroup(), requester, "deshacer liquidación");
        settlementRecordDao.delete(record);
        log.info("Liquidación eliminada: id={}, por userId={}", settlementId, requester.getId());
    }

    /**
     * Devuelve todos los pagos confirmados del grupo, ordenados de más
     * reciente a más antiguo.
     *
     * @param group Grupo
     * @return Lista de SettlementRecord con fromUser y toUser cargados
     */
    public List<SettlementRecord> getConfirmedSettlements(ExpenseGroup group) {
        return settlementRecordDao.findByGroupFetchingUsers(group);
    }

    // -----------------------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------------------

    /**
     * Comprueba que {@code user} sea ADMIN u OWNER del {@code group}.
     *
     * <p>NOTE: se permite OWNER además de ADMIN para ser consistente con
     * {@code GroupService.requireAdminOrOwner}. El OWNER siempre tiene al
     * menos los mismos privilegios que un ADMIN.
     *
     * @param group       Grupo (puede ser un proxy Hibernate detached)
     * @param user        Usuario a verificar
     * @param action      Descripción de la acción, para el mensaje de error
     */
    private void validateAdmin(ExpenseGroup group, User user, String action)
            throws BusinessException {
        GroupMember member = memberDao.findByUserAndGroup(user, group)
                .orElseThrow(() -> new BusinessException(
                        "El usuario no es miembro del grupo"));
        if (member.getRole() == GroupRole.MEMBER)
            throw new BusinessException(
                    "Se requiere rol ADMIN u OWNER para " + action);
    }
}
