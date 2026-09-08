package com.yhc.flower_delivery.domain.customer.entity;

import com.yhc.flower_delivery.domain.account.entity.Account;
import com.yhc.flower_delivery.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {
    @Id
    @Column(name = "account_id")
    private Long accountId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    public Customer(Account account, String name, String phone) {
        this.account = account;
        this.name = name;
        this.phone = phone;
    }
}

