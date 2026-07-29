package com.jiubuntu.wms.biz.dashboard.application;

import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyOverviewSummaryResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.CompanyStatsResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.ExpiringInventoryItemResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.InboundQueueItemResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.InventoryHealthStatus;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.InventoryStatusItemResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.OutboundQueueItemResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.ProcessingTrendPointResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.StageBreakdownResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseOpsRowResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseOpsStatus;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeStatsResult;
import com.jiubuntu.wms.biz.dashboard.application.dto.result.WarehouseScopeSummaryResult;
import com.jiubuntu.wms.biz.inbound.application.InboundService;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryExpiringRow;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryProductSummaryRow;
import com.jiubuntu.wms.biz.outbound.application.OutboundService;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.application.dto.result.WarehouseResult;
import com.jiubuntu.wms.biz.location.application.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int QUEUE_LIMIT = 5;
    private static final int EXPIRING_LIMIT = 5;
    private static final int INVENTORY_STATUS_LIMIT = 5;
    private static final int EXPIRING_SOON_DAYS = 7;
    private static final int TREND_DAYS = 14;
    private static final double LOW_STOCK_RATIO_THRESHOLD = 0.2;
    private static final long WAREHOUSE_ALERT_EXPIRING_THRESHOLD = 5;
    private static final long WAREHOUSE_ALERT_OUTBOUND_PENDING_THRESHOLD = 15;
    private static final long WAREHOUSE_WATCH_EXPIRING_THRESHOLD = 2;
    private static final long WAREHOUSE_WATCH_OUTBOUND_PENDING_THRESHOLD = 8;

    private final WarehouseService warehouseService;
    private final LocationService locationService;
    private final OutboundService outboundService;
    private final InboundService inboundService;
    private final InventoryService inventoryService;
    private final ProductService productService;

    public WarehouseScopeSummaryResult getWarehouseScopeSummary(Long warehouseId, Long companyId, UserRole role,
                                                                  Long principalWarehouseId) {
        warehouseService.getAccessible(warehouseId, companyId, role, principalWarehouseId);

        long outboundPending = outboundService.countByWarehouseAndStatus(warehouseId, OutboundStatus.PENDING);
        long outboundPicking = outboundService.countByWarehouseAndStatus(warehouseId, OutboundStatus.PICKING);
        long outboundCompletedToday = outboundService.countCompletedTodayByWarehouse(warehouseId);
        long inboundPending = inboundService.countByWarehouseAndStatus(warehouseId, InboundStatus.PENDING);
        long expiringSoonCount = inventoryService.countExpiringSoon(warehouseId, EXPIRING_SOON_DAYS);

        WarehouseScopeStatsResult stats = new WarehouseScopeStatsResult(
                outboundPending, outboundPicking, outboundCompletedToday, inboundPending, expiringSoonCount);

        List<OutboundQueueItemResult> outboundQueue = buildOutboundQueue(warehouseId);
        List<InboundQueueItemResult> inboundQueue = buildInboundQueue(warehouseId);
        List<ExpiringInventoryItemResult> expiringInventory = buildExpiringInventory(warehouseId);
        List<InventoryStatusItemResult> inventoryStatus = buildInventoryStatus(warehouseId);

        return new WarehouseScopeSummaryResult(stats, outboundQueue, inboundQueue, expiringInventory, inventoryStatus);
    }

    public CompanyOverviewSummaryResult getCompanyOverviewSummary(Long companyId, UserRole role, Long principalWarehouseId) {
        List<WarehouseResult> warehouses = warehouseService.listAll(companyId, role, principalWarehouseId);
        List<Long> warehouseIds = warehouses.stream().map(WarehouseResult::getId).toList();

        Map<Long, Long> outboundPendingByWarehouse = outboundService.countByWarehousesAndStatus(warehouseIds, OutboundStatus.PENDING);
        Map<Long, Long> outboundPickingByWarehouse = outboundService.countByWarehousesAndStatus(warehouseIds, OutboundStatus.PICKING);
        Map<Long, Long> inboundPendingByWarehouse = inboundService.countByWarehousesAndStatus(warehouseIds, InboundStatus.PENDING);
        Map<Long, Long> expiringSoonByWarehouse = inventoryService.countExpiringSoonGroupedByWarehouses(warehouseIds, EXPIRING_SOON_DAYS);
        Map<Long, Long> locationCountByWarehouse = locationService.countActiveByWarehouseIds(warehouseIds);

        List<WarehouseOpsRowResult> warehouseOps = new ArrayList<>();
        long totalOutboundPending = 0;
        long totalOutboundPicking = 0;
        long totalInboundPending = 0;
        long totalExpiringSoon = 0;
        for (WarehouseResult warehouse : warehouses) {
            long pending = outboundPendingByWarehouse.getOrDefault(warehouse.getId(), 0L);
            long picking = outboundPickingByWarehouse.getOrDefault(warehouse.getId(), 0L);
            long inboundPending = inboundPendingByWarehouse.getOrDefault(warehouse.getId(), 0L);
            long expiringSoon = expiringSoonByWarehouse.getOrDefault(warehouse.getId(), 0L);
            long locationCount = locationCountByWarehouse.getOrDefault(warehouse.getId(), 0L);

            warehouseOps.add(new WarehouseOpsRowResult(
                    warehouse.getId(),
                    warehouse.getName(),
                    "위치 " + locationCount,
                    pending, picking, inboundPending, expiringSoon,
                    classifyWarehouseStatus(pending, expiringSoon)
            ));

            totalOutboundPending += pending;
            totalOutboundPicking += picking;
            totalInboundPending += inboundPending;
            totalExpiringSoon += expiringSoon;
        }

        CompanyStatsResult companyStats = new CompanyStatsResult(
                totalOutboundPending, totalOutboundPicking, totalInboundPending, totalExpiringSoon);

        List<ProcessingTrendPointResult> processingTrend = buildProcessingTrend(companyId);
        StageBreakdownResult todayOutboundStatus = buildOutboundStageBreakdown(companyId);
        StageBreakdownResult todayInboundStatus = buildInboundStageBreakdown(companyId);
        long productSkuCount = productService.countByCompany(companyId);

        return new CompanyOverviewSummaryResult(
                productSkuCount, companyStats, warehouseOps, processingTrend, todayOutboundStatus, todayInboundStatus);
    }

    private List<OutboundQueueItemResult> buildOutboundQueue(Long warehouseId) {
        LocalDateTime now = LocalDateTime.now();
        List<OutboundListResult> rows = outboundService.findWaitingQueue(warehouseId, QUEUE_LIMIT);
        return rows.stream()
                .map(row -> new OutboundQueueItemResult(
                        row.getId(),
                        row.getCustomerName(),
                        row.getItemCount(),
                        ChronoUnit.MINUTES.between(row.getCreatedAt(), now),
                        row.getStatus()
                ))
                .toList();
    }

    private List<InboundQueueItemResult> buildInboundQueue(Long warehouseId) {
        List<InboundListResult> rows = inboundService.findWaitingQueue(warehouseId, QUEUE_LIMIT);
        return rows.stream()
                .map(row -> new InboundQueueItemResult(
                        row.getId(), row.getSupplierName(), row.getItemCount(), row.getCreatedAt(), row.getStatus()))
                .toList();
    }

    private List<ExpiringInventoryItemResult> buildExpiringInventory(Long warehouseId) {
        LocalDate today = LocalDate.now();
        List<InventoryExpiringRow> rows = inventoryService.findExpiringSoon(warehouseId, EXPIRING_SOON_DAYS, EXPIRING_LIMIT);
        return rows.stream()
                .map(row -> new ExpiringInventoryItemResult(
                        row.getProductName(),
                        row.getLotNumber(),
                        row.getLocationCode(),
                        row.getQuantity(),
                        ChronoUnit.DAYS.between(today, row.getExpiryDate())
                ))
                .toList();
    }

    private List<InventoryStatusItemResult> buildInventoryStatus(Long warehouseId) {
        List<InventoryProductSummaryRow> rows = inventoryService.findProductSummary(warehouseId);
        return rows.stream()
                .map(row -> {
                    int available = row.getQuantity() - row.getReservedQuantity();
                    return new InventoryStatusItemResult(
                            row.getProductName(), row.getSkuCode(), row.getQuantity(), available,
                            classifyInventoryHealth(row.getQuantity(), available));
                })
                .sorted((a, b) -> Double.compare(availableRatio(a), availableRatio(b)))
                .limit(INVENTORY_STATUS_LIMIT)
                .toList();
    }

    private double availableRatio(InventoryStatusItemResult item) {
        return item.getQuantity() == 0 ? 0 : (double) item.getAvailableQuantity() / item.getQuantity();
    }

    private InventoryHealthStatus classifyInventoryHealth(int quantity, int availableQuantity) {
        if (availableQuantity <= 0) {
            return InventoryHealthStatus.UNAVAILABLE;
        }
        if (quantity > 0 && (double) availableQuantity / quantity < LOW_STOCK_RATIO_THRESHOLD) {
            return InventoryHealthStatus.LOW;
        }
        return InventoryHealthStatus.NORMAL;
    }

    private WarehouseOpsStatus classifyWarehouseStatus(long outboundPending, long expiringSoonCount) {
        if (expiringSoonCount >= WAREHOUSE_ALERT_EXPIRING_THRESHOLD || outboundPending >= WAREHOUSE_ALERT_OUTBOUND_PENDING_THRESHOLD) {
            return WarehouseOpsStatus.ALERT;
        }
        if (expiringSoonCount >= WAREHOUSE_WATCH_EXPIRING_THRESHOLD || outboundPending >= WAREHOUSE_WATCH_OUTBOUND_PENDING_THRESHOLD) {
            return WarehouseOpsStatus.WATCH;
        }
        return WarehouseOpsStatus.NORMAL;
    }

    private List<ProcessingTrendPointResult> buildProcessingTrend(Long companyId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(TREND_DAYS - 1L);

        Map<LocalDate, Long> outboundCounts = outboundService.countCompletedByDateRange(companyId, from, today);
        Map<LocalDate, Long> inboundCounts = inboundService.countCompletedByDateRange(companyId, from, today);

        List<ProcessingTrendPointResult> points = new ArrayList<>();
        for (int offset = TREND_DAYS - 1; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            String label = offset == 0 ? "오늘" : offset + "일 전";
            points.add(new ProcessingTrendPointResult(
                    label,
                    outboundCounts.getOrDefault(date, 0L),
                    inboundCounts.getOrDefault(date, 0L)
            ));
        }
        return points;
    }

    private StageBreakdownResult buildOutboundStageBreakdown(Long companyId) {
        Map<OutboundStatus, Long> counts = outboundService.countTodayGroupedByStatus(companyId);
        return new StageBreakdownResult(
                counts.getOrDefault(OutboundStatus.PENDING, 0L),
                counts.getOrDefault(OutboundStatus.PICKING, 0L),
                counts.getOrDefault(OutboundStatus.COMPLETED, 0L),
                counts.getOrDefault(OutboundStatus.CANCELLED, 0L)
        );
    }

    private StageBreakdownResult buildInboundStageBreakdown(Long companyId) {
        Map<InboundStatus, Long> counts = inboundService.countTodayGroupedByStatus(companyId);
        return new StageBreakdownResult(
                counts.getOrDefault(InboundStatus.PENDING, 0L),
                counts.getOrDefault(InboundStatus.IN_PROGRESS, 0L),
                counts.getOrDefault(InboundStatus.COMPLETED, 0L),
                counts.getOrDefault(InboundStatus.CANCELLED, 0L)
        );
    }

}
