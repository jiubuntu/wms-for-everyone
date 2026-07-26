package com.jiubuntu.wms.biz.outbound.ui;

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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class OutboundApiIntegrationTest extends IntegrationTestSupport {

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

    private Warehouse seedWarehouse(Company company, CommonCode storageType, String name) {
        return warehouseRepository.save(new Warehouse(company, name, storageType, "서울시"));
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

    private Inventory seedInventory(Location location, Product product, String lotNumber, LocalDate expiryDate, int quantity) {
        Inventory inventory = new Inventory(location, product, lotNumber, null, expiryDate, quantity);
        return inventoryRepository.save(inventory);
    }

    private Map<String, Object> itemBody(Long productId, Long unitId, int quantity, String allocationType,
                                          List<Map<String, Object>> allocations) {
        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("unitId", unitId);
        item.put("quantity", quantity);
        item.put("allocationType", allocationType);
        item.put("allocations", allocations);
        return item;
    }

    private Map<String, Object> createBody(String customerName, List<Map<String, Object>> items) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerName", customerName);
        body.put("note", "테스트 출고");
        body.put("items", items);
        return body;
    }

    @Test
    @DisplayName("FEFO 출고를 등록하면 유효기간이 임박한 위치부터 예약재고에 반영된다")
    void create_fefo_success() throws Exception {
        Company company = seedCompany("출고FEFO기업", "111-11-11111");
        seedUser(company, null, "outbound-fefo-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-fefo-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE1");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT1");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고FEFO창고");
        Location earlyLocation = seedLocation(warehouse, "A-01-01-1");
        Location lateLocation = seedLocation(warehouse, "A-01-02-1");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-1");
        seedInventory(earlyLocation, product, "LOT-EARLY", LocalDate.of(2026, 1, 1), 20);
        seedInventory(lateLocation, product, "LOT-LATE", LocalDate.of(2026, 6, 1), 20);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 30, "FEFO", List.of())));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-fefo-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].allocations.length()").value(2));

        Inventory early = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                earlyLocation.getId(), product.getId(), "LOT-EARLY").orElseThrow();
        Inventory late = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                lateLocation.getId(), product.getId(), "LOT-LATE").orElseThrow();
        assertThat(early.getReservedQuantity()).isEqualTo(20);
        assertThat(late.getReservedQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("MANUAL 출고를 등록하면 지정한 위치의 예약재고만 정확히 반영된다")
    void create_manual_success() throws Exception {
        Company company = seedCompany("출고MANUAL기업", "222-22-22222");
        seedUser(company, null, "outbound-manual-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-manual-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE2");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT2");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고MANUAL창고");
        Location location = seedLocation(warehouse, "A-01-01-2");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-2");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> allocation = new HashMap<>();
        allocation.put("locationId", location.getId());
        allocation.put("lotNumber", "LOT-1");
        allocation.put("quantity", 15);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 15, "MANUAL", List.of(allocation))));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-manual-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items[0].allocations[0].locationCode").value("A-01-01-2"))
                .andExpect(jsonPath("$.data.items[0].allocations[0].quantity").value(15));

        Inventory inventory = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                location.getId(), product.getId(), "LOT-1").orElseThrow();
        assertThat(inventory.getReservedQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("MANUAL 할당 합계가 출고 수량과 다르면 등록이 거부된다")
    void create_manualSumMismatch_rejected() throws Exception {
        Company company = seedCompany("출고합계불일치기업", "333-33-33333");
        seedUser(company, null, "outbound-mismatch-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-mismatch-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE3");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT3");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고합계불일치창고");
        Location location = seedLocation(warehouse, "A-01-01-3");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-3");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> allocation = new HashMap<>();
        allocation.put("locationId", location.getId());
        allocation.put("lotNumber", "LOT-1");
        allocation.put("quantity", 10);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 15, "MANUAL", List.of(allocation))));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-mismatch-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.OUTBOUND_ALLOCATION_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("가용재고가 부족하면 등록 전체가 실패하고 예약재고도 반영되지 않는다")
    void create_insufficientQuantity_rejected() throws Exception {
        Company company = seedCompany("출고재고부족기업", "444-44-44444");
        seedUser(company, null, "outbound-insufficient-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-insufficient-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE4");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT4");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고재고부족창고");
        Location location = seedLocation(warehouse, "A-01-01-4");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-4");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 10);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 30, "FEFO", List.of())));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-insufficient-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY.getMessage()));

        Inventory inventory = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                location.getId(), product.getId(), "LOT-1").orElseThrow();
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("출하를 확정하면 예약재고가 실재고 차감으로 전환되고 이력이 남는다")
    void complete_success() throws Exception {
        Company company = seedCompany("출고확정기업", "555-55-55555");
        seedUser(company, null, "outbound-complete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-complete-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE5");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT5");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고확정창고");
        Location location = seedLocation(warehouse, "A-01-01-5");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-5");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-complete-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long outboundId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/" + outboundId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-complete-key-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.processedByName").value("홍길동"));

        Inventory inventory = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                location.getId(), product.getId(), "LOT-1").orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(30);
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);

        long outboundHistoryCount = inventoryHistoryRepository.findAll().stream()
                .filter(h -> h.getTargetType() == InventoryHistoryTargetType.OUTBOUND)
                .count();
        assertThat(outboundHistoryCount).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 확정된 출고를 다시 확정하면 400을 반환한다")
    void complete_alreadyCompleted_rejected() throws Exception {
        Company company = seedCompany("출고재확정기업", "666-66-66666");
        seedUser(company, null, "outbound-recomplete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-recomplete-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE6");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT6");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고재확정창고");
        Location location = seedLocation(warehouse, "A-01-01-6");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-6");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-recomplete-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long outboundId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/" + outboundId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-recomplete-key-2"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/" + outboundId + "/complete")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-recomplete-key-3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.OUTBOUND_ALREADY_COMPLETED.getMessage()));
    }

    @Test
    @DisplayName("취소하면 예약재고만 해제되고 실재고와 이력은 변하지 않는다")
    void cancel_success() throws Exception {
        Company company = seedCompany("출고취소기업", "777-77-77777");
        seedUser(company, null, "outbound-cancel-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-cancel-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE7");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT7");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고취소창고");
        Location location = seedLocation(warehouse, "A-01-01-7");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-7");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-cancel-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long outboundId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/" + outboundId + "/cancel")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-cancel-key-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        Inventory inventory = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                location.getId(), product.getId(), "LOT-1").orElseThrow();
        assertThat(inventory.getQuantity()).isEqualTo(50);
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);

        long outboundHistoryCount = inventoryHistoryRepository.findAll().stream()
                .filter(h -> h.getTargetType() == InventoryHistoryTargetType.OUTBOUND)
                .count();
        assertThat(outboundHistoryCount).isEqualTo(0);
    }

    @Test
    @DisplayName("출고 목록/상세를 조회한다")
    void list_and_detail_success() throws Exception {
        Company company = seedCompany("출고조회기업", "888-88-88888");
        seedUser(company, null, "outbound-list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-list-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE8");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT8");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고조회창고");
        Location location = seedLocation(warehouse, "A-01-01-8");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-8");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));

        MvcResult createResult = mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-list-key-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long outboundId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/outbound/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].itemCount").value(1))
                .andExpect(jsonPath("$.data.content[0].customerName").value("㈜모두상사"));

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/outbound/" + outboundId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].productSkuCode").value("SKU-OUT-8"))
                .andExpect(jsonPath("$.data.items[0].allocations[0].quantity").value(20));
    }

    @Test
    @DisplayName("Idempotency-Key 헤더 없이 출고를 등록하면 400을 반환한다")
    void create_withoutIdempotencyKey_rejected() throws Exception {
        Company company = seedCompany("출고멱등키누락기업", "999-99-99999");
        seedUser(company, null, "outbound-idem-missing-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-idem-missing-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE9");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT9");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고멱등키누락창고");
        Location location = seedLocation(warehouse, "A-01-01-9");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-9");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.IDEMPOTENCY_KEY_REQUIRED.getMessage()));
    }

    @Test
    @DisplayName("같은 Idempotency-Key로 동일한 등록 요청을 반복해도 출고는 한 건만 생성된다")
    void create_sameIdempotencyKeyAndBody_createsOnce() throws Exception {
        Company company = seedCompany("출고멱등재생기업", "121-21-21212");
        seedUser(company, null, "outbound-idem-replay-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("outbound-idem-replay-admin@test.com");

        CommonCode storageType = seedCommonCode(CommonCodeGroup.STORAGE_TYPE, "OUT-STORE10");
        CommonCode category = seedCommonCode(CommonCodeGroup.PRODUCT_CATEGORY, "OUT-CAT10");
        Warehouse warehouse = seedWarehouse(company, storageType, "출고멱등재생창고");
        Location location = seedLocation(warehouse, "A-01-01-10");
        ProductUnit unit = seedProductUnit(company, "박스");
        Product product = seedProduct(company, category, storageType, unit, "SKU-OUT-10");
        seedInventory(location, product, "LOT-1", LocalDate.of(2026, 1, 1), 50);

        Map<String, Object> body = createBody("㈜모두상사", List.of(
                itemBody(product.getId(), unit.getId(), 20, "FEFO", List.of())));
        String content = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-replay-key-1")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/outbound/create")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "outbound-replay-key-1")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/outbound/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        Inventory inventory = inventoryRepository.findActiveByLocationAndProductAndLotNumber(
                location.getId(), product.getId(), "LOT-1").orElseThrow();
        assertThat(inventory.getReservedQuantity()).isEqualTo(20);
    }

}
