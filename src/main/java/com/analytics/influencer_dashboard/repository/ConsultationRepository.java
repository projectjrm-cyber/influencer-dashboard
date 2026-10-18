package com.analytics.influencer_dashboard.repository;

import com.analytics.influencer_dashboard.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository
        extends JpaRepository<Consultation, Long> {
}