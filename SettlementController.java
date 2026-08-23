package com.splitwise.backend.controller;

import com.splitwise.backend.dto.SettlementRequest;
import com.splitwise.backend.dto.SettlementResponse;
import com.splitwise.backend.service.SettlementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@PathVariable Long groupId,
                                                                @RequestBody SettlementRequest request) {
        SettlementResponse response = settlementService.createSettlement(groupId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getSettlements(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlementsByGroup(groupId));
    }
}
