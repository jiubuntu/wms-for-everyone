package com.jiubuntu.wms.biz.user.ui;

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
import com.fasterxml.jackson.databind.JsonNode;
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
class UserApiIntegrationTest extends IntegrationTestSupport {

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

    private Warehouse seedWarehouse(Company company, String name) {
        return warehouseRepository.save(new Warehouse(company, name, "서울시"));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("accessToken").asText();
    }

    private JsonNode loginBody(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private Map<String, Object> issueBody(String email, String name, String phone, String role, Long warehouseId) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("name", name);
        body.put("phone", phone);
        body.put("role", role);
        body.put("warehouseId", warehouseId);
        return body;
    }

    @Test
    @DisplayName("기업관리자가 계정을 발급하면 비밀번호 없이 요청하고, 응답으로 1회성 임시 비밀번호를 받는다")
    void create_success_byCompanyAdmin() throws Exception {
        Company company = seedCompany("계정발급기업", "111-11-11111");
        Warehouse warehouse = seedWarehouse(company, "본사창고");
        seedUser(company, null, "issuer1@test.com", UserRole.COMPANY_ADMIN);
        String token = login("issuer1@test.com", "password1!");

        MvcResult result = mockMvc.perform(post("/api/user/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                issueBody("worker1@test.com", "김작업", "010-1111-1111", "WORKER", warehouse.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("worker1@test.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.temporaryPassword").isNotEmpty())
                .andReturn();

        String temporaryPassword = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("temporaryPassword").asText();

        User created = userRepository.findActiveByEmail("worker1@test.com").orElseThrow();
        assertThat(created.isMustChangePassword()).isTrue();
        assertThat(passwordEncoder.matches(temporaryPassword, created.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이미 가입된 이메일로 발급을 시도하면 409를 반환한다")
    void create_duplicateEmail_rejected() throws Exception {
        Company company = seedCompany("계정중복기업", "222-22-22222");
        Warehouse warehouse = seedWarehouse(company, "본사창고");
        seedUser(company, null, "issuer2@test.com", UserRole.COMPANY_ADMIN);
        seedUser(company, warehouse, "existing@test.com", UserRole.WORKER);
        String token = login("issuer2@test.com", "password1!");

        mockMvc.perform(post("/api/user/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                issueBody("existing@test.com", "김작업", "010-1111-1111", "WORKER", warehouse.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("창고관리자가 담당 창고가 아닌 창고에 발급을 시도하면 거부된다")
    void create_byWarehouseManager_otherWarehouse_rejected() throws Exception {
        Company company = seedCompany("창고스코프기업", "333-33-33333");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "manager1@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("manager1@test.com", "password1!");

        mockMvc.perform(post("/api/user/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                issueBody("worker3@test.com", "김작업", "010-1111-1111", "WORKER", otherWarehouse.getId()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.WAREHOUSE_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("기업관리자는 직원 목록에서 전사 직원을 전부 조회하고, 관리자 본인은 목록에 나오지 않는다")
    void list_companyAdmin_returnsAllStaffExcludingAdmin() throws Exception {
        Company company = seedCompany("목록조회기업", "444-44-44444");
        Warehouse warehouse = seedWarehouse(company, "본사창고");
        seedUser(company, null, "admin4@test.com", UserRole.COMPANY_ADMIN);
        seedUser(company, warehouse, "manager4@test.com", UserRole.WAREHOUSE_MANAGER);
        seedUser(company, warehouse, "worker4@test.com", UserRole.WORKER);
        String token = login("admin4@test.com", "password1!");

        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[*].email")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("manager4@test.com", "worker4@test.com")));
    }

    @Test
    @DisplayName("창고관리자는 직원 목록 조회 시 담당 창고 소속만 조회되고, 다른 창고 소속은 제외된다")
    void list_warehouseManager_scopedToOwnWarehouse() throws Exception {
        Company company = seedCompany("창고목록기업", "555-55-55555");
        Warehouse ownWarehouse = seedWarehouse(company, "담당창고");
        Warehouse otherWarehouse = seedWarehouse(company, "다른창고");
        seedUser(company, ownWarehouse, "manager5@test.com", UserRole.WAREHOUSE_MANAGER);
        seedUser(company, ownWarehouse, "worker5-own@test.com", UserRole.WORKER);
        seedUser(company, otherWarehouse, "worker5-other@test.com", UserRole.WORKER);
        String token = login("manager5@test.com", "password1!");

        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[*].email")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("manager5@test.com", "worker5-own@test.com")))
                .andExpect(jsonPath("$.data.content[*].warehouseName")
                        .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("담당창고"))));
    }

    @Test
    @DisplayName("탈퇴 처리하면 대상 계정이 비활성화된다")
    void withdraw_success() throws Exception {
        Company company = seedCompany("탈퇴기업", "666-66-66666");
        Warehouse warehouse = seedWarehouse(company, "본사창고");
        seedUser(company, null, "admin6@test.com", UserRole.COMPANY_ADMIN);
        User worker = seedUser(company, warehouse, "worker6@test.com", UserRole.WORKER);
        String token = login("admin6@test.com", "password1!");

        mockMvc.perform(post("/api/user/" + worker.getId() + "/withdraw")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("발급받은 임시 비밀번호로 로그인하면 mustChangePassword가 true이고, 비밀번호 변경 후에는 false가 된다")
    void login_mustChangePassword_clearsAfterPasswordChange() throws Exception {
        Company company = seedCompany("최초로그인기업", "777-77-77777");
        Warehouse warehouse = seedWarehouse(company, "본사창고");
        seedUser(company, null, "admin7@test.com", UserRole.COMPANY_ADMIN);
        String adminToken = login("admin7@test.com", "password1!");

        MvcResult createResult = mockMvc.perform(post("/api/user/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                issueBody("worker7@test.com", "김작업", "010-1111-1111", "WORKER", warehouse.getId()))))
                .andExpect(status().isCreated())
                .andReturn();
        String temporaryPassword = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("temporaryPassword").asText();

        JsonNode firstLogin = loginBody("worker7@test.com", temporaryPassword);
        assertThat(firstLogin.path("mustChangePassword").asBoolean()).isTrue();
        String workerToken = firstLogin.path("accessToken").asText();

        mockMvc.perform(post("/api/user/change-password")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("currentPassword", temporaryPassword, "newPassword", "newPassword1!"))))
                .andExpect(status().isOk());

        JsonNode secondLogin = loginBody("worker7@test.com", "newPassword1!");
        assertThat(secondLogin.path("mustChangePassword").asBoolean()).isFalse();
    }

}
