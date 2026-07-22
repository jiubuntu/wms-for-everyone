package com.jiubuntu.wms.admin.ui.commoncode;

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
class CommonCodeAdminApiIntegrationTest extends IntegrationTestSupport {

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
    @DisplayName("시스템관리자는 시스템 코드만 조회하며 회사 커스텀 코드는 섞이지 않는다")
    void list_onlySystemCodes() throws Exception {
        Company company = seedCompany("일반기업", "888-88-88888");
        Company adminCompany = seedCompany("관리기업", "999-99-99999");
        seedUser(adminCompany, "sysadmin@test.com", UserRole.SYSTEM_ADMIN);
        String token = login("sysadmin@test.com");

        commonCodeRepository.save(new CommonCode(null, CommonCodeGroup.PRODUCT_CATEGORY, "ELECTRONICS", "전자제품", 1));
        commonCodeRepository.save(new CommonCode(company, CommonCodeGroup.PRODUCT_CATEGORY, "CUSTOM_CAT", "회사커스텀", 2));

        mockMvc.perform(get("/api/admin/common-code/list")
                        .header("Authorization", "Bearer " + token)
                        .param("groupCode", "PRODUCT_CATEGORY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("ELECTRONICS"));
    }

    @Test
    @DisplayName("전체 목록 조회(/all)는 시스템 코드만 반환하며 회사 커스텀 코드는 섞이지 않는다")
    void listAll_onlySystemCodes() throws Exception {
        Company company = seedCompany("일반기업2", "141-41-41414");
        Company adminCompany = seedCompany("관리기업3", "151-51-51515");
        seedUser(adminCompany, "sysadmin3@test.com", UserRole.SYSTEM_ADMIN);
        String token = login("sysadmin3@test.com");

        commonCodeRepository.save(new CommonCode(null, CommonCodeGroup.STORAGE_TYPE, "COLD2", "냉장2", 1));
        commonCodeRepository.save(new CommonCode(company, CommonCodeGroup.PRODUCT_CATEGORY, "CUSTOM_CAT3", "회사커스텀3", 2));

        mockMvc.perform(get("/api/admin/common-code/all")
                        .header("Authorization", "Bearer " + token)
                        .param("groupCode", "STORAGE_TYPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("COLD2"));
    }

    @Test
    @DisplayName("기업관리자는 관리자 공통 코드 API에 접근할 수 없다")
    void list_deniedForCompanyAdmin() throws Exception {
        Company company = seedCompany("권한없는기업", "121-21-21212");
        seedUser(company, "company-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("company-admin@test.com");

        mockMvc.perform(get("/api/admin/common-code/list")
                        .header("Authorization", "Bearer " + token)
                        .param("groupCode", "PRODUCT_CATEGORY"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("시스템관리자는 커스텀 불가 그룹(STORAGE_TYPE)의 시스템 코드를 등록할 수 있다")
    void create_systemCode_anyGroup_success() throws Exception {
        Company adminCompany = seedCompany("관리기업2", "131-31-31313");
        seedUser(adminCompany, "sysadmin2@test.com", UserRole.SYSTEM_ADMIN);
        String token = login("sysadmin2@test.com");

        mockMvc.perform(post("/api/admin/common-code/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "groupCode", "STORAGE_TYPE", "code", "COLD", "name", "냉장", "sortOrder", 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyId").value(org.hamcrest.Matchers.nullValue()));
    }

}
