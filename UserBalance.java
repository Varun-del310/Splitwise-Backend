package com.splitwise.backend.dto;

import java.math.BigDecimal;

/**
 * Represents a single user's net balance within a group.
 * A positive netBalance means the user is owed money overall.
 * A negative netBalance means the user owes money overall.
 */
public class UserBalance {

    private Long userId;
    private String name;
    private BigDecimal netBalance;

    public UserBalance() {
    }

    public UserBalance(Long userId, String name, BigDecimal netBalance) {
        this.userId = userId;
        this.name = name;
        this.netBalance = netBalance;
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

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}
