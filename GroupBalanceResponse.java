package com.splitwise.backend.dto;

import java.util.List;

/**
 * Full balance summary for a group: each member's net balance, plus a
 * simplified list of who-owes-whom transactions needed to settle up.
 */
public class GroupBalanceResponse {

    private Long groupId;
    private List<UserBalance> netBalances;
    private List<DebtResponse> simplifiedDebts;

    public GroupBalanceResponse() {
    }

    public GroupBalanceResponse(Long groupId, List<UserBalance> netBalances, List<DebtResponse> simplifiedDebts) {
        this.groupId = groupId;
        this.netBalances = netBalances;
        this.simplifiedDebts = simplifiedDebts;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<UserBalance> getNetBalances() {
        return netBalances;
    }

    public void setNetBalances(List<UserBalance> netBalances) {
        this.netBalances = netBalances;
    }

    public List<DebtResponse> getSimplifiedDebts() {
        return simplifiedDebts;
    }

    public void setSimplifiedDebts(List<DebtResponse> simplifiedDebts) {
        this.simplifiedDebts = simplifiedDebts;
    }
}
