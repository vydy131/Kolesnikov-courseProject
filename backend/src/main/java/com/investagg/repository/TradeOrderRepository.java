package com.investagg.repository;

import com.investagg.entity.TradeOrder;
import com.investagg.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, UUID> {

    @Query("SELECT o FROM TradeOrder o WHERE o.account.user.id = :userId AND o.deletedAt IS NULL")
    Page<TradeOrder> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT o FROM TradeOrder o WHERE o.account.user.id = :userId AND o.orderStatus = :status AND o.deletedAt IS NULL")
    Page<TradeOrder> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status, Pageable pageable);

    Optional<TradeOrder> findByIdAndDeletedAtIsNull(UUID id);
}
