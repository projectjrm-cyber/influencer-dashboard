package com.analytics.influencer_dashboard.repository;

import com.analytics.influencer_dashboard.dto.OrderRow;
import com.analytics.influencer_dashboard.entity.OrderStage;
import com.analytics.influencer_dashboard.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    @Query("""
            select new com.analytics.influencer_dashboard.dto.OrderRow(
                o.id, o.customerName, o.dressName, o.color, o.dressSize, o.phone, o.address,
                o.amount, o.paid, o.utr, o.stage, o.createdAt,
                case when o.paymentImage is null then false else true end)
            from ShopOrder o
            order by o.createdAt desc
            """)
    List<OrderRow> findAllRows();

    // Small updates that never load or re-send the payment image
    @Modifying
    @Query("update ShopOrder o set o.stage = :stage where o.id = :id")
    int updateStage(@Param("id") Long id, @Param("stage") OrderStage stage);

    @Modifying
    @Query("delete from ShopOrder o where o.id = :id")
    int deleteOrder(@Param("id") Long id);

    long countByPaidFalse();

    long countByStage(OrderStage stage);

    long countByStageIn(Collection<OrderStage> stages);

    @Query("select coalesce(sum(o.amount), 0) from ShopOrder o where o.paid = true")
    Long sumPaidAmount();
}