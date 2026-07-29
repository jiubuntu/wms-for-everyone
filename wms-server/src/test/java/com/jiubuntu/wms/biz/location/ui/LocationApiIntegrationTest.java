package com.jiubuntu.wms.biz.location.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class LocationApiIntegrationTest extends IntegrationTestSupport {

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

    private Map<String, Object> bulkCreateBody(String zone, int rowFrom, int rowTo, int colFrom, int colTo, int levelCount) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("zone", zone);
        body.put("rowFrom", rowFrom);
        body.put("rowTo", rowTo);
        body.put("colFrom", colFrom);
        body.put("colTo", colTo);
        body.put("levelCount", levelCount);
        return body;
    }

    @Test
    @DisplayName("행 2개 열 3개 2단을 입력하면 위치 12개가 일괄 생성된다")
    void bulkCreate_success() throws Exception {
        Company company = seedCompany("위치생성기업", "111-11-11111");
        seedUser(company, null, "loc-create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("loc-create-admin@test.com");

        Warehouse warehouse = seedWarehouse(company, "생성창고");

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/location/bulk-create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bulkCreateBody("A", 1, 2, 1, 3, 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(12))
                .andExpect(jsonPath("$.data[0].code").value("A-01-01-1"));
    }

    @Test
    @DisplayName("이미 존재하는 위치 코드와 겹치면 409를 반환한다")
    void bulkCreate_duplicateCode_rejected() throws Exception {
        Company company = seedCompany("위치중복기업", "222-22-22222");
        seedUser(company, null, "loc-dup-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("loc-dup-admin@test.com");

        Warehouse warehouse = seedWarehouse(company, "중복창고");
        locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/location/bulk-create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bulkCreateBody("A", 1, 1, 1, 1, 1))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.LOCATION_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("위치 목록을 조회한다")
    void list_success() throws Exception {
        Company company = seedCompany("위치조회기업", "333-33-33333");
        seedUser(company, null, "loc-list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("loc-list-admin@test.com");

        Warehouse warehouse = seedWarehouse(company, "조회창고");
        locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build());

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/location/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("A-01-01-1"));
    }

    @Test
    @DisplayName("위치를 수정하면 상태가 갱신된다")
    void update_success() throws Exception {
        Company company = seedCompany("위치수정기업", "444-44-44444");
        seedUser(company, null, "loc-update-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("loc-update-admin@test.com");

        Warehouse warehouse = seedWarehouse(company, "수정창고");
        Location location = locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/location/" + location.getId() + "/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("status", "DISABLED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    @DisplayName("위치를 삭제하면 소프트 삭제된다")
    void delete_success() throws Exception {
        Company company = seedCompany("위치삭제기업", "555-55-55555");
        seedUser(company, null, "loc-delete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("loc-delete-admin@test.com");

        Warehouse warehouse = seedWarehouse(company, "삭제창고");
        Location location = locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build());

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/location/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", location.getId()))))
                .andExpect(status().isOk());

        Optional<Location> found = locationRepository.findActiveById(location.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("창고관리자가 담당 창고가 아닌 창고의 위치에 접근하면 거부된다")
    void list_warehouseManager_otherWarehouse_rejected() throws Exception {
        Company company = seedCompany("위치스코프기업", "666-66-66666");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고3");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고3");
        seedUser(company, ownWarehouse, "loc-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("loc-manager@test.com");

        mockMvc.perform(get("/api/warehouse/" + otherWarehouse.getId() + "/location/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("작업자도 담당 창고의 위치 전체 목록은 조회할 수 있다 (입출고/재고이동 등록 화면의 위치 선택용)")
    void listAll_allowedForWorker() throws Exception {
        Company company = seedCompany("위치작업자전체조회기업", "777-77-77777");
        Warehouse warehouse = seedWarehouse(company, "작업자전체조회창고");
        seedUser(company, warehouse, "loc-worker-all@test.com", UserRole.WORKER);
        String token = login("loc-worker-all@test.com");

        locationRepository.save(Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build());

        mockMvc.perform(get("/api/warehouse/" + warehouse.getId() + "/location/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("A-01-01-1"));
    }

    @Test
    @DisplayName("작업자가 담당 창고가 아닌 창고의 위치 전체 목록을 조회하면 거부된다")
    void listAll_worker_otherWarehouse_rejected() throws Exception {
        Company company = seedCompany("위치작업자스코프기업", "888-88-88888");
        Warehouse ownWarehouse = seedWarehouse(company, "작업자담당창고");
        Warehouse otherWarehouse = seedWarehouse(company, "작업자다른창고");
        seedUser(company, ownWarehouse, "loc-worker-scope@test.com", UserRole.WORKER);
        String token = login("loc-worker-scope@test.com");

        mockMvc.perform(get("/api/warehouse/" + otherWarehouse.getId() + "/location/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_SCOPE_VIOLATION.getMessage()));
    }

}
