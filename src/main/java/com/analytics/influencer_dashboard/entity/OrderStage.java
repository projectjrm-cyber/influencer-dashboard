package com.analytics.influencer_dashboard.entity;

public enum OrderStage {
    NEW("New"),
    ORDERED("Manufacturing"),   // order placed with the manufacturer, work in progress
    PACKED("Packed"),
    DISPATCHED("Dispatched");

    private final String label;

    OrderStage(String label) { this.label = label; }

    public String getLabel() { return label; }

    /** Returns null for unknown text. */
    public static OrderStage fromText(String text) {
        if (text == null) return null;
        try {
            return valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}