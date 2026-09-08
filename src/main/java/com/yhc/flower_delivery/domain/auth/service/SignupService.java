package com.yhc.flower_delivery.domain.auth.service;

import com.yhc.flower_delivery.domain.account.entity.*;
import com.yhc.flower_delivery.domain.account.repository.AccountRepository;
import com.yhc.flower_delivery.domain.auth.dto.*;
import com.yhc.flower_delivery.domain.customer.entity.Customer;
import com.yhc.flower_delivery.domain.storeowner.entity.StoreOwner;
import com.yhc.flower_delivery.domain.rider.entity.Rider;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Validated
@RequiredArgsConstructor
public class SignupService {
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public SignupResponse signup(@Valid SignupRequest request) {
        if (request.role() == AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자 계정은 회원가입으로 생성할 수 없습니다.");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
        Account account = accounts.save(Account.create(email, passwordEncoder.encode(request.password()), request.role()));
        switch (request.role()) {
            case CUSTOMER -> entityManager.persist(new Customer(account, request.name().strip(), request.phone().strip()));
            case STORE_OWNER -> entityManager.persist(new StoreOwner(account, request.name().strip(), request.phone().strip()));
            case RIDER -> entityManager.persist(new Rider(account, request.name().strip(), request.phone().strip(), request.vehicleInfo()));
            case ADMIN -> throw new IllegalStateException("Unsupported signup role");
        }
        entityManager.flush();
        return new SignupResponse(account.getId(), account.getEmail(), account.getRole(), account.getStatus());
    }
}

