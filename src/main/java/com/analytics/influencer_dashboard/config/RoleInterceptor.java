package com.analytics.influencer_dashboard.config;

import com.analytics.influencer_dashboard.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class RoleInterceptor implements HandlerInterceptor {

    private final Role required;

    public RoleInterceptor(Role required) {
        this.required = required;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {

        HttpSession session = request.getSession(false);
        Role role = session == null ? null : Role.fromText((String) session.getAttribute(SessionKeys.ROLE));

        if (role == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (role != required) {
            // Logged in, but this area belongs to the other dashboard
            response.sendRedirect(request.getContextPath() + role.getHome());
            return false;
        }
        return true;
    }
}