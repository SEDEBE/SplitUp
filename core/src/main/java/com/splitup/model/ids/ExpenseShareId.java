package com.splitup.model.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExpenseShareId implements Serializable {

    @Column(name = "expense_id", columnDefinition = "BIGINT UNSIGNED")
    private Long expenseId;

    @Column(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    private Long userId;

    public ExpenseShareId() {
    }

    public ExpenseShareId(Long expenseId, Long userId) {
        this.expenseId = expenseId;
        this.userId = userId;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExpenseShareId that))
            return false;
        return Objects.equals(expenseId, that.expenseId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expenseId, userId);
    }
}
