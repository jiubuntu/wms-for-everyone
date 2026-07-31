package com.jiubuntu.wms.biz.warehouse.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class WarehouseApiIntegrationTest extends IntegrationTestSupport {

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

    @Test
    @DisplayName("기업관리자는 전역 선택기용 전체 목록에서 자사 창고를 전부 조회한다")
    void listAll_companyAdmin_returnsAllOwnCompanyWarehouses() throws Exception {
        Company company = seedCompany("전체조회기업", "999-11-11111");
        seedUser(company, null, "wh-all-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("wh-all-admin@test.com");

        seedWarehouse(company, "창고A");
        seedWarehouse(company, "창고B");

        mockMvc.perform(get("/api/warehouse/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("작업자가 전역 선택기용 전체 목록을 조회하면 담당 창고 1건만 내려온다")
    void listAll_worker_scopedToOwnWarehouse() throws Exception {
        Company company = seedCompany("작업자전체조회기업", "999-22-22222");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "wh-all-worker@test.com", UserRole.WORKER);
        String token = login("wh-all-worker@test.com");

        mockMvc.perform(get("/api/warehouse/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("담당창고"));
    }

    @Test
    @DisplayName("기업관리자는 자사 창고 목록을 조회한다")
    void list_returnsOwnCompanyWarehouses() throws Exception {
        Company company = seedCompany("목록조회기업", "111-11-11111");
        seedUser(company, null, "wh-list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("wh-list-admin@test.com");

        seedWarehouse(company, "본사창고");

        mockMvc.perform(get("/api/warehouse/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("본사창고"))
                .andExpect(jsonPath("$.data.content[0].locationCount").value(0));
    }

    @Test
    @DisplayName("창고관리자는 목록 조회 시 본인 담당 창고 1건만 조회된다")
    void list_warehouseManager_scopedToOwnWarehouse() throws Exception {
        Company company = seedCompany("창고관리자기업", "222-22-22222");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "wh-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("wh-manager@test.com");

        mockMvc.perform(get("/api/warehouse/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("담당창고"));
    }

    @Test
    @DisplayName("작업자는 창고 API에 접근할 수 없다")
    void list_deniedForWorker() throws Exception {
        Company company = seedCompany("작업자기업", "333-33-33333");
        seedUser(company, null, "wh-worker@test.com", UserRole.WORKER);
        String token = login("wh-worker@test.com");

        mockMvc.perform(get("/api/warehouse/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("창고관리자가 담당 창고가 아닌 창고를 상세 조회하면 거부된다")
    void get_warehouseManager_otherWarehouse_rejected() throws Exception {
        Company company = seedCompany("상세조회기업", "444-44-44444");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고2");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고2");
        seedUser(company, ownWarehouse, "wh-manager2@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("wh-manager2@test.com");

        mockMvc.perform(get("/api/warehouse/" + otherWarehouse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("기업관리자는 창고를 등록할 수 있다")
    void create_success() throws Exception {
        Company company = seedCompany("등록기업", "555-55-55555");
        seedUser(company, null, "wh-create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("wh-create-admin@test.com");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "신규창고");
        body.put("address", "인천시");

        mockMvc.perform(post("/api/warehouse/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("신규창고"))
                .andExpect(jsonPath("$.data.companyId").value(company.getId()));
    }

    @Test
    @DisplayName("창고관리자는 창고를 등록할 수 없다")
    void create_deniedForWarehouseManager() throws Exception {
        Company company = seedCompany("등록거부기업", "666-66-66666");
        Warehouse warehouse = seedWarehouse(company, "기존창고");
        seedUser(company, warehouse, "wh-create-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("wh-create-manager@test.com");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "신규창고2");

        mockMvc.perform(post("/api/warehouse/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 회사의 창고를 수정하려 하면 거부된다")
    void update_otherCompanyWarehouse_rejected() throws Exception {
        Company ownerCompany = seedCompany("창고소유기업", "777-77-77777");
        Company attackerCompany = seedCompany("공격기업3", "888-88-88888");
        seedUser(attackerCompany, null, "wh-attacker@test.com", UserRole.COMPANY_ADMIN);
        String token = login("wh-attacker@test.com");

        Warehouse warehouse = seedWarehouse(ownerCompany, "타사창고");

        Map<String, Object> body = new HashMap<>();
        body.put("name", "변경시도");

        mockMvc.perform(post("/api/warehouse/" + warehouse.getId() + "/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMPANY_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("담당자가 배정된 창고는 비활성화할 수 없다")
    void delete_withAssignedStaff_rejected() throws Exception {
        Company company = seedCompany("담당자배정기업", "121-21-21212");
        Warehouse warehouse = seedWarehouse(company, "담당자있는창고");
        seedUser(company, null, "wh-delete-admin@test.com", UserRole.COMPANY_ADMIN);
        seedUser(company, warehouse, "wh-delete-manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("wh-delete-admin@test.com");

        mockMvc.perform(post("/api/warehouse/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", warehouse.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_HAS_ASSIGNED_STAFF.getMessage()));
    }

    @Test
    @DisplayName("담당자가 없는 창고는 비활성화하면 소프트 삭제된다")
    void delete_withoutAssignedStaff_softDeletes() throws Exception {
        Company company = seedCompany("담당자없음기업", "131-31-31313");
        Warehouse warehouse = seedWarehouse(company, "담당자없는창고");
        seedUser(company, null, "wh-delete-admin2@test.com", UserRole.COMPANY_ADMIN);
        String token = login("wh-delete-admin2@test.com");

        mockMvc.perform(post("/api/warehouse/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", warehouse.getId()))))
                .andExpect(status().isOk());

        Optional<Warehouse> found = warehouseRepository.findActiveById(warehouse.getId());
        assertThat(found).isEmpty();
    }

}
