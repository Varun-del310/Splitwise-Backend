package com.splitwise.backend.service;

import com.splitwise.backend.dto.GroupBalanceResponse;

public interface BalanceService {

    GroupBalanceResponse getGroupBalances(Long groupId);
}
