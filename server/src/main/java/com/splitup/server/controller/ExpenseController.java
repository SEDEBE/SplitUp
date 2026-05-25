package com.splitup.server.controller;

import com.splitup.model.Expense;
import com.splitup.model.ExpenseGroup;
import com.splitup.model.GroupMember;
import com.splitup.model.User;
import com.splitup.model.enums.SplitMode;
import com.splitup.server.dto.ExpenseDto;
import com.splitup.service.BusinessException;
import com.splitup.service.ExpenseService;
import com.splitup.service.GroupService;
import com.splitup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService = new ExpenseService();
    private final GroupService   groupService   = new GroupService();
    private final UserService    userService    = new UserService();

    /** Lista todos los gastos de un grupo. */
    @GetMapping
    public ResponseEntity<?> getExpenses(@PathVariable("groupId") Long groupId,
                                         @RequestParam("userId") Long userId) {
        ExpenseGroup group = resolveGroup(groupId, userId);
        if (group == null) return ResponseEntity.notFound().build();

        List<ExpenseDto> dtos = expenseService.getExpensesByGroup(group).stream()
                .map(ExpenseDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Crea un gasto en el grupo.
     * Body: { payerId, title, amount, date (YYYY-MM-DD), splitMode, participantIds: [id,...] }
     */
    @PostMapping
    public ResponseEntity<?> createExpense(@PathVariable("groupId") Long groupId,
                                           @RequestBody Map<String, Object> body) {
        Long requesterId = ((Number) body.get("requesterId")).longValue();
        Long payerId     = ((Number) body.get("payerId")).longValue();

        User requester = userService.findById(requesterId).orElse(null);
        if (requester == null) return ResponseEntity.badRequest().body("Usuario no encontrado");

        ExpenseGroup group = resolveGroup(groupId, requesterId);
        if (group == null) return ResponseEntity.notFound().build();

        User payer = userService.findById(payerId).orElse(null);
        if (payer == null) return ResponseEntity.badRequest().body("Pagador no encontrado");

        String title  = (String) body.get("title");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        LocalDate date = LocalDate.parse(body.get("date").toString());

        SplitMode mode = SplitMode.EQUAL;
        if (body.containsKey("splitMode"))
            mode = SplitMode.valueOf(body.get("splitMode").toString().toUpperCase());

        List<User> participants;
        Map<User, BigDecimal> customAmounts = null;

        if (mode == SplitMode.CUSTOM && body.containsKey("shares")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> sharesList = (List<Map<String, Object>>) body.get("shares");
            customAmounts = new LinkedHashMap<>();
            participants  = new ArrayList<>();
            for (Map<String, Object> share : sharesList) {
                Long       uid = ((Number) share.get("userId")).longValue();
                BigDecimal amt = new BigDecimal(share.get("amount").toString());
                User       u   = userService.findById(uid).orElse(null);
                if (u != null) { participants.add(u); customAmounts.put(u, amt); }
            }
            if (participants.isEmpty())
                return ResponseEntity.badRequest().body("No se encontraron participantes válidos");
        } else {
            @SuppressWarnings("unchecked")
            List<Number> participantIds = (List<Number>) body.getOrDefault("participantIds", List.of());
            participants = participantIds.stream()
                    .map(id -> userService.findById(id.longValue()).orElse(null))
                    .filter(u -> u != null)
                    .toList();
            if (participants.isEmpty()) {
                participants = groupService.getMembersOfGroup(group).stream()
                        .map(GroupMember::getUser).toList();
            }
        }

        try {
            Expense expense = expenseService.createExpense(
                    group, payer, title, amount, date,
                    null, null, null, mode, participants, customAmounts);
            return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseDto.from(expense));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /** Elimina un gasto. */
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable("groupId") Long groupId,
                                           @PathVariable("expenseId") Long expenseId,
                                           @RequestParam("userId") Long userId) {
        ExpenseGroup group = resolveGroup(groupId, userId);
        if (group == null) return ResponseEntity.notFound().build();

        Optional<Expense> expenseOpt = expenseService.getExpensesByGroup(group).stream()
                .filter(e -> e.getId().equals(expenseId))
                .findFirst();
        if (expenseOpt.isEmpty()) return ResponseEntity.notFound().build();

        expenseService.deleteExpense(expenseOpt.get());
        return ResponseEntity.noContent().build();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private ExpenseGroup resolveGroup(Long groupId, Long userId) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return null;
        return groupService.getGroupsByMember(user).stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst().orElse(null);
    }
}
