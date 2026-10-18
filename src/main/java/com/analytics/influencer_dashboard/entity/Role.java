package com.analytics.influencer_dashboard.entity;

public enum Role {
    CONSULTATION("Consultation", "/dashboard"),
    ECOMMERCE("E-commerce", "/ecommerce/dashboard");

    private final String label;
    private final String home;

    Role(String label, String home) {
        this.label = label;
        this.home = home;
    }

    public String getLabel() { return label; }
    public String getHome() { return home; }

    /** Returns null for unknown text. */
    public static Role fromText(String text) {
        if (text == null) return null;
        try {
            return valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}