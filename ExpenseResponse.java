package com.splitwise.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private Long groupId;
    private UserResponse paidBy;
    private List<ExpenseParticipantResponse> participants;
    private LocalDateTime createdAt;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, String description, BigDecimal amount, Long groupId,
                            UserResponse paidBy, List<ExpenseParticipantResponse> participants,
                            LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.participants = participants;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public List<ExpenseParticipantResponse> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ExpenseParticipantResponse> participants) {
        this.participants = participants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
