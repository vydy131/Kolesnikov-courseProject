package com.investagg.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_data")
@Getter
@Setter
@NoArgsConstructor
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "RUB";

    @Column(length = 100)
    private String source;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    private OffsetDateTime fetchedAt = OffsetDateTime.now();
}
