package com.jiubuntu.wms.admin.application.commoncode;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeCreateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeDeleteCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.command.CommonCodeUpdateCommand;
import com.jiubuntu.wms.biz.commoncode.application.dto.result.CommonCodeResult;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeAdminService {

    private final CommonCodeService commonCodeService;

    public Page<CommonCodeResult> list(CommonCodeGroup groupCode, String keyword, Pageable pageable) {
        return commonCodeService.list(groupCode, null, keyword, pageable);
    }

    public List<CommonCodeResult> listAll(CommonCodeGroup groupCode) {
        return commonCodeService.listAll(groupCode, null);
    }

    @Transactional
    public CommonCode create(CommonCodeGroup groupCode, String code, String name, int sortOrder) {
        CommonCodeCreateCommand command = new CommonCodeCreateCommand(null, groupCode, code, name, sortOrder);
        return commonCodeService.create(command);
    }

    @Transactional
    public CommonCode update(Long id, String name, int sortOrder, Long updatedBy) {
        CommonCodeUpdateCommand command = new CommonCodeUpdateCommand(id, null, name, sortOrder, updatedBy);
        return commonCodeService.update(command);
    }

    @Transactional
    public void delete(Long id, Long updatedBy) {
        CommonCodeDeleteCommand command = new CommonCodeDeleteCommand(id, null, updatedBy);
        commonCodeService.delete(command);
    }

}
