package com.analytics.influencer_dashboard.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String instagramId;

    private String consultationType;

    private String preferredSlot;

    private String confirmedSlot;

    private String status;

    private LocalDateTime createdAt;

    // PostgreSQL stores raw bytes in a "bytea" column (no @Lob needed)
    @Column(name = "payment_image", columnDefinition = "bytea")
    private byte[] paymentImage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInstagramId() { return instagramId; }
    public void setInstagramId(String instagramId) { this.instagramId = instagramId; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getPreferredSlot() { return preferredSlot; }
    public void setPreferredSlot(String preferredSlot) { this.preferredSlot = preferredSlot; }

    public String getConfirmedSlot() { return confirmedSlot; }
    public void setConfirmedSlot(String confirmedSlot) { this.confirmedSlot = confirmedSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public byte[] getPaymentImage() { return paymentImage; }
    public void setPaymentImage(byte[] paymentImage) { this.paymentImage = paymentImage; }
}