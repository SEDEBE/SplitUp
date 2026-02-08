package com.splitup.model;

import com.splitup.model.enums.GroupRole;
import com.splitup.model.ids.GroupMemberId;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")

public class GroupMember {
    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role = GroupRole.MEMBER;

    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime joinedAt;

    public GroupMember() {
    }

    public GroupMember(User user, ExpenseGroup group, GroupRole role) {
        this.user = user;
        this.group = group;
        this.role = role != null ? role : GroupRole.MEMBER;
        this.id = new GroupMemberId(group.getId(), user.getId());
    }

    public GroupMemberId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ExpenseGroup getGroup() {
        return group;
    }

    public GroupRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

}
