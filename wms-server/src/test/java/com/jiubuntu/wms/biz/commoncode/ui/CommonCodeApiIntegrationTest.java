package com.jiubuntu.wms.biz.commoncode.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
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
class CommonCodeApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company seedCompany(String name, String businessNumber) {
        return companyRepository.save(new Company(name, businessNumber, CompanyStatus.ACTIVE));
    }

    private User seedUser(Company company, String email, UserRole role) {
        User user = new User(company, null, email, passwordEncoder.encode("password1!"),
                "홍길동", "010-0000-0000", role, UserStatus.ACTIVE);
        return userRepository.save(user);
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
    @DisplayName("기업관리자는 시스템 코드와 자사 커스텀 코드를 함께 조회한다")
    void list_returnsSystemAndOwnCompanyCodes() throws Exception {
        Company company = seedCompany("목록조회기업", "111-11-11111");
        seedUser(company, "list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("list-admin@test.com");

        commonCodeRepository.save(new CommonCode(null, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1));
        commonCodeRepository.save(new CommonCode(company, CommonCodeGroup.PRODUCT_CATEGORY, "CUSTOM_CAT", "우리회사카테고리", 2));

        mockMvc.perform(get("/api/common-code/list")
                        .header("Authorization", "Bearer " + token)
                        .param("groupCode", "PRODUCT_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("창고관리자는 공통 코드 API를 기업관리자와 동일하게 조회할 수 있다")
    void list_allowedForWarehouseManager() throws Exception {
        Company company = seedCompany("창고관리자권한기업", "222-22-22222");
        seedUser(company, "manager@test.com", UserRole.WAREHOUSE_MANAGER);
        String managerToken = login("manager@test.com");

        mockMvc.perform(get("/api/common-code/list")
                        .header("Authorization", "Bearer " + managerToken)
                        .param("groupCode", "PRODUCT_CATEGORY"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("작업자는 공통 코드 API에 접근할 수 없다")
    void list_deniedForWorker() throws Exception {
        Company company = seedCompany("작업자권한테스트기업", "232-32-32323");
        seedUser(company, "worker@test.com", UserRole.WORKER);
        String workerToken = login("worker@test.com");

        mockMvc.perform(get("/api/common-code/list")
                        .header("Authorization", "Bearer " + workerToken)
                        .param("groupCode", "PRODUCT_CATEGORY"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("커스텀 허용 그룹(PRODUCT_CATEGORY)은 창고관리자도 등록할 수 있다")
    void create_customizableGroup_warehouseManager_success() throws Exception {
        Company company = seedCompany("창고관리자등록기업", "242-42-42424");
        seedUser(company, "manager-create@test.com", UserRole.WAREHOUSE_MANAGER);
        String token = login("manager-create@test.com");

        mockMvc.perform(post("/api/common-code/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupCode", "PRODUCT_CATEGORY", "code", "MANAGER_CAT", "name", "창고관리자카테고리", "sortOrder", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyId").value(company.getId()));
    }

    @Test
    @DisplayName("커스텀 허용 그룹(PRODUCT_CATEGORY)은 기업관리자가 등록할 수 있다")
    void create_customizableGroup_success() throws Exception {
        Company company = seedCompany("등록성공기업", "333-33-33333");
        seedUser(company, "create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("create-admin@test.com");

        mockMvc.perform(post("/api/common-code/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupCode", "PRODUCT_CATEGORY", "code", "CUSTOM_CAT", "name", "커스텀카테고리", "sortOrder", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyId").value(company.getId()))
                .andExpect(jsonPath("$.data.code").value("CUSTOM_CAT"));
    }

    @Test
    @DisplayName("커스텀 불가 그룹(STORAGE_TYPE)은 기업관리자가 등록하면 거부된다")
    void create_nonCustomizableGroup_rejected() throws Exception {
        Company company = seedCompany("등록거부기업", "444-44-44444");
        seedUser(company, "reject-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("reject-admin@test.com");

        mockMvc.perform(post("/api/common-code/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupCode", "STORAGE_TYPE", "code", "COLD", "name", "냉장", "sortOrder", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMMON_CODE_GROUP_NOT_CUSTOMIZABLE.getMessage()));
    }

    @Test
    @DisplayName("다른 회사의 커스텀 코드를 수정하려 하면 거부된다")
    void update_otherCompanyCode_rejected() throws Exception {
        Company ownerCompany = seedCompany("코드소유기업", "555-55-55555");
        Company attackerCompany = seedCompany("공격기업", "666-66-66666");
        seedUser(attackerCompany, "attacker-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("attacker-admin@test.com");

        CommonCode ownerCode = commonCodeRepository.save(
                new CommonCode(ownerCompany, CommonCodeGroup.PRODUCT_CATEGORY, "OWNER_CAT", "소유기업카테고리", 1));

        mockMvc.perform(post("/api/common-code/" + ownerCode.getId() + "/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "변경시도", "sortOrder", 2))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMPANY_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("자사 커스텀 코드를 삭제하면 소프트 삭제되어 목록에서 사라진다")
    void delete_ownCode_softDeletes() throws Exception {
        Company company = seedCompany("삭제테스트기업", "777-77-77777");
        seedUser(company, "delete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("delete-admin@test.com");

        CommonCode code = commonCodeRepository.save(
                new CommonCode(company, CommonCodeGroup.PRODUCT_CATEGORY, "TO_DELETE", "삭제될코드", 1));

        mockMvc.perform(post("/api/common-code/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", code.getId()))))
                .andExpect(status().isOk());

        Optional<CommonCode> found = commonCodeRepository.findActiveById(code.getId());
        assertThat(found).isEmpty();
    }

}
