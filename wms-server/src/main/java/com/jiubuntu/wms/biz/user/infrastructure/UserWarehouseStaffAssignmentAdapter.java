package com.jiubuntu.wms.biz.user.infrastructure;

import com.jiubuntu.wms.biz.warehouse.domain.WarehouseStaffAssignmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserWarehouseStaffAssignmentAdapter implements WarehouseStaffAssignmentPort {

    private final UserRepository userRepository;

    @Override
    public boolean hasActiveStaff(Long warehouseId) {
        return userRepository.existsActiveByWarehouseId(warehouseId);
    }

}
