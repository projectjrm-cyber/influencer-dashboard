package com.analytics.influencer_dashboard.service;

import com.analytics.influencer_dashboard.entity.Consultation;
import com.analytics.influencer_dashboard.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;

    /*
     =====================================================
     SAVE CONSULTATION
     =====================================================
    */
    public Consultation saveConsultation(
            Consultation consultation,
            MultipartFile paymentImage
    ) throws IOException {

        /*
         ===============================================
         VALIDATIONS
         ===============================================
        */
        if (consultation.getName() == null
                || consultation.getName().isBlank()) {

            throw new RuntimeException(
                    "Name is required");
        }

        /*
         ===============================================
         SAVE IMAGE AS BYTE ARRAY
         ===============================================
        */
        if (paymentImage != null
                && !paymentImage.isEmpty()) {

            consultation.setPaymentImage(
                    paymentImage.getBytes()
            );
        }

        return consultationRepository.save(
                consultation
        );
    }

    /*
     =====================================================
     GET ALL CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public List<Consultation> getAllConsultations() {

        List<Consultation> consultations =
                consultationRepository.findAll();

        consultations.sort(
                Comparator.comparing(
                        Consultation::getCreatedAt
                ).reversed()
        );

        return consultations;
    }

    /*
     =====================================================
     GET CONSULTATION BY ID
     =====================================================
    */
    @Transactional(readOnly = true)
    public Consultation getConsultationById(Long id) {

        return consultationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Consultation Not Found"
                        ));
    }

    /*
     =====================================================
     UPDATE STATUS
     =====================================================
    */
    public void updateStatus(
            Long id,
            String status
    ) {

        Consultation consultation =
                getConsultationById(id);

        consultation.setStatus(
                status.toUpperCase()
        );

        consultationRepository.save(
                consultation
        );
    }

    /*
     =====================================================
     DELETE CONSULTATION
     =====================================================
    */
    public void deleteConsultation(Long id) {

        Consultation consultation =
                getConsultationById(id);

        consultationRepository.delete(
                consultation
        );
    }

    /*
     =====================================================
     TOTAL CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public long getTotalConsultations() {

        return consultationRepository.count();
    }

    /*
     =====================================================
     PENDING CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public long getPendingConsultations() {

        return consultationRepository.findAll()
                .stream()
                .filter(c ->
                        "PENDING".equalsIgnoreCase(
                                c.getStatus()
                        ))
                .count();
    }

    /*
     =====================================================
     COMPLETED CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public long getCompletedConsultations() {

        return consultationRepository.findAll()
                .stream()
                .filter(c ->
                        "COMPLETED".equalsIgnoreCase(
                                c.getStatus()
                        ))
                .count();
    }

    /*
     =====================================================
     APPROVED CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public long getApprovedConsultations() {

        return consultationRepository.findAll()
                .stream()
                .filter(c ->
                        "APPROVED".equalsIgnoreCase(
                                c.getStatus()
                        ))
                .count();
    }

    /*
     =====================================================
     REJECTED CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public long getRejectedConsultations() {

        return consultationRepository.findAll()
                .stream()
                .filter(c ->
                        "REJECTED".equalsIgnoreCase(
                                c.getStatus()
                        ))
                .count();
    }

    /*
     =====================================================
     TOTAL REVENUE
     =====================================================
    */
    @Transactional(readOnly = true)
    public int getTotalRevenue() {

        List<Consultation> consultations =
                consultationRepository.findAll();

        int totalRevenue = 0;

        for (Consultation consultation
                : consultations) {

            String type =
                    consultation.getConsultationType();

            if (type == null) {
                continue;
            }

            String lowerType =
                    type.toLowerCase();

            if (lowerType.contains("400")) {

                totalRevenue += 400;

            } else if (lowerType.contains("600")) {

                totalRevenue += 600;

            } else if (lowerType.contains("1000")) {

                totalRevenue += 1000;

            } else {

                totalRevenue += 500;
            }
        }

        return totalRevenue;
    }

    /*
     =====================================================
     RECENT CONSULTATIONS
     =====================================================
    */
    @Transactional(readOnly = true)
    public List<Consultation> getRecentConsultations(
            int limit
    ) {

        return getAllConsultations()
                .stream()
                .limit(limit)
                .toList();
    }

    /*
     =====================================================
     UPDATE CONSULTATION
     =====================================================
    */
    public void updateConsultation(
            Consultation consultation
    ) {

        consultationRepository.save(
                consultation
        );
    }
}