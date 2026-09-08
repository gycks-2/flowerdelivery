package com.yhc.flower_delivery.domain.auth.dto;

import com.yhc.flower_delivery.domain.account.entity.AccountRole;
import com.yhc.flower_delivery.domain.account.entity.AccountStatus;

public record SignupResponse(Long accountId, String email, AccountRole role, AccountStatus status) {}

