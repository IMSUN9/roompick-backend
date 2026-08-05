package com.roompick.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void 앞뒤_공백을_제거한다() {
        // given: 앞뒤에 공백이 포함된 문자열입니다.
        String value = "  roompick  ";

        // when: trimToEmpty를 호출합니다.
        String result = StringUtils.trimToEmpty(value);

        // then: 공백이 제거된 문자열이 반환된다.
        assertThat(result).isEqualTo("roompick");
    }

    @Test
    void null이면_빈_문자열을_반환한다() {
        // given: null 값입니다.
        String value = null;

        // when: trimToEmpty를 호출합니다.
        String result = StringUtils.trimToEmpty(value);

        // then: 빈 문자열이 반환된다.
        assertThat(result).isEmpty();
    }
}
