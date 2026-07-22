package com.jiubuntu.wms.biz.productunit.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.ProductUnitRepository;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
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
class ProductUnitApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company seedCompany(String name, String businessNumber) {
        return companyRepository.save(new Company(name, businessNumber, CompanyStatus.ACTIVE));
    }

    private void seedUser(Company company, String email, UserRole role) {
        userRepository.save(new User(company, null, email, passwordEncoder.encode("password1!"),
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

    @Test
    @DisplayName("기업관리자는 자사 상품 단위 목록을 조회한다")
    void list_returnsOwnCompanyUnits() throws Exception {
        Company company = seedCompany("목록조회기업", "111-11-11111");
        seedUser(company, "list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("list-admin@test.com");

        Company otherCompany = seedCompany("다른회사", "999-11-11111");
        productUnitRepository.save(new ProductUnit(company, "박스"));
        productUnitRepository.save(new ProductUnit(otherCompany, "kg"));

        mockMvc.perform(get("/api/product-unit/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("박스"));
    }

    @Test
    @DisplayName("전체 목록 조회(/all)는 페이지네이션 없이 자사 단위만 반환한다")
    void listAll_returnsOwnCompanyUnits() throws Exception {
        Company company = seedCompany("전체목록조회기업", "161-61-61616");
        seedUser(company, "list-all-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("list-all-admin@test.com");

        Company otherCompany = seedCompany("다른회사2", "171-71-71717");
        productUnitRepository.save(new ProductUnit(company, "박스2"));
        productUnitRepository.save(new ProductUnit(otherCompany, "kg2"));

        mockMvc.perform(get("/api/product-unit/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("박스2"));
    }

    @Test
    @DisplayName("창고관리자도 상품 단위 API에 접근할 수 있다")
    void list_allowedForWarehouseManager() throws Exception {
        Company company = seedCompany("창고관리자기업", "222-22-22222");
        seedUser(company, "manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("manager@test.com");

        mockMvc.perform(get("/api/product-unit/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("작업자는 상품 단위 API에 접근할 수 없다")
    void list_deniedForWorker() throws Exception {
        Company company = seedCompany("작업자기업", "333-33-33333");
        seedUser(company, "worker@test.com", UserRole.WORKER);
        String token = login("worker@test.com");

        mockMvc.perform(get("/api/product-unit/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품 단위를 등록할 수 있다")
    void create_success() throws Exception {
        Company company = seedCompany("등록기업", "444-44-44444");
        seedUser(company, "create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("create-admin@test.com");

        mockMvc.perform(post("/api/product-unit/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "박스"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyId").value(company.getId()))
                .andExpect(jsonPath("$.data.name").value("박스"));
    }

    @Test
    @DisplayName("같은 회사에 이미 등록된 단위명이면 409를 반환한다")
    void create_duplicateName_rejected() throws Exception {
        Company company = seedCompany("중복등록기업", "555-55-55555");
        seedUser(company, "dup-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("dup-admin@test.com");
        productUnitRepository.save(new ProductUnit(company, "박스"));

        mockMvc.perform(post("/api/product-unit/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "박스"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_UNIT_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("다른 회사의 단위를 수정하려 하면 거부된다")
    void update_otherCompanyUnit_rejected() throws Exception {
        Company ownerCompany = seedCompany("단위소유기업", "666-66-66666");
        Company attackerCompany = seedCompany("공격기업", "777-77-77777");
        seedUser(attackerCompany, "attacker-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("attacker-admin@test.com");

        ProductUnit ownerUnit = productUnitRepository.save(new ProductUnit(ownerCompany, "박스"));

        mockMvc.perform(post("/api/product-unit/" + ownerUnit.getId() + "/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "변경시도"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMPANY_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("자사 단위를 삭제하면 소프트 삭제되어 목록에서 사라진다")
    void delete_ownUnit_softDeletes() throws Exception {
        Company company = seedCompany("삭제테스트기업", "888-88-88888");
        seedUser(company, "delete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("delete-admin@test.com");

        ProductUnit unit = productUnitRepository.save(new ProductUnit(company, "삭제될단위"));

        mockMvc.perform(post("/api/product-unit/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", unit.getId()))))
                .andExpect(status().isOk());

        Optional<ProductUnit> found = productUnitRepository.findActiveById(unit.getId());
        assertThat(found).isEmpty();
    }

}
