package com.analytics.influencer_dashboard.controller;

import com.analytics.influencer_dashboard.entity.Consultation;
import com.analytics.influencer_dashboard.repository.ConsultationRepository;
import com.analytics.influencer_dashboard.service.ConsultationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 Login, registration and logout now live in AuthController.
 Access to every URL below is enforced by RoleInterceptor (see WebConfig).
*/
@Controller
public class DashboardController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private ConsultationRepository consultationRepository;

    /*
     =====================================================
     PAYMENT IMAGE
     =====================================================
    */
    @GetMapping("/payment-image/{id}")
    public ResponseEntity<byte[]> getPaymentImage(@PathVariable Long id) {

        Consultation consultation =
                consultationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Consultation not found"));

        if (consultation.getPaymentImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(consultation.getPaymentImage());
    }

    /*
     =====================================================
     DASHBOARD PAGE
     =====================================================
    */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        List<Consultation> consultations =
                consultationService.getAllConsultations();

        model.addAttribute("consultations", consultations);

        model.addAttribute("totalConsultations",
                consultationService.getTotalConsultations());

        model.addAttribute("pendingConsultations",
                consultationService.getPendingConsultations());

        model.addAttribute("completedConsultations",
                consultationService.getCompletedConsultations());

        model.addAttribute("approvedConsultations",
                consultationService.getApprovedConsultations());

        model.addAttribute("totalRevenue",
                consultationService.getTotalRevenue());

        // Auto refresh every 5 minutes
        model.addAttribute("refreshInterval", 300);

        return "dashboard";
    }

    /*
     =====================================================
     NEW CONSULTATION PAGE
     =====================================================
    */
    @GetMapping("/new-consultation")
    public String newConsultationPage(HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        return "new-consultation";
    }

    /*
     =====================================================
     SAVE CONSULTATION
     =====================================================
    */
    @PostMapping("/consultation/save")
    public String saveConsultation(
            @RequestParam String rawText,
            @RequestParam(value = "paymentImage", required = false) MultipartFile paymentImage,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) throws IOException {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        // --- Automatic parsing ---
        String[] lines = rawText.split("\\n");

        String name = "Unknown";
        String instagramId = "@unknown";
        String consultationType = "Video";
        String preferredSlot = "Flexible";

        for (String line : lines) {
            String trimmedLine = line.trim();
            String lowerLine = trimmedLine.toLowerCase();

            if (lowerLine.startsWith("name:")) {
                name = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
            }
            else if (lowerLine.startsWith("instagram id:") || lowerLine.startsWith("instagram:") || lowerLine.startsWith("ig:")) {
                instagramId = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
                if (!instagramId.startsWith("@")) {
                    instagramId = "@" + instagramId;
                }
            }
            else if (lowerLine.startsWith("consultation type:") || lowerLine.startsWith("type:") || lowerLine.startsWith("consultation:")) {
                consultationType = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
            }
            else if (lowerLine.startsWith("preferred slot:") || lowerLine.startsWith("slot:")) {
                preferredSlot = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
            }
        }

        // --- Backend validation rule ---
        // Block if it mentions 40, or doesn't contain a valid package (20 or 45)
        String typeLower = consultationType.toLowerCase();
        if (typeLower.contains("40") || (!typeLower.contains("20") && !typeLower.contains("45"))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid Format: The package duration must be exactly (20 Min) or (45 Min). Received: '"
                            + consultationType + "'");
            return "redirect:/dashboard";
        }

        Consultation consultation = new Consultation();
        consultation.setStatus("PENDING");
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setName(name);
        consultation.setInstagramId(instagramId);
        consultation.setConsultationType(consultationType);
        consultation.setPreferredSlot(preferredSlot);

        consultationService.saveConsultation(consultation, paymentImage);
        return "redirect:/dashboard";
    }

    /*
     =====================================================
     UPDATE STATUS
     =====================================================
    */
    @PostMapping("/update-status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session
    ) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        consultationService.updateStatus(id, status);
        return "redirect:/dashboard";
    }

    /*
     =====================================================
     DELETE CONSULTATION
     =====================================================
    */
    @GetMapping("/consultation/delete/{id}")
    public String deleteConsultation(
            @PathVariable Long id,
            HttpSession session
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        consultationService.deleteConsultation(id);
        return "redirect:/dashboard";
    }

    /*
     =====================================================
     SET CONFIRMED SLOT
     =====================================================
    */
    @PostMapping("/consultation/set-slot/{id}")
    public String setConfirmedSlot(
            @PathVariable Long id,
            @RequestParam("confirmedSlot") String confirmedSlot,
            HttpSession session
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        Consultation consultation = consultationService.getConsultationById(id);
        if (consultation != null) {
            consultation.setConfirmedSlot(confirmedSlot);
            consultationService.updateConsultation(consultation);
        }
        return "redirect:/dashboard";
    }

    /*
     =====================================================
     UPDATE STATUS (link version)
     =====================================================
    */
    @GetMapping("/consultation/status/{id}/{status}")
    public String updateConsultationStatus(
            @PathVariable Long id,
            @PathVariable String status,
            HttpSession session
    ) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        Consultation consultation = consultationService.getConsultationById(id);
        if (consultation != null) {
            consultation.setStatus(status.toUpperCase());
            consultationService.updateConsultation(consultation);
        }
        return "redirect:/dashboard";
    }

    /*
     =====================================================
     UPDATE PAYMENT PROOF
     =====================================================
    */
    @PostMapping("/consultation/update-payment")
    public String updatePaymentProof(@RequestParam("consultationId") Long id,
                                     @RequestParam("paymentImage") MultipartFile file,
                                     HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        try {
            if (!file.isEmpty()) {
                Optional<Consultation> optionalConsultation = consultationRepository.findById(id);

                if (optionalConsultation.isPresent()) {
                    Consultation consultation = optionalConsultation.get();
                    consultation.setPaymentImage(file.getBytes());
                    consultationRepository.save(consultation);
                    System.out.println("Success: linked payment screenshot to Consultation ID: " + id);
                } else {
                    System.out.println("Error: Consultation ID " + id + " not found in database.");
                }
            }
        } catch (Exception e) {
            System.err.println("Exception occurred while uploading payment image:");
            e.printStackTrace();
        }

        return "redirect:/dashboard";
    }
}