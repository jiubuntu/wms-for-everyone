package com.jiubuntu.wms.biz.commoncode.application.validator;

import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.infrastructure.CommonCodeRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CommonCodeValidator {

    private final CommonCodeRepository commonCodeRepository;

    public void validateCreate(CommonCodeCreateCommand command) {
        if (command.getCompanyId() != null && !command.getGroupCode().isCustomizable()) {
            throw new CommonException(ErrorCode.COMMON_CODE_GROUP_NOT_CUSTOMIZABLE);
        }

        if (commonCodeRepository.existsActiveByCompanyAndGroupAndCode(
                command.getCompanyId(), command.getGroupCode(), command.getCode())) {
            throw new CommonException(ErrorCode.COMMON_CODE_ALREADY_EXISTS);
        }
    }

    public void validateScope(CommonCode existing, Long expectedCompanyId) {
        Long existingCompanyId = existing.getCompany() != null ? existing.getCompany().getId() : null;
        if (!Objects.equals(existingCompanyId, expectedCompanyId)) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }
    }

}
