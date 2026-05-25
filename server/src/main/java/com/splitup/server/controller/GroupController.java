package com.splitup.server.controller;

import com.splitup.model.ExpenseGroup;
import com.splitup.model.User;
import com.splitup.model.enums.GroupRole;
import com.splitup.server.dto.GroupDto;
import com.splitup.server.dto.MemberDto;
import com.splitup.service.BusinessException;
import com.splitup.service.GroupService;
import com.splitup.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService = new GroupService();
    private final UserService  userService  = new UserService();

    /** Lista los grupos en los que un usuario es miembro. */
    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(@RequestParam("userId") Long userId) {
        return userService.findById(userId)
                .map(u -> ResponseEntity.ok(
                        groupService.getGroupsByMember(u).stream()
                                .map(GroupDto::from).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Crea un nuevo grupo con el usuario indicado como OWNER. */
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> body) {
        String name        = (String) body.get("name");
        String description = (String) body.get("description");
        Long   creatorId   = ((Number) body.get("creatorId")).longValue();

        User creator = userService.findById(creatorId).orElse(null);
        if (creator == null) return ResponseEntity.badRequest().body("Creador no encontrado");

        try {
            ExpenseGroup group = groupService.createGroup(name, description, creator);
            return ResponseEntity.status(HttpStatus.CREATED).body(GroupDto.from(group));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /** Lista los miembros de un grupo. */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<?> getMembers(@PathVariable("groupId") Long groupId,
                                        @RequestParam("requesterId") Long requesterId) {
        // Usamos el groupId para buscar el grupo via members del requester
        User requester = userService.findById(requesterId).orElse(null);
        if (requester == null) return ResponseEntity.badRequest().body("Usuario no encontrado");

        List<ExpenseGroup> groups = groupService.getGroupsByMember(requester).stream()
                .filter(g -> g.getId().equals(groupId)).toList();
        if (groups.isEmpty()) return ResponseEntity.notFound().build();

        ExpenseGroup group = groups.get(0);
        List<MemberDto> members = groupService.getMembersOfGroup(group).stream()
                .map(MemberDto::from).toList();
        return ResponseEntity.ok(members);
    }

    /** Añade un miembro al grupo por email o por userId. */
    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<?> addMember(@PathVariable("groupId") Long groupId,
                                       @RequestBody Map<String, Object> body) {
        Long requesterId = ((Number) body.get("requesterId")).longValue();

        User requester = userService.findById(requesterId).orElse(null);
        if (requester == null) return ResponseEntity.badRequest().body("Solicitante no encontrado");

        User target;
        if (body.containsKey("email")) {
            target = userService.findByEmail((String) body.get("email")).orElse(null);
        } else if (body.containsKey("userId")) {
            target = userService.findById(((Number) body.get("userId")).longValue()).orElse(null);
        } else {
            return ResponseEntity.badRequest().body("Se requiere 'email' o 'userId'");
        }
        if (target == null)
            return ResponseEntity.badRequest().body("Usuario no encontrado");

        List<ExpenseGroup> groups = groupService.getGroupsByMember(requester).stream()
                .filter(g -> g.getId().equals(groupId)).toList();
        if (groups.isEmpty()) return ResponseEntity.notFound().build();

        try {
            groupService.addMember(groups.get(0), target, GroupRole.MEMBER, requester);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Elimina un grupo y todo su contenido.
     * Solo el OWNER puede hacerlo.
     */
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable("groupId") Long groupId,
                                         @RequestParam("requesterId") Long requesterId) {
        User requester = userService.findById(requesterId).orElse(null);
        if (requester == null) return ResponseEntity.badRequest().body("Usuario no encontrado");

        List<ExpenseGroup> groups = groupService.getGroupsByMember(requester).stream()
                .filter(g -> g.getId().equals(groupId)).toList();
        if (groups.isEmpty()) return ResponseEntity.notFound().build();

        try {
            groupService.deleteGroup(groups.get(0), requester);
            return ResponseEntity.noContent().build();
        } catch (BusinessException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    /**
     * Une a un usuario a un grupo mediante su código de invitación.
     * Body: { inviteCode, userId }
     */
    @PostMapping("/groups/join")
    public ResponseEntity<?> joinByCode(@RequestBody Map<String, Object> body) {
        String inviteCode = (String) body.get("inviteCode");
        Long   userId     = ((Number) body.get("userId")).longValue();

        User user = userService.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("Usuario no encontrado");

        try {
            groupService.joinByInviteCode(inviteCode, user);
            // Devolver el grupo al que se unió para que el cliente lo muestre
            ExpenseGroup group = groupService.getGroupsByMember(user).stream()
                    .filter(g -> inviteCode.equals(g.getInviteCode()))
                    .findFirst().orElse(null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(group != null ? GroupDto.from(group) : null);
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
