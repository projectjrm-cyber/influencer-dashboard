package com.influencer.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Value("${app.username}")
    private String appUsername;

    @Value("${app.password}")
    private String appPassword;

    /*
     =====================================================
     LOGIN PAGE
     =====================================================
    */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /*
     =====================================================
     LOGIN VALIDATION
     =====================================================
    */
    @PostMapping("/login")
    public String login(

            @RequestParam String username,

            @RequestParam String password,

            HttpSession session,

            Model model

    ) {

        if (username.equals(appUsername)
                &&
                password.equals(appPassword)) {

            session.setAttribute("loggedIn", true);

            return "redirect:/dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid Username or Password"
        );

        return "login";
    }

    /*
     =====================================================
     LOGOUT
     =====================================================
    */
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}