package com.jiubuntu.wms.biz.outbound.application.validator;

import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundValidatorTest {

    private final OutboundValidator outboundValidator = new OutboundValidator();

    private ProductUnit unitOf(Long id) {
        ProductUnit unit = new ProductUnit(null, "단위" + id);
        ReflectionTestUtils.setField(unit, "id", id);
        return unit;
    }

    private Product productOf(ProductUnit baseUnit, ProductUnit subUnit) {
        return new Product(null, "SKU-1", "테스트상품", null, null, baseUnit, subUnit,
                subUnit != null ? BigDecimal.valueOf(10) : null, false, null);
    }

    private Outbound outboundOf(OutboundStatus status) {
        Outbound outbound = new Outbound(null, null, "고객사", null);
        ReflectionTestUtils.setField(outbound, "status", status);
        return outbound;
    }

    @Test
    @DisplayName("기본 단위로 출고를 등록하면 예외가 발생하지 않는다")
    void validateUnit_baseUnit_success() {
        ProductUnit baseUnit = unitOf(1L);
        Product product = productOf(baseUnit, null);

        assertThatCode(() -> outboundValidator.validateUnit(product, baseUnit)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("보조 단위로 출고를 등록하면 예외가 발생하지 않는다")
    void validateUnit_subUnit_success() {
        ProductUnit baseUnit = unitOf(1L);
        ProductUnit subUnit = unitOf(2L);
        Product product = productOf(baseUnit, subUnit);

        assertThatCode(() -> outboundValidator.validateUnit(product, subUnit)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상품에 등록되지 않은 단위면 OUTBOUND_INVALID_UNIT 예외가 발생한다")
    void validateUnit_invalidUnit_throws() {
        ProductUnit baseUnit = unitOf(1L);
        Product product = productOf(baseUnit, null);
        ProductUnit otherUnit = unitOf(99L);

        assertThatThrownBy(() -> outboundValidator.validateUnit(product, otherUnit))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_INVALID_UNIT);
    }

    @Test
    @DisplayName("MANUAL 할당 수량 합계가 요청 수량과 같으면 예외가 발생하지 않는다")
    void validateManualAllocationSum_match_success() {
        assertThatCode(() -> outboundValidator.validateManualAllocationSum(30, 30)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("MANUAL 할당 수량 합계가 요청 수량과 다르면 OUTBOUND_ALLOCATION_MISMATCH 예외가 발생한다")
    void validateManualAllocationSum_mismatch_throws() {
        assertThatThrownBy(() -> outboundValidator.validateManualAllocationSum(30, 20))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALLOCATION_MISMATCH);
    }

    @Test
    @DisplayName("PENDING 상태는 확정/취소 검증을 통과한다")
    void validate_pending_success() {
        Outbound outbound = outboundOf(OutboundStatus.PENDING);

        assertThatCode(() -> outboundValidator.validateComplete(outbound)).doesNotThrowAnyException();
        assertThatCode(() -> outboundValidator.validateCancel(outbound)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 COMPLETED인 출고를 확정/취소하려 하면 OUTBOUND_ALREADY_COMPLETED 예외가 발생한다")
    void validate_alreadyCompleted_throws() {
        Outbound outbound = outboundOf(OutboundStatus.COMPLETED);

        assertThatThrownBy(() -> outboundValidator.validateComplete(outbound))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALREADY_COMPLETED);
        assertThatThrownBy(() -> outboundValidator.validateCancel(outbound))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("이미 CANCELLED인 출고를 확정/취소하려 하면 OUTBOUND_ALREADY_CANCELLED 예외가 발생한다")
    void validate_alreadyCancelled_throws() {
        Outbound outbound = outboundOf(OutboundStatus.CANCELLED);

        assertThatThrownBy(() -> outboundValidator.validateComplete(outbound))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALREADY_CANCELLED);
        assertThatThrownBy(() -> outboundValidator.validateCancel(outbound))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALREADY_CANCELLED);
    }

}
