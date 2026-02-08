package com.splitup.model;

import com.splitup.model.enums.ShareType;
import com.splitup.model.ids.ExpenseShareId;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @EmbeddedId
    private ExpenseShareId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("expenseId")
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false)
    private ShareType shareType = ShareType.EQUAL;

    @Column(name = "amount_assigned", precision = 12, scale = 2)
    private BigDecimal amountAssigned;

    public ExpenseShare() {
    }

    public ExpenseShare(Expense expense, User user, ShareType shareType, BigDecimal amountAssigned) {
        this.expense = expense;
        this.user = user;
        this.shareType = (shareType != null ? shareType : ShareType.EQUAL);
        this.amountAssigned = amountAssigned;
        this.id = new ExpenseShareId(expense.getId(), user.getId());
    }

    public ExpenseShareId getId() {
        return id;
    }

    public Expense getExpense() {
        return expense;
    }

    public User getUser() {
        return user;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public BigDecimal getAmountAssigned() {
        return amountAssigned;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    public void setAmountAssigned(BigDecimal amountAssigned) {
        this.amountAssigned = amountAssigned;
    }
}
