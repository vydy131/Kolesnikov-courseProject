package com.investagg.entity;

import com.investagg.entity.enums.OrderDirection;
import com.investagg.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_order")
@Getter
@Setter
@NoArgsConstructor
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private InvestmentAccount account;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderDirection direction;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal qty;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "broker_order_id", length = 255)
    private String brokerOrderId;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private OffsetDateTime placedAt = OffsetDateTime.now();

    @Column(name = "filled_at")
    private OffsetDateTime filledAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
