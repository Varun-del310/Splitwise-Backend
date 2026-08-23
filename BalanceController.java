package com.splitwise.backend.controller;

import com.splitwise.backend.dto.GroupBalanceResponse;
import com.splitwise.backend.service.BalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<GroupBalanceResponse> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.getGroupBalances(groupId));
    }
}
