package com.analytics.influencer_dashboard.dto;

import com.analytics.influencer_dashboard.entity.OrderStage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Light row for the dashboard table: carries a flag for the image, not the image itself. */
@Getter
@AllArgsConstructor
public class OrderRow {
    private Long id;
    private String customerName;
    private String dressName;
    private String color;
    private String dressSize;
    private String phone;
    private String address;
    private Integer amount;
    private boolean paid;
    private String utr;
    private OrderStage stage;
    private LocalDateTime createdAt;
    private boolean imageAttached;

    public String getCreatedLabel() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"));
    }
}