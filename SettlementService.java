package com.splitwise.backend.service;

import com.splitwise.backend.dto.SettlementRequest;
import com.splitwise.backend.dto.SettlementResponse;

import java.util.List;

public interface SettlementService {

    SettlementResponse createSettlement(Long groupId, SettlementRequest request);

    List<SettlementResponse> getSettlementsByGroup(Long groupId);
}
