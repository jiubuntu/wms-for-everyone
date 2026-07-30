package com.jiubuntu.wms.biz.commoncode.application;

import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeDeleteCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeUpdateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.result.CommonCodeResult;
import com.jiubuntu.wms.biz.commoncode.application.validator.CommonCodeValidator;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeValidator commonCodeValidator;
    private final CompanyService companyService;

    public Page<CommonCodeResult> list(CommonCodeGroup groupCode, Long companyId, String keyword, Pageable pageable) {
        return commonCodeRepository.findActiveByGroupVisibleTo(groupCode, companyId, keyword, pageable)
                .map(this::toResult);
    }

    public List<CommonCodeResult> listAll(CommonCodeGroup groupCode, Long companyId) {
        return commonCodeRepository.findActiveByGroupVisibleTo(groupCode, companyId).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public CommonCode create(CommonCodeCreateCommand command) {
        commonCodeValidator.validateCreate(command);

        Company company = command.getCompanyId() != null
                ? companyService.getActiveById(command.getCompanyId())
                : null;

        CommonCode commonCode = new CommonCode(
                company, command.getGroupCode(), command.getCode(), command.getName(), command.getSortOrder());
        return commonCodeRepository.save(commonCode);
    }

    @Transactional
    public CommonCode update(CommonCodeUpdateCommand command) {
        CommonCode commonCode = getActiveById(command.getId());
        commonCodeValidator.validateScope(commonCode, command.getExpectedCompanyId());

        commonCode.update(command.getName(), command.getSortOrder());
        commonCode.assignUpdater(command.getUpdatedBy());
        return commonCode;
    }

    @Transactional
    public void delete(CommonCodeDeleteCommand command) {
        CommonCode commonCode = getActiveById(command.getId());
        commonCodeValidator.validateScope(commonCode, command.getExpectedCompanyId());

        commonCode.assignUpdater(command.getUpdatedBy());
        commonCode.delete();
    }

    public CommonCode getAccessible(Long id, CommonCodeGroup expectedGroup, Long companyId) {
        CommonCode commonCode = getActiveById(id);

        Long ownerId = commonCode.getCompany() != null ? commonCode.getCompany().getId() : null;
        boolean groupMatches = commonCode.getGroupCode() == expectedGroup;
        boolean scopeMatches = ownerId == null || Objects.equals(ownerId, companyId);

        if (!groupMatches || !scopeMatches) {
            throw new CommonException(ErrorCode.COMMON_CODE_NOT_FOUND);
        }
        return commonCode;
    }

    private CommonCode getActiveById(Long id) {
        return commonCodeRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.COMMON_CODE_NOT_FOUND));
    }

    private CommonCodeResult toResult(CommonCode commonCode) {
        return new CommonCodeResult(
                commonCode.getId(),
                commonCode.getCompany() != null ? commonCode.getCompany().getId() : null,
                commonCode.getGroupCode(),
                commonCode.getCode(),
                commonCode.getName(),
                commonCode.getSortOrder(),
                commonCode.getCreatedAt()
        );
    }

}
