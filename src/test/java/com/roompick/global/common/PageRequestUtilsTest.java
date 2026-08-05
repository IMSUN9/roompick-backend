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
        "10, 10",
    })
    void page가_음수이면_0으로_보정한다(int page, int expected) {
        // when
        int resolved = PageRequestUtils.resolvePage(page);

        // then
        assertThat(resolved).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 20",
        "1, 1",
        "50, 50",
        "100, 100",
        "101, 20",
        "-1, 20",
    })
    void size가_1에서_100_범위를_벗어나면_기본값_20으로_보정한다(int size, int expected) {
        // when
        int resolved = PageRequestUtils.resolveSize(size);

        // then
        assertThat(resolved).isEqualTo(expected);
    }
}
