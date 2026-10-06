package com.barpro.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @Column(name = "public_token", nullable = false, unique = true, length = 64)
    private String publicToken;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "event_type_id")
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(nullable = false)
    private int guests;

    @Column(name = "duration_hours", nullable = false)
    private int durationHours;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestLevel level = RequestLevel.BASE;

    @Column(name = "extra_bartenders", nullable = false)
    private int extraBartenders;

    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "premium_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumAmount = BigDecimal.ZERO;

    @Column(name = "personal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal personalAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDIENTE;

    @Column(name = "client_name", nullable = false, length = 120)
    private String clientName;

    @Column(name = "client_email", length = 150)
    private String clientEmail;

    @Column(name = "client_whatsapp", length = 30)
    private String clientWhatsapp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "request_cocktails",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "cocktail_id"))
    private Set<Cocktail> cocktails = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
