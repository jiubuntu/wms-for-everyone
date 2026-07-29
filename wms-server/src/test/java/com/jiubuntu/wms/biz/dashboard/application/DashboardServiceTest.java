package com.jiubuntu.wms.biz.dashboard.application;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyOverviewSummaryResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.InventoryHealthStatus;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseOpsStatus;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeSummaryResult;
import com.jiubuntu.wms.biz.inbound.application.InboundService;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryExpiringRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryProductSummaryRow;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.outbound.application.OutboundService;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.application.dto.result.WarehouseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private LocationService locationService;

    @Mock
    private OutboundService outboundService;

    @Mock
    private InboundService inboundService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("창고-스코프 요약은 접근 권한을 확인한 뒤 각 도메인 집계 결과를 그대로 모은다")
    void getWarehouseScopeSummary_aggregatesStats() {
        when(outboundService.countByWarehouseAndStatus(10L, OutboundStatus.PENDING)).thenReturn(8L);
        when(outboundService.countByWarehouseAndStatus(10L, OutboundStatus.PICKING)).thenReturn(3L);
        when(outboundService.countCompletedTodayByWarehouse(10L)).thenReturn(12L);
        when(inboundService.countByWarehouseAndStatus(10L, InboundStatus.PENDING)).thenReturn(5L);
        when(inventoryService.countExpiringSoon(10L, 7)).thenReturn(4L);
        when(outboundService.findWaitingQueue(eq(10L), anyInt())).thenReturn(List.of());
        when(inboundService.findWaitingQueue(eq(10L), anyInt())).thenReturn(List.of());
        when(inventoryService.findExpiringSoon(eq(10L), anyInt(), anyInt())).thenReturn(List.of());
        when(inventoryService.findProductSummary(10L)).thenReturn(List.of());

        WarehouseScopeSummaryResult result = dashboardService.getWarehouseScopeSummary(
                10L, 1L, UserRole.WAREHOUSE_MANAGER, 10L);

        assertThat(result.getStats().getOutboundPending()).isEqualTo(8L);
        assertThat(result.getStats().getOutboundPicking()).isEqualTo(3L);
        assertThat(result.getStats().getOutboundCompletedToday()).isEqualTo(12L);
        assertThat(result.getStats().getInboundPending()).isEqualTo(5L);
        assertThat(result.getStats().getExpiringSoonCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("창고-스코프 요약 조회 시 담당 창고 접근 권한을 확인한다")
    void getWarehouseScopeSummary_checksAccessible() {
        stubEmptyWarehouseScope(10L);

        dashboardService.getWarehouseScopeSummary(10L, 1L, UserRole.WORKER, 10L);

        org.mockito.Mockito.verify(warehouseService).getAccessible(10L, 1L, UserRole.WORKER, 10L);
    }

    @Test
    @DisplayName("유통기한 임박 재고는 만료일까지 남은 일수(D-day)를 계산해 담는다")
    void getWarehouseScopeSummary_computesDaysLeft() {
        stubEmptyWarehouseScope(10L);
        LocalDate expiryDate = LocalDate.now().plusDays(3);
        when(inventoryService.findExpiringSoon(eq(10L), anyInt(), anyInt())).thenReturn(List.of(
                new InventoryExpiringRow("두유", "LOT-1", "A-01-01", 24, expiryDate)
        ));

        WarehouseScopeSummaryResult result = dashboardService.getWarehouseScopeSummary(
                10L, 1L, UserRole.COMPANY_ADMIN, null);

        assertThat(result.getExpiringInventory()).hasSize(1);
        assertThat(result.getExpiringInventory().get(0).getDaysLeft()).isEqualTo(3L);
    }

    @Test
    @DisplayName("가용수량이 0이면 가용없음, 20% 미만이면 부족, 그 외에는 정상으로 분류한다")
    void getWarehouseScopeSummary_classifiesInventoryHealth() {
        stubEmptyWarehouseScope(10L);
        when(inventoryService.findProductSummary(10L)).thenReturn(List.of(
                new InventoryProductSummaryRow("완전소진", "SKU-1", 100, 100),
                new InventoryProductSummaryRow("부족상품", "SKU-2", 100, 90),
                new InventoryProductSummaryRow("정상상품", "SKU-3", 100, 50)
        ));

        WarehouseScopeSummaryResult result = dashboardService.getWarehouseScopeSummary(
                10L, 1L, UserRole.COMPANY_ADMIN, null);

        assertThat(result.getInventoryStatus()).extracting("productName", "status")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("완전소진", InventoryHealthStatus.UNAVAILABLE),
                        org.assertj.core.groups.Tuple.tuple("부족상품", InventoryHealthStatus.LOW),
                        org.assertj.core.groups.Tuple.tuple("정상상품", InventoryHealthStatus.NORMAL)
                );
    }

    @Test
    @DisplayName("전사 요약의 창고별 운영 현황은 유통기한임박 5건 이상이거나 출고대기 15건 이상이면 주의로 분류한다")
    void getCompanyOverviewSummary_classifiesAlertWarehouse() {
        WarehouseResult warehouse = WarehouseResult.builder().id(1L).companyId(1L).name("서울창고").build();
        when(warehouseService.listAll(1L, UserRole.COMPANY_ADMIN, null)).thenReturn(List.of(warehouse));
        when(outboundService.countByWarehousesAndStatus(any(), eq(OutboundStatus.PENDING))).thenReturn(Map.of(1L, 20L));
        when(outboundService.countByWarehousesAndStatus(any(), eq(OutboundStatus.PICKING))).thenReturn(Map.of());
        when(inboundService.countByWarehousesAndStatus(any(), eq(InboundStatus.PENDING))).thenReturn(Map.of());
        when(inventoryService.countExpiringSoonGroupedByWarehouses(any(), anyInt())).thenReturn(Map.of());
        when(locationService.countActiveByWarehouseIds(any())).thenReturn(Map.of(1L, 10L));
        stubEmptyCompanyTrendAndBreakdown();

        CompanyOverviewSummaryResult result = dashboardService.getCompanyOverviewSummary(1L, UserRole.COMPANY_ADMIN, null);

        assertThat(result.getWarehouseOps()).hasSize(1);
        assertThat(result.getWarehouseOps().get(0).getStatus()).isEqualTo(WarehouseOpsStatus.ALERT);
        assertThat(result.getWarehouseOps().get(0).getWarehouseNote()).isEqualTo("위치 10");
    }

    @Test
    @DisplayName("전사 요약의 companyStats는 창고별 집계를 합산한 값이다")
    void getCompanyOverviewSummary_sumsCompanyStats() {
        WarehouseResult warehouse1 = WarehouseResult.builder().id(1L).companyId(1L).name("서울창고").build();
        WarehouseResult warehouse2 = WarehouseResult.builder().id(2L).companyId(1L).name("부산창고").build();
        when(warehouseService.listAll(1L, UserRole.COMPANY_ADMIN, null)).thenReturn(List.of(warehouse1, warehouse2));
        when(outboundService.countByWarehousesAndStatus(any(), eq(OutboundStatus.PENDING)))
                .thenReturn(Map.of(1L, 5L, 2L, 3L));
        when(outboundService.countByWarehousesAndStatus(any(), eq(OutboundStatus.PICKING))).thenReturn(Map.of());
        when(inboundService.countByWarehousesAndStatus(any(), eq(InboundStatus.PENDING))).thenReturn(Map.of());
        when(inventoryService.countExpiringSoonGroupedByWarehouses(any(), anyInt())).thenReturn(Map.of());
        when(locationService.countActiveByWarehouseIds(any())).thenReturn(Map.of());
        stubEmptyCompanyTrendAndBreakdown();

        CompanyOverviewSummaryResult result = dashboardService.getCompanyOverviewSummary(1L, UserRole.COMPANY_ADMIN, null);

        assertThat(result.getCompanyStats().getOutboundPending()).isEqualTo(8L);
    }

    @Test
    @DisplayName("최근 14일 처리 건수 추이는 오늘을 포함해 14개 포인트를 만들고, 데이터가 없는 날은 0으로 채운다")
    void getCompanyOverviewSummary_buildsProcessingTrend() {
        when(warehouseService.listAll(1L, UserRole.COMPANY_ADMIN, null)).thenReturn(List.of());
        when(outboundService.countByWarehousesAndStatus(any(), any())).thenReturn(Map.of());
        when(inboundService.countByWarehousesAndStatus(any(), any())).thenReturn(Map.of());
        when(inventoryService.countExpiringSoonGroupedByWarehouses(any(), anyInt())).thenReturn(Map.of());
        when(locationService.countActiveByWarehouseIds(any())).thenReturn(Map.of());
        LocalDate today = LocalDate.now();
        when(outboundService.countCompletedByDateRange(eq(1L), any(), any())).thenReturn(Map.of(today, 7L));
        when(inboundService.countCompletedByDateRange(eq(1L), any(), any())).thenReturn(Map.of());
        when(outboundService.countTodayGroupedByStatus(1L)).thenReturn(Map.of());
        when(inboundService.countTodayGroupedByStatus(1L)).thenReturn(Map.of());
        when(productService.countByCompany(1L)).thenReturn(0L);

        CompanyOverviewSummaryResult result = dashboardService.getCompanyOverviewSummary(1L, UserRole.COMPANY_ADMIN, null);

        assertThat(result.getProcessingTrend()).hasSize(14);
        assertThat(result.getProcessingTrend().get(13).getLabel()).isEqualTo("오늘");
        assertThat(result.getProcessingTrend().get(13).getOutboundCount()).isEqualTo(7L);
        assertThat(result.getProcessingTrend().get(0).getLabel()).isEqualTo("13일 전");
        assertThat(result.getProcessingTrend().get(0).getOutboundCount()).isZero();
    }

    private void stubEmptyWarehouseScope(Long warehouseId) {
        org.mockito.Mockito.lenient().when(outboundService.countByWarehouseAndStatus(eq(warehouseId), any())).thenReturn(0L);
        org.mockito.Mockito.lenient().when(outboundService.countCompletedTodayByWarehouse(warehouseId)).thenReturn(0L);
        org.mockito.Mockito.lenient().when(inboundService.countByWarehouseAndStatus(eq(warehouseId), any())).thenReturn(0L);
        org.mockito.Mockito.lenient().when(inventoryService.countExpiringSoon(eq(warehouseId), anyInt())).thenReturn(0L);
        org.mockito.Mockito.lenient().when(outboundService.findWaitingQueue(eq(warehouseId), anyInt())).thenReturn(List.of());
        org.mockito.Mockito.lenient().when(inboundService.findWaitingQueue(eq(warehouseId), anyInt())).thenReturn(List.of());
        org.mockito.Mockito.lenient().when(inventoryService.findExpiringSoon(eq(warehouseId), anyInt(), anyInt())).thenReturn(List.of());
        org.mockito.Mockito.lenient().when(inventoryService.findProductSummary(warehouseId)).thenReturn(List.of());
    }

    private void stubEmptyCompanyTrendAndBreakdown() {
        when(outboundService.countCompletedByDateRange(eq(1L), any(), any())).thenReturn(Map.of());
        when(inboundService.countCompletedByDateRange(eq(1L), any(), any())).thenReturn(Map.of());
        when(outboundService.countTodayGroupedByStatus(1L)).thenReturn(Map.of());
        when(inboundService.countTodayGroupedByStatus(1L)).thenReturn(Map.of());
        when(productService.countByCompany(1L)).thenReturn(0L);
    }

}
