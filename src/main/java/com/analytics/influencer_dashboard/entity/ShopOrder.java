package com.analytics.influencer_dashboard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_orders")
@Getter
@Setter
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    private String dressName;
    private String color;
    private String dressSize;

    @Column(nullable = false)
    private String phone;

    // Full delivery address (can be several lines)
    @Column(length = 500)
    private String address;

    // Given in the pasted text
    private Integer amount;

    // Becomes true only when a payment photo / screenshot is attached
    private boolean paid;

    // UTR / UPI transaction reference read from the payment image (or typed in)
    @Column(length = 40)
    private String utr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStage stage = OrderStage.NEW;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "payment_image", columnDefinition = "bytea")
    private byte[] paymentImage;
}