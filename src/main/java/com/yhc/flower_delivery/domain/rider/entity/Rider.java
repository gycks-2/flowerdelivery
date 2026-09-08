package com.yhc.flower_delivery.domain.rider.entity;

import com.yhc.flower_delivery.domain.account.entity.Account;
import com.yhc.flower_delivery.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rider extends BaseEntity {
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

    @Column(name = "vehicle_info")
    private String vehicleInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private RiderApprovalStatus approvalStatus;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "is_online", nullable = false)
    private boolean online;

    public Rider(Account account, String name, String phone, String vehicleInfo) {
        this.account = account;
        this.name = name;
        this.phone = phone;
        this.vehicleInfo = vehicleInfo;
        this.approvalStatus = RiderApprovalStatus.PENDING;
        this.online = false;
    }
}

