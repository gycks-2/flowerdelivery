package com.yhc.flower_delivery.domain.storeowner.entity;

import com.yhc.flower_delivery.domain.account.entity.Account;
import com.yhc.flower_delivery.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_owners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreOwner extends BaseEntity {
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

    public StoreOwner(Account account, String name, String phone) {
        this.account = account;
        this.name = name;
        this.phone = phone;
    }
}

