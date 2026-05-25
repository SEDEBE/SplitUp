package com.splitup.server.controller;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.User;
import com.splitup.server.dto.BalanceDto;
import com.splitup.server.dto.SettlementDto;
import com.splitup.service.BalanceService;
import com.splitup.service.GroupService;
import com.splitup.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@CrossOrigin(origins = "*")
public class BalanceController {

    private final BalanceService balanceService = new BalanceService();
    private final GroupService   groupService   = new GroupService();
    private final UserService    userService    = new UserService();

    /** Devuelve el balance neto de cada miembro del grupo. */
    @GetMapping("/balances")
    public ResponseEntity<?> getBalances(@PathVariable("groupId") Long groupId,
                                         @RequestParam("userId") Long userId) {
        ExpenseGroup group = resolveGroup(groupId, userId);
        if (group == null) return ResponseEntity.notFound().build();

        List<BalanceDto> dtos = balanceService.getGroupBalances(group).stream()
                .map(BalanceDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Devuelve las transferencias mínimas para saldar todas las deudas.
     * Este endpoint expone el algoritmo de minimización de BalanceService.
     */
    @GetMapping("/settlements")
    public ResponseEntity<?> getSettlements(@PathVariable("groupId") Long groupId,
                                            @RequestParam("userId") Long userId) {
        ExpenseGroup group = resolveGroup(groupId, userId);
        if (group == null) return ResponseEntity.notFound().build();

        List<SettlementDto> dtos = balanceService.getSettlements(group).stream()
                .map(SettlementDto::from).toList();
        return ResponseEntity.ok(dtos);
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
