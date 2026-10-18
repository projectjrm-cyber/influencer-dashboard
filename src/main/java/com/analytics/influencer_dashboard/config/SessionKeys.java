package com.analytics.influencer_dashboard.config;

public final class SessionKeys {
    private SessionKeys() {}

    public static final String USER = "loggedInUser"; // same key DashboardController already checks
    public static final String ROLE = "role";
}