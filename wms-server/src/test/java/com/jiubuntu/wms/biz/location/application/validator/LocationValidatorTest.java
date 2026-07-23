package com.jiubuntu.wms.biz.location.application.validator;

import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationValidatorTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationValidator locationValidator;

    @Test
    @DisplayName("시작 행이 종료 행보다 크면 LOCATION_INVALID_RANGE 예외가 발생한다")
    void validateRange_rowFromGreaterThanRowTo_throws() {
        assertThatThrownBy(() -> locationValidator.validateRange(5, 1, 1, 1, 1))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.LOCATION_INVALID_RANGE);
    }

    @Test
    @DisplayName("단 수가 0 이하이면 LOCATION_INVALID_RANGE 예외가 발생한다")
    void validateRange_levelCountZero_throws() {
        assertThatThrownBy(() -> locationValidator.validateRange(1, 1, 1, 1, 0))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.LOCATION_INVALID_RANGE);
    }

    @Test
    @DisplayName("올바른 범위면 예외가 발생하지 않는다")
    void validateRange_success() {
        assertThatCode(() -> locationValidator.validateRange(1, 5, 1, 10, 3)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 존재하는 코드가 포함되어 있으면 LOCATION_ALREADY_EXISTS 예외가 발생한다")
    void validateNoDuplicateCodes_duplicateExists_throws() {
        when(locationRepository.findActiveCodesByWarehouseAndCodeIn(1L, List.of("A-01-01-1")))
                .thenReturn(List.of("A-01-01-1"));

        assertThatThrownBy(() -> locationValidator.validateNoDuplicateCodes(1L, List.of("A-01-01-1")))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.LOCATION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("중복 코드가 없으면 예외가 발생하지 않는다")
    void validateNoDuplicateCodes_noDuplicate_success() {
        when(locationRepository.findActiveCodesByWarehouseAndCodeIn(1L, List.of("A-01-01-1")))
                .thenReturn(List.of());

        assertThatCode(() -> locationValidator.validateNoDuplicateCodes(1L, List.of("A-01-01-1")))
                .doesNotThrowAnyException();
    }

}
