package com.jiubuntu.wms.biz.inventory.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryHistoryRepository;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class InventoryApiIntegrationTest extends IntegrationTestSupport {

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
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company seedCompany(String name, String businessNumber) {
        return companyRepository.save(new Company(name, businessNumber, CompanyStatus.ACTIVE));
    }

    private User seedUser(Company company, Warehouse warehouse, String email, UserRole role) {
        return userRepository.save(new User(company, warehouse, email, passwordEncoder.encode("password1!"),
                "홍길동", "010-0000-0000", role, UserStatus.ACTIVE));
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

    private CommonCode seedCommonCode(CommonCodeGroup group, String code) {
        return commonCodeRepository.save(new CommonCode(null, group, code, code, 1));
    }

    private Warehouse seedWarehouse(Company company, String name) {
        return warehouseRepository.save(new Warehouse(company, name, "서울시"));
    }

    private Location seedLocation(Warehouse warehouse, String code) {
        return locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code(code).build());
    }

    private ProductUnit seedProductUnit(Company company, String name) {
        return productUnitRepository.save(new ProductUnit(company, name));
    }

    private Product seedProduct(Company company, CommonCode category, CommonCode storageType, ProductUnit baseUnit,
                                 String skuCode) {
        return productRepository.save(new Product(
                company, skuCode, "테스트상품", category, storageType, baseUnit, null, null, false, null));
    }

    private Inventory seedInventory(Location location, Product product, int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(location, product, null, null, null, quantity);
        inventory.reserve(reservedQuantity);
        return inventoryRepository.save(inventory);
    }

    @Test
    @DisplayName("재고 목록을 조회하면 위치/상품 조인 필드까지 함께 내려온다")
    void list_success() throws Exception {
        Company company = seedCompany("재고조회기업", "111-11-11111");
        seedUser(company, null, "inv-list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-list-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE1");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT1");
        Warehouse warehouse = seedWarehouse(company, "재고조회창고");
        Location location = seedLocation(warehouse, "A-01-01-1");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-1");
        seedInventory(location, product, 100, 20);

        mockMvc.perform(get("/api/inventory/list")
                        .header("Authorization", "Bearer " + token)
                        .param("warehouseId", warehouse.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].locationCode").value("A-01-01-1"))
                .andExpect(jsonPath("$.data.content[0].productSkuCode").value("SKU-INV-1"))
                .andExpect(jsonPath("$.data.content[0].unitName").value("박스"))
                .andExpect(jsonPath("$.data.content[0].quantity").value(100))
                .andExpect(jsonPath("$.data.content[0].reservedQuantity").value(20));
    }

    @Test
    @DisplayName("재고를 조정하면 실재고가 갱신되고 이력이 남는다")
    void adjust_success() throws Exception {
        Company company = seedCompany("재고조정기업", "222-22-22222");
        seedUser(company, null, "inv-adjust-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-adjust-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE2");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT2");
        Warehouse warehouse = seedWarehouse(company, "재고조정창고");
        Location location = seedLocation(warehouse, "A-01-01-2");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-2");
        Inventory inventory = seedInventory(location, product, 50, 0);

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 80, "reason", "실사 결과 반영"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(80));

        mockMvc.perform(get("/api/inventory/history")
                        .header("Authorization", "Bearer " + token)
                        .param("warehouseId", warehouse.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].quantityChange").value(30))
                .andExpect(jsonPath("$.data.content[0].quantityAfter").value(80))
                .andExpect(jsonPath("$.data.content[0].targetType").value("ADJUSTMENT"));
    }

    @Test
    @DisplayName("조정 후 수량이 예약재고보다 적으면 400을 반환한다")
    void adjust_belowReserved_rejected() throws Exception {
        Company company = seedCompany("재고조정거부기업", "333-33-33333");
        seedUser(company, null, "inv-adjust-reject-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-adjust-reject-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE3");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT3");
        Warehouse warehouse = seedWarehouse(company, "재고조정거부창고");
        Location location = seedLocation(warehouse, "A-01-01-3");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-3");
        Inventory inventory = seedInventory(location, product, 50, 30);

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-key-2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 20, "reason", "실사 결과 반영"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY.getMessage()));
    }

    @Test
    @DisplayName("Idempotency-Key 헤더 없이 재고를 조정하면 400을 반환한다")
    void adjust_withoutIdempotencyKey_rejected() throws Exception {
        Company company = seedCompany("멱등키누락기업", "666-66-66666");
        seedUser(company, null, "inv-idem-missing-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-idem-missing-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE6");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT6");
        Warehouse warehouse = seedWarehouse(company, "멱등키누락창고");
        Location location = seedLocation(warehouse, "A-01-01-6");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-6");
        Inventory inventory = seedInventory(location, product, 50, 0);

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 80, "reason", "실사 결과 반영"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.IDEMPOTENCY_KEY_REQUIRED.getMessage()));
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 동일한 조정 요청을 반복하면 재처리 없이 같은 응답을 반환한다")
    void adjust_sameIdempotencyKeyAndBody_returnsCachedResponse() throws Exception {
        Company company = seedCompany("멱등재생기업", "777-77-77777");
        seedUser(company, null, "inv-idem-replay-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-idem-replay-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE7");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT7");
        Warehouse warehouse = seedWarehouse(company, "멱등재생창고");
        Location location = seedLocation(warehouse, "A-01-01-7");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-7");
        Inventory inventory = seedInventory(location, product, 50, 0);

        String body = objectMapper.writeValueAsString(Map.of("quantity", 80, "reason", "실사 결과 반영"));

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "replay-key-1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(80));

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "replay-key-1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(80));

        mockMvc.perform(get("/api/inventory/history")
                        .header("Authorization", "Bearer " + token)
                        .param("warehouseId", warehouse.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 다른 내용의 조정 요청을 보내면 409를 반환한다")
    void adjust_sameIdempotencyKeyDifferentBody_rejected() throws Exception {
        Company company = seedCompany("멱등불일치기업", "888-88-88888");
        seedUser(company, null, "inv-idem-mismatch-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-idem-mismatch-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE8");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT8");
        Warehouse warehouse = seedWarehouse(company, "멱등불일치창고");
        Location location = seedLocation(warehouse, "A-01-01-8");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-8");
        Inventory inventory = seedInventory(location, product, 50, 0);

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "mismatch-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 80, "reason", "실사 결과 반영"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inventory/" + inventory.getId() + "/adjust")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "mismatch-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 90, "reason", "다른 사유"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.IDEMPOTENCY_REQUEST_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("가용재고가 0 이하인 위치는 가용재고 조회 결과에서 제외된다")
    void availableLocations_excludesZeroAvailable() throws Exception {
        Company company = seedCompany("가용재고기업", "444-44-44444");
        seedUser(company, null, "inv-avail-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inv-avail-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE4");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT4");
        Warehouse warehouse = seedWarehouse(company, "가용재고창고");
        Location locationWithStock = seedLocation(warehouse, "A-01-01-4");
        Location locationFullyReserved = seedLocation(warehouse, "A-01-02-4");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-4");
        seedInventory(locationWithStock, product, 50, 10);
        seedInventory(locationFullyReserved, product, 20, 20);

        mockMvc.perform(get("/api/inventory/available-locations")
                        .header("Authorization", "Bearer " + token)
                        .param("warehouseId", warehouse.getId().toString())
                        .param("productId", product.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].locationCode").value("A-01-01-4"))
                .andExpect(jsonPath("$.data[0].availableQuantity").value(40));
    }

    @Test
    @DisplayName("창고관리자는 warehouseId 파라미터를 다른 창고로 보내도 자신의 담당 창고로만 스코프된다")
    void list_warehouseManager_ignoresQueryParam_scopedToOwnWarehouse() throws Exception {
        Company company = seedCompany("재고스코프기업", "555-55-55555");
        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "INV-STORE5");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "INV-CAT5");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "inv-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("inv-manager@test.com");

        Location otherLocation = seedLocation(otherWarehouse, "B-01-01-1");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-INV-5");
        seedInventory(otherLocation, product, 100, 0);

        mockMvc.perform(get("/api/inventory/list")
                        .header("Authorization", "Bearer " + token)
                        .param("warehouseId", otherWarehouse.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

}
