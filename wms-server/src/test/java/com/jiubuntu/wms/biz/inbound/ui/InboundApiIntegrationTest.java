package com.jiubuntu.wms.biz.inbound.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 이 도메인의 확정(complete) API는 낙관적 락 재시도를 위해 PROPAGATION_REQUIRES_NEW로 완전히 새 트랜잭션을 연다.
// 테스트에 @Transactional을 걸면 그 새 트랜잭션이 아직 커밋 안 된 테스트 트랜잭션의 데이터를 못 봐서 실패하므로,
// 이 클래스는 다른 통합 테스트와 달리 @Transactional을 걸지 않는다 (시드 데이터는 테스트마다 고유값 사용).
class InboundApiIntegrationTest extends IntegrationTestSupport {

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

    private Map<String, Object> itemBody(Long productId, Long unitId, int quantity, String lotNumber) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("unitId", unitId);
        item.put("quantity", quantity);
        item.put("lotNumber", lotNumber);
        item.put("manufactureDate", lotNumber != null ? "2026-01-01" : null);
        item.put("expiryDate", lotNumber != null ? "2026-06-01" : null);
        return item;
    }

    private Map<String, Object> createBody(String supplierName, List<Map<String, Object>> items) {
        Map<String, Object> body = new HashMap<>();
        body.put("supplierName", supplierName);
        body.put("note", "테스트 입고");
        body.put("items", items);
        return body;
    }

    private Map<String, Object> locationsBody(List<Map<String, Object>> locations) {
        Map<String, Object> body = new HashMap<>();
        body.put("locations", locations);
        return body;
    }

    private Map<String, Object> locationEntry(Long locationId, int quantity) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("locationId", locationId);
        entry.put("quantity", quantity);
        return entry;
    }

    @Test
    @DisplayName("입고를 등록하면 PENDING 상태로 생성되고 위치는 아직 없다")
    void create_success() throws Exception {
        Company company = seedCompany("입고등록기업", "111-11-11111");
        seedUser(company, null, "inbound-create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-create-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE1");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT1");
        Warehouse warehouse = seedWarehouse(company, "입고등록창고");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-1");

        Map<String, Object> body = createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 100, null)));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-create-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].locations.length()").value(0));
    }

    @Test
    @DisplayName("LOT 관리 상품인데 LOT 정보 없이 등록하면 400을 반환한다")
    void create_lotTrackingMissingInfo_rejected() throws Exception {
        Company company = seedCompany("입고LOT누락기업", "222-22-22222");
        seedUser(company, null, "inbound-lot-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-lot-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE2");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT2");
        Warehouse warehouse = seedWarehouse(company, "입고LOT누락창고");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product lotProduct = productRepository.save(new Product(
                company, "SKU-IN-2", "LOT관리상품", category, storageType, unit, null, null, true, null));

        Map<String, Object> body = createBody("㈜모두공급", List.of(itemBody(lotProduct.getId(), unit.getId(), 100, null)));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-lot-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INBOUND_LOT_INFO_REQUIRED.getMessage()));
    }

    @Test
    @DisplayName("위치를 하나 배치하면 PENDING에서 IN_PROGRESS로 자동 전환된다")
    void replaceLocations_singleLocation_autoTransitionsToInProgress() throws Exception {
        Company company = seedCompany("입고단일배치기업", "333-33-33333");
        seedUser(company, null, "inbound-single-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-single-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE3");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT3");
        Warehouse warehouse = seedWarehouse(company, "입고단일배치창고");
        Location location = seedLocation(warehouse, "A-01-01-3");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-3");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-single-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        var root = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long inboundId = root.path("id").asLong();
        long itemId = root.path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/items/" + itemId + "/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(locationsBody(List.of(locationEntry(location.getId(), 50))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.items[0].locations.length()").value(1));
    }

    @Test
    @DisplayName("위치 배치 합계가 상품 라인 수량과 다르면 400을 반환한다")
    void replaceLocations_sumMismatch_rejected() throws Exception {
        Company company = seedCompany("입고배치불일치기업", "444-44-44444");
        seedUser(company, null, "inbound-mismatch-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-mismatch-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE4");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT4");
        Warehouse warehouse = seedWarehouse(company, "입고배치불일치창고");
        Location location = seedLocation(warehouse, "A-01-01-4");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-4");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-mismatch-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        var root = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long inboundId = root.path("id").asLong();
        long itemId = root.path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/items/" + itemId + "/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(locationsBody(List.of(locationEntry(location.getId(), 30))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INBOUND_LOCATION_SUM_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("여러 위치에 나눠 배치하고 확정하면 위치별로 실재고가 반영되고 이력이 남는다")
    void completeFlow_splitAcrossLocations_success() throws Exception {
        Company company = seedCompany("입고확정기업", "555-55-55555");
        seedUser(company, null, "inbound-complete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-complete-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE5");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT5");
        Warehouse warehouse = seedWarehouse(company, "입고확정창고");
        Location locationA = seedLocation(warehouse, "A-01-01-5");
        Location locationB = seedLocation(warehouse, "A-01-02-5");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-5");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-complete-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 100, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        var root = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long inboundId = root.path("id").asLong();
        long itemId = root.path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/items/" + itemId + "/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(locationsBody(List.of(
                                locationEntry(locationA.getId(), 60), locationEntry(locationB.getId(), 40))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].locations.length()").value(2));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-complete-key-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.processedByName").value("홍길동"));

        var inventoryA = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                locationA.getId(), product.getId(), null).orElseThrow();
        var inventoryB = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                locationB.getId(), product.getId(), null).orElseThrow();
        assertThat(inventoryA.getQuantity()).isEqualTo(60);
        assertThat(inventoryB.getQuantity()).isEqualTo(40);

        long inboundHistoryCount = inventoryHistoryRepository.findAll().stream()
                .filter(h -> h.getTargetType() == InventoryHistoryTargetType.INBOUND)
                .count();
        assertThat(inboundHistoryCount).isEqualTo(2);
    }

    @Test
    @DisplayName("배치가 끝나지 않은 상품 라인이 있으면 확정이 거부된다")
    void complete_notFullyPlaced_rejected() throws Exception {
        Company company = seedCompany("입고미배치기업", "666-66-66666");
        seedUser(company, null, "inbound-notplaced-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-notplaced-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE6");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT6");
        Warehouse warehouse = seedWarehouse(company, "입고미배치창고");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-6");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-notplaced-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        long inboundId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-notplaced-key-2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INBOUND_NOT_FULLY_PLACED.getMessage()));
    }

    @Test
    @DisplayName("취소하면 재고는 반영되지 않고 상태만 CANCELLED로 바뀐다")
    void cancel_success() throws Exception {
        Company company = seedCompany("입고취소기업", "777-77-77777");
        seedUser(company, null, "inbound-cancel-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-cancel-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE7");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT7");
        Warehouse warehouse = seedWarehouse(company, "입고취소창고");
        Location location = seedLocation(warehouse, "A-01-01-7");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-7");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-cancel-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        var root = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long inboundId = root.path("id").asLong();
        long itemId = root.path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/items/" + itemId + "/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(locationsBody(List.of(locationEntry(location.getId(), 50))))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/cancel")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-cancel-key-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertThat(inventoryRepository.findActiveByLocationAndProductAndLotNumber(location.getId(), product.getId(), null))
                .isEmpty();
        long inboundHistoryCount = inventoryHistoryRepository.findAll().stream()
                .filter(h -> h.getTargetType() == InventoryHistoryTargetType.INBOUND)
                .count();
        assertThat(inboundHistoryCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Idempotency-Key 헤더 없이 등록하면 400을 반환하지만, 위치 배치는 헤더 없이도 성공한다")
    void idempotency_createRequiresKey_locationsDoNotRequireKey() throws Exception {
        Company company = seedCompany("입고멱등기업", "888-88-88888");
        seedUser(company, null, "inbound-idem-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-idem-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE8");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT8");
        Warehouse warehouse = seedWarehouse(company, "입고멱등창고");
        Location location = seedLocation(warehouse, "A-01-01-8");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-8");

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.IDEMPOTENCY_KEY_REQUIRED.getMessage()));

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-idem-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        var root = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data");
        long inboundId = root.path("id").asLong();
        long itemId = root.path("items").get(0).path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId + "/items/" + itemId + "/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(locationsBody(List.of(locationEntry(location.getId(), 50))))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("입고 목록/상세를 조회한다")
    void list_and_detail_success() throws Exception {
        Company company = seedCompany("입고조회기업", "999-99-99999");
        seedUser(company, null, "inbound-list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("inbound-list-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "IN-STORE9");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "IN-CAT9");
        Warehouse warehouse = seedWarehouse(company, "입고조회창고");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-IN-9");

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/inbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "inbound-list-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                createBody("㈜모두공급", List.of(itemBody(product.getId(), unit.getId(), 50, null))))))
                .andExpect(status().isCreated())
                .andReturn();
        long inboundId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/inbound/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].itemCount").value(1))
                .andExpect(jsonPath("$.data.content[0].supplierName").value("㈜모두공급"));

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/inbound/" + inboundId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].productSkuCode").value("SKU-IN-9"));
    }

}
