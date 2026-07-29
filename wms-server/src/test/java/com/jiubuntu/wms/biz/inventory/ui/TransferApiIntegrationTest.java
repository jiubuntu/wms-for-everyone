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
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryHistoryRepository;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.LocationStatus;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TransferApiIntegrationTest extends IntegrationTestSupport {

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

    private Map<String, Object> createBody(Long productId, Long fromLocationId, Long toLocationId, int quantity,
                                            Long reasonId) {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("fromLocationId", fromLocationId);
        body.put("toLocationId", toLocationId);
        body.put("lotNumber", null);
        body.put("quantity", quantity);
        body.put("reasonId", reasonId);
        body.put("note", "재고 재배치");
        return body;
    }

    @Test
    @DisplayName("이동을 등록하면 출발 위치 실재고가 줄고 도착 위치 실재고가 늘며 이력 2건이 남는다")
    void create_success() throws Exception {
        Company company = seedCompany("이동등록기업", "111-11-11111");
        seedUser(company, null, "transfer-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE1");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT1");
        CommonCode reason = seedCommonCode(CommonCodeGroup.TRANSFER_REASON, "TR-REASON1");
        Warehouse warehouse = seedWarehouse(company, "이동창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-1");
        Location toLocation = seedLocation(warehouse, "A-01-02-1");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-1");
        seedInventory(fromLocation, product, 100, 0);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-transfer-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 30, reason.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fromLocationCode").value("A-01-01-1"))
                .andExpect(jsonPath("$.data.toLocationCode").value("A-01-02-1"))
                .andExpect(jsonPath("$.data.quantity").value(30));

        Inventory fromInventory = inventoryRepository
                .findActiveByLocationAndProductAndLotNumber(fromLocation.getId(), product.getId(), null).orElseThrow();
        Inventory toInventory = inventoryRepository
                .findActiveByLocationAndProductAndLotNumber(toLocation.getId(), product.getId(), null).orElseThrow();
        assertThat(fromInventory.getQuantity()).isEqualTo(70);
        assertThat(toInventory.getQuantity()).isEqualTo(30);

        long historyCount = inventoryHistoryRepository.findAll().stream()
                .filter(h -> h.getTargetType() == InventoryHistoryTargetType.TRANSFER)
                .count();
        assertThat(historyCount).isEqualTo(2);
    }

    @Test
    @DisplayName("가용재고보다 많은 수량을 이동하려 하면 400을 반환한다")
    void create_insufficientQuantity_rejected() throws Exception {
        Company company = seedCompany("이동수량부족기업", "222-22-22222");
        seedUser(company, null, "transfer-insufficient@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-insufficient@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE2");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT2");
        Warehouse warehouse = seedWarehouse(company, "이동수량부족창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-2");
        Location toLocation = seedLocation(warehouse, "A-01-02-2");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-2");
        seedInventory(fromLocation, product, 10, 5);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-transfer-key-2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 30, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY.getMessage()));
    }

    @Test
    @DisplayName("출발 위치와 도착 위치가 같으면 400을 반환한다")
    void create_sameLocation_rejected() throws Exception {
        Company company = seedCompany("이동동일위치기업", "333-33-33333");
        seedUser(company, null, "transfer-same@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-same@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE3");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT3");
        Warehouse warehouse = seedWarehouse(company, "이동동일위치창고");
        Location location = seedLocation(warehouse, "A-01-01-3");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-3");
        seedInventory(location, product, 100, 0);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-transfer-key-3")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), location.getId(), location.getId(), 10, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.TRANSFER_SAME_LOCATION.getMessage()));
    }

    @Test
    @DisplayName("비활성화된 위치로는 이동할 수 없다")
    void create_disabledDestination_rejected() throws Exception {
        Company company = seedCompany("이동비활성기업", "444-44-44444");
        seedUser(company, null, "transfer-disabled@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-disabled@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE4");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT4");
        Warehouse warehouse = seedWarehouse(company, "이동비활성창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-4");
        Location toLocation = seedLocation(warehouse, "A-01-02-4");
        toLocation.update(storageType, LocationStatus.DISABLED);
        locationRepository.save(toLocation);
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-4");
        seedInventory(fromLocation, product, 100, 0);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-transfer-key-4")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 10, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.TRANSFER_LOCATION_DISABLED.getMessage()));
    }

    @Test
    @DisplayName("Idempotency-Key 헤더 없이 이동을 등록하면 400을 반환한다")
    void create_withoutIdempotencyKey_rejected() throws Exception {
        Company company = seedCompany("이동멱등키누락기업", "666-66-66666");
        seedUser(company, null, "transfer-idem-missing@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-idem-missing@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE6");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT6");
        Warehouse warehouse = seedWarehouse(company, "이동멱등키누락창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-6");
        Location toLocation = seedLocation(warehouse, "A-01-02-6");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-6");
        seedInventory(fromLocation, product, 100, 0);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 10, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.IDEMPOTENCY_KEY_REQUIRED.getMessage()));
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 동일한 이동 등록을 반복해도 재고는 한 번만 반영된다")
    void create_sameIdempotencyKeyAndBody_appliedOnce() throws Exception {
        Company company = seedCompany("이동멱등재생기업", "777-77-77777");
        seedUser(company, null, "transfer-idem-replay@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-idem-replay@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE7");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT7");
        Warehouse warehouse = seedWarehouse(company, "이동멱등재생창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-7");
        Location toLocation = seedLocation(warehouse, "A-01-02-7");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-7");
        seedInventory(fromLocation, product, 100, 0);

        String body = objectMapper.writeValueAsString(
                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 30, null));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "replay-transfer-key-1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "replay-transfer-key-1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());

        Inventory fromInventory = inventoryRepository
                .findActiveByLocationAndProductAndLotNumber(fromLocation.getId(), product.getId(), null).orElseThrow();
        assertThat(fromInventory.getQuantity()).isEqualTo(70);
    }

    @Test
    @DisplayName("이동 목록을 조회한다")
    void list_success() throws Exception {
        Company company = seedCompany("이동조회기업", "555-55-55555");
        seedUser(company, null, "transfer-list@test.com", UserRole.COMPANY_ADMIN);
        String token = login("transfer-list@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "TR-STORE5");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "TR-CAT5");
        Warehouse warehouse = seedWarehouse(company, "이동조회창고");
        Location fromLocation = seedLocation(warehouse, "A-01-01-5");
        Location toLocation = seedLocation(warehouse, "A-01-02-5");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-TR-5");
        seedInventory(fromLocation, product, 100, 0);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/transfer/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-transfer-key-5")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody(product.getId(), fromLocation.getId(), toLocation.getId(), 10, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/transfer/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].quantity").value(10));
    }

}
