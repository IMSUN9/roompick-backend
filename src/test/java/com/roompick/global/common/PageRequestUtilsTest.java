package com.roompick.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PageRequestUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "-1, 0",
            "0, 0",
            "1, 1",
            "100, 100"
    })
    void resolvePage_보정결과를_반환한다(int page, int expected) {
        assertThat(PageRequestUtils.resolvePage(page)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 20",
            "0, 20",
            "1, 1",
            "100, 100",
            "101, 20"
    })
    void resolveSize_보정결과를_반환한다(int size, int expected) {
        assertThat(PageRequestUtils.resolveSize(size)).isEqualTo(expected);
    }
}
