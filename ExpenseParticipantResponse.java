package com.splitwise.backend.dto;

import java.math.BigDecimal;

public class ExpenseParticipantResponse {

    private Long userId;
    private String name;
    private BigDecimal shareAmount;

    public ExpenseParticipantResponse() {
    }

    public ExpenseParticipantResponse(Long userId, String name, BigDecimal shareAmount) {
        this.userId = userId;
        this.name = name;
        this.shareAmount = shareAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }
}
