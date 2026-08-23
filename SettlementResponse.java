package com.splitwise.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementResponse {

    private Long id;
    private Long groupId;
    private UserResponse paidBy;
    private UserResponse paidTo;
    private BigDecimal amount;
    private LocalDateTime settledAt;

    public SettlementResponse() {
    }

    public SettlementResponse(Long id, Long groupId, UserResponse paidBy, UserResponse paidTo,
                                BigDecimal amount, LocalDateTime settledAt) {
        this.id = id;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.paidTo = paidTo;
        this.amount = amount;
        this.settledAt = settledAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public UserResponse getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(UserResponse paidBy) {
        this.paidBy = paidBy;
    }

    public UserResponse getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(UserResponse paidTo) {
        this.paidTo = paidTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }
}
