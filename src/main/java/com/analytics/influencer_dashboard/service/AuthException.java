package com.analytics.influencer_dashboard.service;

/** Error whose message is safe to show to the user. */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}