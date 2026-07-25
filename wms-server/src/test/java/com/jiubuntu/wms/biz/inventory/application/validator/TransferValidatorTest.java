package com.jiubuntu.wms.biz.inventory.application.validator;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;

class TransferValidatorTest {

    private final TransferValidator transferValidator = new TransferValidator();

    private Location locationOf(Long id, LocationStatus status) {
        Location location = Location.builder()
                .zone("A").row("01").col("01").level("1").code("LOC-" + id)
                .build();
        ReflectionTestUtils.setField(location, "id", id);
        ReflectionTestUtils.setField(location, "status", status);
        return location;
    }

    @Test
    @DisplayName("출발 위치와 도착 위치가 같으면 TRANSFER_SAME_LOCATION 예외가 발생한다")
    void validateLocations_sameLocation_throws() {
        Location location = locationOf(1L, LocationStatus.ACTIVE);

        assertThatThrownBy(() -> transferValidator.validateLocations(location, location))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSFER_SAME_LOCATION);
    }

    @Test
    @DisplayName("도착 위치가 비활성화 상태면 TRANSFER_LOCATION_DISABLED 예외가 발생한다")
    void validateLocations_disabledDestination_throws() {
        Location from = locationOf(1L, LocationStatus.ACTIVE);
        Location to = locationOf(2L, LocationStatus.DISABLED);

        assertThatThrownBy(() -> transferValidator.validateLocations(from, to))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSFER_LOCATION_DISABLED);
    }

    @Test
    @DisplayName("서로 다른 활성 위치면 예외가 발생하지 않는다")
    void validateLocations_valid_success() {
        Location from = locationOf(1L, LocationStatus.ACTIVE);
        Location to = locationOf(2L, LocationStatus.ACTIVE);

        assertThatCode(() -> transferValidator.validateLocations(from, to)).doesNotThrowAnyException();
        assertThat(from.getId()).isNotEqualTo(to.getId());
    }

}
