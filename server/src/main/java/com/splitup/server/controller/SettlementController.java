package com.splitup.server.controller;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.SettlementRecord;
import com.splitup.model.User;
import com.splitup.server.dto.ConfirmedSettlementDto;
import com.splitup.service.BalanceService;
import com.splitup.service.BusinessException;
import com.splitup.service.GroupService;
import com.splitup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
@CrossOrigin(origins = "*")
public class SettlementController {

    private final BalanceService balanceService = new BalanceService();
    private final GroupService   groupService   = new GroupService();
    private final UserService    userService    = new UserService();

    /**
     * Lista los pagos confirmados del grupo.
     * Solo accesible para miembros del grupo.
     */
    @GetMapping("/confirmed")
    public ResponseEntity<?> getConfirmed(@PathVariable("groupId") Long groupId,
                                          @RequestParam("userId") Long userId) {
        ExpenseGroup group = resolveGroup(groupId, userId);
        if (group == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<ConfirmedSettlementDto> dtos = balanceService.getConfirmedSettlements(group)
                .stream().map(ConfirmedSettlementDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Confirma que un miembro ha realizado un pago.
     * Solo ADMIN u OWNER pueden llamar a este endpoint.
     * Devuelve 201 con el DTO del registro creado.
     */
    @PostMapping
    public ResponseEntity<?> confirmSettlement(@PathVariable("groupId") Long groupId,
                                               @RequestBody Map<String, Object> body) {
        Long requesterId = ((Number) body.get("requesterId")).longValue();
        Long fromUserId  = ((Number) body.get("fromUserId")).longValue();
        Long toUserId    = ((Number) body.get("toUserId")).longValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        ExpenseGroup group = resolveGroup(groupId, requesterId);
        if (group == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User requester = userService.findById(requesterId).orElse(null);
        User from      = userService.findById(fromUserId).orElse(null);
        User to        = userService.findById(toUserId).orElse(null);
        if (from == null || to == null)
            return ResponseEntity.badRequest().body("Usuario no encontrado");

        try {
            SettlementRecord record =
                    balanceService.confirmSettlement(group, from, to, amount, requester);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ConfirmedSettlementDto.from(record));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Deshace un pago confirmado (borrado físico).
     * Solo ADMIN u OWNER pueden llamar a este endpoint.
     * Devuelve 204 en éxito, 403 si el requester no es admin,
     * 404 si el registro no existe.
     */
    @DeleteMapping("/{settlementId}")
    public ResponseEntity<?> unconfirmSettlement(@PathVariable("groupId") Long groupId,
                                                 @PathVariable("settlementId") Long settlementId,
                                                 @RequestParam("requesterId") Long requesterId) {
        ExpenseGroup group = resolveGroup(groupId, requesterId);
        if (group == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        User requester = userService.findById(requesterId).orElse(null);
        if (requester == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            balanceService.unconfirmSettlement(settlementId, requester);
            return ResponseEntity.noContent().build();
        } catch (BusinessException ex) {
            // "Liquidación no encontrada" → 404; resto de BusinessException → 403
            if (ex.getMessage().contains("no encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
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
