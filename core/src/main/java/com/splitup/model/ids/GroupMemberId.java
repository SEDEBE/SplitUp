package com.splitup.model.ids;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable

public class GroupMemberId implements Serializable {
    @Column(name = "group_id", columnDefinition = "BIGINT UNSIGNED")
    private Long groupId;
    @Column(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    private Long userId;

    public GroupMemberId() {
    }

    public GroupMemberId(Long groupId, Long userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupMemberId that))
            return false;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}
