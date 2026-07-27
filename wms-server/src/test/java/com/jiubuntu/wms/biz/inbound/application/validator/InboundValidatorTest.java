package com.jiubuntu.wms.biz.inbound.application.validator;

import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundValidatorTest {

    private final InboundValidator inboundValidator = new InboundValidator();

    private ProductUnit unitOf(Long id) {
        ProductUnit unit = new ProductUnit(null, "단위" + id);
        ReflectionTestUtils.setField(unit, "id", id);
        return unit;
    }

    private Product productOf(ProductUnit baseUnit, ProductUnit subUnit, boolean lotTracking) {
        return new Product(null, "SKU-1", "테스트상품", null, null, baseUnit, subUnit,
                subUnit != null ? BigDecimal.valueOf(10) : null, lotTracking, null);
    }

    private Inbound inboundOf(InboundStatus status) {
        Inbound inbound = new Inbound(null, null, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "status", status);
        return inbound;
    }

    @Test
    @DisplayName("기본 단위/보조 단위면 예외가 발생하지 않는다")
    void validateUnit_success() {
        ProductUnit baseUnit = unitOf(1L);
        ProductUnit subUnit = unitOf(2L);
        Product product = productOf(baseUnit, subUnit, false);

        assertThatCode(() -> inboundValidator.validateUnit(product, baseUnit)).doesNotThrowAnyException();
        assertThatCode(() -> inboundValidator.validateUnit(product, subUnit)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상품에 등록되지 않은 단위면 INBOUND_INVALID_UNIT 예외가 발생한다")
    void validateUnit_invalidUnit_throws() {
        ProductUnit baseUnit = unitOf(1L);
        Product product = productOf(baseUnit, null, false);
        ProductUnit otherUnit = unitOf(99L);

        assertThatThrownBy(() -> inboundValidator.validateUnit(product, otherUnit))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_INVALID_UNIT);
    }

    @Test
    @DisplayName("LOT 관리 상품인데 LOT 정보가 없으면 INBOUND_LOT_INFO_REQUIRED 예외가 발생한다")
    void validateLotFields_lotTrackingMissingInfo_throws() {
        Product product = productOf(unitOf(1L), null, true);

        assertThatThrownBy(() -> inboundValidator.validateLotFields(product, null, null, null))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_LOT_INFO_REQUIRED);
    }

    @Test
    @DisplayName("LOT 관리 상품이고 LOT 정보가 전부 있으면 예외가 발생하지 않는다")
    void validateLotFields_lotTrackingWithInfo_success() {
        Product product = productOf(unitOf(1L), null, true);

        assertThatCode(() -> inboundValidator.validateLotFields(
                product, "LOT-1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("LOT 비관리 상품이면 LOT 정보가 없어도 예외가 발생하지 않는다")
    void validateLotFields_notLotTracking_success() {
        Product product = productOf(unitOf(1L), null, false);

        assertThatCode(() -> inboundValidator.validateLotFields(product, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("위치 배치 합계가 요청 수량과 같으면 예외가 발생하지 않는다")
    void validateLocationSum_match_success() {
        assertThatCode(() -> inboundValidator.validateLocationSum(30, 30)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("위치 배치 합계가 요청 수량과 다르면 INBOUND_LOCATION_SUM_MISMATCH 예외가 발생한다")
    void validateLocationSum_mismatch_throws() {
        assertThatThrownBy(() -> inboundValidator.validateLocationSum(30, 20))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_LOCATION_SUM_MISMATCH);
    }

    @Test
    @DisplayName("배치 합계가 요청 수량과 같으면 완전 배치로 판단해 예외가 발생하지 않는다")
    void validateFullyPlaced_match_success() {
        assertThatCode(() -> inboundValidator.validateFullyPlaced(30, 30)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("배치 합계가 요청 수량보다 적으면 INBOUND_NOT_FULLY_PLACED 예외가 발생한다")
    void validateFullyPlaced_mismatch_throws() {
        assertThatThrownBy(() -> inboundValidator.validateFullyPlaced(30, 20))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_NOT_FULLY_PLACED);
    }

    @Test
    @DisplayName("PENDING/IN_PROGRESS 상태는 수정·확정·취소 검증을 통과한다")
    void validate_notTerminal_success() {
        Inbound pending = inboundOf(InboundStatus.PENDING);
        Inbound inProgress = inboundOf(InboundStatus.IN_PROGRESS);

        assertThatCode(() -> inboundValidator.validateModifiable(pending)).doesNotThrowAnyException();
        assertThatCode(() -> inboundValidator.validateComplete(inProgress)).doesNotThrowAnyException();
        assertThatCode(() -> inboundValidator.validateCancel(inProgress)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 COMPLETED/CANCELLED인 입고는 수정·확정·취소 시 각각 구분된 예외가 발생한다")
    void validate_terminal_throws() {
        Inbound completed = inboundOf(InboundStatus.COMPLETED);
        Inbound cancelled = inboundOf(InboundStatus.CANCELLED);

        assertThatThrownBy(() -> inboundValidator.validateModifiable(completed))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_ALREADY_COMPLETED);
        assertThatThrownBy(() -> inboundValidator.validateComplete(cancelled))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_ALREADY_CANCELLED);
    }

}
