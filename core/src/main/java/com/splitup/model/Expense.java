package com.splitup.model;

import com.splitup.model.enums.SplitMode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // En BD: CHAR(3) NOT NULL DEFAULT 'EUR'
    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
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
        setGroup(group);
        setPayer(payer);
        setTitle(title);
        setTotalAmount(totalAmount);
        setExpenseDate(expenseDate);
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

    public void setGroup(ExpenseGroup group) {
        if (group == null)
            throw new IllegalArgumentException("group no puede ser null");
        this.group = group;
    }

    public void setPayer(User payer) {
        if (payer == null)
            throw new IllegalArgumentException("payer no puede ser null");
        this.payer = payer;
    }

    public void setCategory(Category category) {
        this.category = category; // nullable en BD, ok
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title no puede estar vacío");
        if (title.length() > 140)
            throw new IllegalArgumentException("title supera 140 caracteres");
        this.title = title;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null)
            throw new IllegalArgumentException("totalAmount no puede ser null");
        if (totalAmount.signum() <= 0)
            throw new IllegalArgumentException("totalAmount debe ser > 0");
        this.totalAmount = totalAmount;
    }

    public void setCurrency(String currency) {
        if (currency == null)
            throw new IllegalArgumentException("currency no puede ser null");
        String c = currency.trim().toUpperCase();
        if (c.length() != 3)
            throw new IllegalArgumentException("currency debe tener 3 letras (ej: EUR)");
        this.currency = c;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        if (expenseDate == null)
            throw new IllegalArgumentException("expenseDate no puede ser null");
        this.expenseDate = expenseDate;
    }

    public void setNote(String note) {
        this.note = note; // TEXT nullable, ok
    }

    public void setSplitMode(SplitMode splitMode) {
        this.splitMode = (splitMode != null) ? splitMode : SplitMode.EQUAL;
    }
}
