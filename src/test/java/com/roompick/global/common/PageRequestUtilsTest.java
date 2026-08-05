package com.roompick.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PageRequestUtils의 페이지네이션 파라미터 보정 로직을 검증하는 단위 테스트입니다.
 */
class PageRequestUtilsTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100})
    @DisplayName("page가 0 이상이면 그대로 반환한다")
    void resolvePage_validValue_returnsAsIs(int page) {
        // when
        int resolved = PageRequestUtils.resolvePage(page);

        // then
        assertThat(resolved).isEqualTo(page);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    @DisplayName("page가 음수이면 0으로 보정한다")
    void resolvePage_negativeValue_returnsZero(int page) {
        // when
        int resolved = PageRequestUtils.resolvePage(page);

        // then
        assertThat(resolved).isZero();
    }

    @ParameterizedTest
    @CsvSource({"1", "20", "100"})
    @DisplayName("size가 1~100 범위 내이면 그대로 반환한다")
    void resolveSize_validValue_returnsAsIs(int size) {
        // when
        int resolved = PageRequestUtils.resolveSize(size);

        // then
        assertThat(resolved).isEqualTo(size);
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "101", "1000"})
    @DisplayName("size가 1~100 범위를 벗어나면 기본값 20으로 보정한다")
    void resolveSize_outOfRange_returnsDefault(int size) {
        // when
        int resolved = PageRequestUtils.resolveSize(size);

        // then
        assertThat(resolved).isEqualTo(20);
    }
}
