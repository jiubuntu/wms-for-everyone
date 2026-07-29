package com.jiubuntu.wms.biz.dashboard.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundItem;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundItemRepository;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundRepository;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItem;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundItemRepository;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundRepository;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.ProductUnitRepository;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.infrastructure.WarehouseRepository;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class DashboardApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OutboundRepository outboundRepository;

    @Autowired
    private OutboundItemRepository outboundItemRepository;

    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private InboundItemRepository inboundItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company seedCompany(String name, String businessNumber) {
        return companyRepository.save(new Company(name, businessNumber, CompanyStatus.ACTIVE));
    }

    private User seedUser(Company company, Warehouse warehouse, String email, UserRole role) {
        return userRepository.save(new User(company, warehouse, email, passwordEncoder.encode("password1!"),
                "홍길동", "010-0000-0000", role, UserStatus.ACTIVE, false));
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password1!"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    private Warehouse seedWarehouse(Company company, String name) {
        return warehouseRepository.save(new Warehouse(company, name, "서울시"));
    }

    private Location seedLocation(Warehouse warehouse, String code) {
        return locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code(code).build());
    }

    private CommonCode seedCommonCode(CommonCodeGroup group, String code) {
        return commonCodeRepository.save(new CommonCode(null, group, code, code, 1));
    }

    private ProductUnit seedProductUnit(Company company, String name) {
        return productUnitRepository.save(new ProductUnit(company, name));
    }

    private Product seedProduct(Company company, CommonCode category, CommonCode storageType, ProductUnit baseUnit, String skuCode) {
        return productRepository.save(new Product(
                company, skuCode, "대시보드테스트상품", category, storageType, baseUnit, null, null, false, null));
    }

    private void seedInventory(Location location, Product product, LocalDate expiryDate, int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(location, product, null, null, expiryDate, quantity);
        inventory.reserve(reservedQuantity);
        inventoryRepository.save(inventory);
    }

    private void seedPendingOutbound(Company company, Warehouse warehouse, Product product, ProductUnit unit, String customerName) {
        Outbound outbound = outboundRepository.save(new Outbound(company, warehouse, customerName, null));
        outboundItemRepository.save(new OutboundItem(outbound, product, unit, 10, AllocationType.MANUAL));
    }

    private void seedPendingInbound(Company company, Warehouse warehouse, Product product, ProductUnit unit, String supplierName) {
        Inbound inbound = inboundRepository.save(new Inbound(company, warehouse, supplierName, null));
        inboundItemRepository.save(new InboundItem(inbound, product, unit, 10, null, null, null));
    }

    @Test
    @DisplayName("창고관리자는 담당 창고의 대시보드 요약을 조회한다")
    void getWarehouseScopeSummary_success() throws Exception {
        Company company = seedCompany("대시보드창고기업", "111-11-11111");
        Warehouse warehouse = seedWarehouse(company, "대시보드창고1");
        Location location = seedLocation(warehouse, "A-01-01-1");
        seedUser(company, warehouse, "dash-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("dash-manager@test.com");

        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "DASH-CAT1");
        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "DASH-STORE1");
        ProductUnit unit = seedProductUnit(company, "박스1");
        Product product = seedProduct(company, category, storageType, unit, "SKU-DASH-1");

        seedInventory(location, product, LocalDate.now().plusDays(3), 100, 90);
        seedPendingOutbound(company, warehouse, product, unit, "테스트거래처");
        seedPendingInbound(company, warehouse, product, unit, "테스트공급처");

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stats.outboundPending").value(1))
                .andExpect(jsonPath("$.data.stats.inboundPending").value(1))
                .andExpect(jsonPath("$.data.stats.expiringSoonCount").value(1))
                .andExpect(jsonPath("$.data.outboundQueue.length()").value(1))
                .andExpect(jsonPath("$.data.outboundQueue[0].customerName").value("테스트거래처"))
                .andExpect(jsonPath("$.data.inboundQueue[0].supplierName").value("테스트공급처"))
                .andExpect(jsonPath("$.data.expiringInventory[0].daysLeft").value(3))
                .andExpect(jsonPath("$.data.inventoryStatus[0].status").value("LOW"));
    }

    @Test
    @DisplayName("창고관리자가 담당 창고가 아닌 창고의 대시보드를 조회하면 거부된다")
    void getWarehouseScopeSummary_otherWarehouse_rejected() throws Exception {
        Company company = seedCompany("대시보드스코프기업", "222-22-22222");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "dash-scope-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("dash-scope-manager@test.com");

        mockMvc.perform(get("/api/warehouse/" + otherWarehouse.getId() + "/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("기업관리자는 전사 요약에서 창고별 운영 현황을 전부 조회한다")
    void getCompanyOverviewSummary_success() throws Exception {
        Company company = seedCompany("대시보드전사기업", "333-33-33333");
        Warehouse warehouse1 = seedWarehouse(company, "전사창고1");
        Warehouse warehouse2 = seedWarehouse(company, "전사창고2");
        seedUser(company, null, "dash-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("dash-admin@test.com");

        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "DASH-CAT2");
        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "DASH-STORE2");
        ProductUnit unit = seedProductUnit(company, "박스2");
        Product product = seedProduct(company, category, storageType, unit, "SKU-DASH-2");

        seedPendingOutbound(company, warehouse1, product, unit, "전사거래처1");
        seedPendingOutbound(company, warehouse2, product, unit, "전사거래처2");

        mockMvc.perform(get("/api/company/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.warehouseOps.length()").value(2))
                .andExpect(jsonPath("$.data.companyStats.outboundPending").value(2))
                .andExpect(jsonPath("$.data.processingTrend.length()").value(14))
                .andExpect(jsonPath("$.data.processingTrend[13].label").value("오늘"));
    }

    @Test
    @DisplayName("작업자는 전사 요약을 조회할 수 없다")
    void getCompanyOverviewSummary_deniedForWorker() throws Exception {
        Company company = seedCompany("대시보드작업자거부기업", "444-44-44444");
        Warehouse warehouse = seedWarehouse(company, "작업자창고");
        seedUser(company, warehouse, "dash-worker@test.com", UserRole.WORKER);
        String token = login("dash-worker@test.com");

        mockMvc.perform(get("/api/company/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

}
