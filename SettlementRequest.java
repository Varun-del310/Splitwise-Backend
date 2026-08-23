package com.splitwise.backend.dto;

import java.math.BigDecimal;

public class SettlementRequest {

    private Long paidById;
    private Long paidToId;
    private BigDecimal amount;

    public SettlementRequest() {
    }

    public Long getPaidById() {
        return paidById;
    }

    public void setPaidById(Long paidById) {
        this.paidById = paidById;
    }

    public Long getPaidToId() {
        return paidToId;
    }

    public void setPaidToId(Long paidToId) {
        this.paidToId = paidToId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
