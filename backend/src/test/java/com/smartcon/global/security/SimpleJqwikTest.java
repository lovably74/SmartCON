package com.smartcon.global.security;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * 간단한 jqwik 테스트 - 프레임워크 동작 확인용
 */
class SimpleJqwikTest {

    @Property
    void simplePropertyTest(@ForAll int number) {
        // 간단한 속성 테스트
        assertThat(number + 0).isEqualTo(number);
    }
}