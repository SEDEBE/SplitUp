package com.splitup.model;

import jakarta.persistence.*;
import com.splitup.model.enums.SplitMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")

public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency = "EUR";

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_mode", nullable = false)
    private SplitMode splitMode = SplitMode.EQUAL;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Expense() {
    }

    public Expense(ExpenseGroup group, User payer, String title, BigDecimal totalAmount, LocalDate expenseDate) {
        this.group = group;
        this.payer = payer;
        this.title = title;
        this.totalAmount = totalAmount;
        this.expenseDate = expenseDate;
    }

    public Long getId() {
        return id;
    }

    public ExpenseGroup getGroup() {
        return group;
    }

    public User getPayer() {
        return payer;
    }

    public Category getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getNote() {
        return note;
    }

    public SplitMode getSplitMode() {
        return splitMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setSplitMode(SplitMode splitMode) {
        this.splitMode = splitMode;
    }

}
