package com.jiubuntu.wms.biz.product.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class ProductApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

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

    private CommonCode seedCategory(String code) {
        return commonCodeRepository.save(new CommonCode(null, CommonCodeGroup.PRODUCT_CATEGORY, code, code, 1));
    }

    private CommonCode seedStorageType(String code) {
        return commonCodeRepository.save(new CommonCode(null, CommonCodeGroup.STORAGE_TYPE, code, code, 1));
    }

    private ProductUnit seedUnit(Company company, String name) {
        return productUnitRepository.save(new ProductUnit(company, name));
    }

    private Map<String, Object> baseCreateBody(String skuCode, Long categoryId, Long storageTypeId, Long baseUnitId) {
        Map<String, Object> body = new HashMap<>();
        body.put("skuCode", skuCode);
        body.put("name", "사과");
        body.put("categoryId", categoryId);
        body.put("storageTypeId", storageTypeId);
        body.put("baseUnitId", baseUnitId);
        body.put("lotTracking", false);
        return body;
    }

    @Test
    @DisplayName("기업관리자는 자사 상품 목록을 조회한다")
    void list_returnsOwnCompanyProducts() throws Exception {
        Company company = seedCompany("목록조회기업", "111-11-11111");
        seedUser(company, "list-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("list-admin@test.com");

        CommonCode category = seedCategory("CAT1");
        CommonCode storageType = seedStorageType("STORE1");
        ProductUnit unit = seedUnit(company, "개1");
        productRepository.save(Product.builder()
                .company(company).skuCode("SKU-LIST-1").name("사과").category(category)
                .storageType(storageType).baseUnit(unit).lotTracking(false).build());

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].skuCode").value("SKU-LIST-1"));
    }

    @Test
    @DisplayName("작업자는 상품 API에 접근할 수 없다")
    void list_deniedForWorker() throws Exception {
        Company company = seedCompany("작업자기업", "222-22-22222");
        seedUser(company, "worker@test.com", UserRole.WORKER);
        String token = login("worker@test.com");

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상품을 등록할 수 있다")
    void create_success() throws Exception {
        Company company = seedCompany("등록기업", "333-33-33333");
        seedUser(company, "create-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("create-admin@test.com");

        CommonCode category = seedCategory("CAT2");
        CommonCode storageType = seedStorageType("STORE2");
        ProductUnit unit = seedUnit(company, "개2");

        mockMvc.perform(post("/api/product/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                baseCreateBody("SKU-0001", category.getId(), storageType.getId(), unit.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.skuCode").value("SKU-0001"))
                .andExpect(jsonPath("$.data.companyId").value(company.getId()));
    }

    @Test
    @DisplayName("같은 회사에 이미 등록된 SKU 코드면 409를 반환한다")
    void create_duplicateSku_rejected() throws Exception {
        Company company = seedCompany("중복SKU기업", "444-44-44444");
        seedUser(company, "dup-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("dup-admin@test.com");

        CommonCode category = seedCategory("CAT3");
        CommonCode storageType = seedStorageType("STORE3");
        ProductUnit unit = seedUnit(company, "개3");
        productRepository.save(Product.builder()
                .company(company).skuCode("SKU-DUP").name("사과").category(category)
                .storageType(storageType).baseUnit(unit).lotTracking(false).build());

        mockMvc.perform(post("/api/product/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                baseCreateBody("SKU-DUP", category.getId(), storageType.getId(), unit.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("보조 단위 없이 변환율만 입력하면 거부된다")
    void create_rateWithoutSubUnit_rejected() throws Exception {
        Company company = seedCompany("변환율기업", "555-55-55555");
        seedUser(company, "rate-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("rate-admin@test.com");

        CommonCode category = seedCategory("CAT4");
        CommonCode storageType = seedStorageType("STORE4");
        ProductUnit unit = seedUnit(company, "개4");

        Map<String, Object> body = baseCreateBody("SKU-RATE", category.getId(), storageType.getId(), unit.getId());
        body.put("unitConversionRate", 24);

        mockMvc.perform(post("/api/product/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_SUB_UNIT_CONVERSION_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("다른 회사의 커스텀 카테고리를 참조하면 거부된다")
    void create_inaccessibleCategory_rejected() throws Exception {
        Company company = seedCompany("참조위반기업", "666-66-66666");
        Company otherCompany = seedCompany("다른회사3", "999-33-33333");
        seedUser(company, "ref-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("ref-admin@test.com");

        CommonCode otherCompanyCategory = commonCodeRepository.save(
                new CommonCode(otherCompany, CommonCodeGroup.PRODUCT_CATEGORY, "OTHER_CAT", "다른회사카테고리", 1));
        CommonCode storageType = seedStorageType("STORE5");
        ProductUnit unit = seedUnit(company, "개5");

        mockMvc.perform(post("/api/product/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                baseCreateBody("SKU-REF", otherCompanyCategory.getId(), storageType.getId(), unit.getId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMMON_CODE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("다른 회사의 상품을 수정하려 하면 거부된다")
    void update_otherCompanyProduct_rejected() throws Exception {
        Company ownerCompany = seedCompany("상품소유기업", "777-77-77777");
        Company attackerCompany = seedCompany("공격기업2", "888-88-88888");
        seedUser(attackerCompany, "attacker-admin2@test.com", UserRole.COMPANY_ADMIN);
        String token = login("attacker-admin2@test.com");

        CommonCode category = seedCategory("CAT6");
        CommonCode storageType = seedStorageType("STORE6");
        ProductUnit unit = seedUnit(ownerCompany, "개6");
        Product product = productRepository.save(Product.builder()
                .company(ownerCompany).skuCode("SKU-OWNER").name("사과").category(category)
                .storageType(storageType).baseUnit(unit).lotTracking(false).build());

        Map<String, Object> body = new HashMap<>();
        body.put("name", "변경시도");
        body.put("categoryId", category.getId());
        body.put("storageTypeId", storageType.getId());
        body.put("baseUnitId", unit.getId());
        body.put("lotTracking", false);

        mockMvc.perform(post("/api/product/" + product.getId() + "/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.COMPANY_SCOPE_VIOLATION.getMessage()));
    }

    @Test
    @DisplayName("자사 상품을 삭제하면 소프트 삭제되어 목록에서 사라진다")
    void delete_ownProduct_softDeletes() throws Exception {
        Company company = seedCompany("삭제테스트기업", "121-21-21212");
        seedUser(company, "delete-admin@test.com", UserRole.COMPANY_ADMIN);
        String token = login("delete-admin@test.com");

        CommonCode category = seedCategory("CAT7");
        CommonCode storageType = seedStorageType("STORE7");
        ProductUnit unit = seedUnit(company, "개7");
        Product product = productRepository.save(Product.builder()
                .company(company).skuCode("SKU-DELETE").name("사과").category(category)
                .storageType(storageType).baseUnit(unit).lotTracking(false).build());

        mockMvc.perform(post("/api/product/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", product.getId()))))
                .andExpect(status().isOk());

        Optional<Product> found = productRepository.findActiveById(product.getId());
        assertThat(found).isEmpty();
    }

}
