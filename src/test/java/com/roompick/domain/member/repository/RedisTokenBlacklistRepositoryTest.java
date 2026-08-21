package com.roompick.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 인증 경로(JwtAuthenticationFilter)가 매 요청마다 타는 isBlacklisted()가
 * 부하 상황에서 Redis 왕복 자체를 병목으로 만드는 것을 막기 위해 추가한
 * 로컬 negative 캐시가 의도대로 동작하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("블랙리스트가 아닌 jti를 반복 조회하면 Redis는 최초 1번만 조회한다")
    void isBlacklisted_캐싱된_결과는_Redis를_다시_조회하지_않는다() {
        // given
        RedisTokenBlacklistRepository repository = new RedisTokenBlacklistRepository(redisTemplate);
        String jti = "jti-not-blacklisted";

        given(redisTemplate.hasKey(anyString())).willReturn(false);

        // when
        boolean first = repository.isBlacklisted(jti);
        boolean second = repository.isBlacklisted(jti);
        boolean third = repository.isBlacklisted(jti);

        // then
        assertThat(first).isFalse();
        assertThat(second).isFalse();
        assertThat(third).isFalse();

        verify(redisTemplate, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("블랙리스트인 jti는 캐시하지 않고 매번 Redis를 조회한다")
    void isBlacklisted_블랙리스트인_경우는_캐싱하지_않는다() {
        // given
        RedisTokenBlacklistRepository repository = new RedisTokenBlacklistRepository(redisTemplate);
        String jti = "jti-blacklisted";

        given(redisTemplate.hasKey(anyString())).willReturn(true);

        // when
        boolean first = repository.isBlacklisted(jti);
        boolean second = repository.isBlacklisted(jti);

        // then
        assertThat(first).isTrue();
        assertThat(second).isTrue();

        verify(redisTemplate, times(2)).hasKey(anyString());
    }

    @Test
    @DisplayName("consume()으로 블랙리스트에 등록되면 이전에 캐싱된 not-blacklisted 결과는 무효화된다")
    void consume_이후에는_캐싱된_결과_대신_다시_블랙리스트로_판정된다() {
        // given
        RedisTokenBlacklistRepository repository = new RedisTokenBlacklistRepository(redisTemplate);
        String jti = "jti-logout";

        given(redisTemplate.hasKey(anyString())).willReturn(false);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(
            valueOperations.setIfAbsent(anyString(), anyString(), org.mockito.ArgumentMatchers.any(Duration.class))
        ).willReturn(true);

        // when — 캐시에 "블랙리스트 아님"이 먼저 쌓인다
        boolean beforeLogout = repository.isBlacklisted(jti);

        // 로그아웃으로 블랙리스트에 등록
        boolean consumed = repository.consume(jti, 60L);

        // then
        assertThat(beforeLogout).isFalse();
        assertThat(consumed).isTrue();

        // 캐시가 무효화됐으므로 다시 조회하면 Redis를 다시 탄다
        given(redisTemplate.hasKey(anyString())).willReturn(true);
        boolean afterLogout = repository.isBlacklisted(jti);

        assertThat(afterLogout).isTrue();
        verify(redisTemplate, times(2)).hasKey(anyString());
    }

    @Test
    @DisplayName("Redis 조회 중 오류가 발생하면 fail-open으로 블랙리스트가 아닌 것으로 처리한다")
    void isBlacklisted_Redis_오류_시_fail_open으로_통과시킨다() {
        // given
        RedisTokenBlacklistRepository repository = new RedisTokenBlacklistRepository(redisTemplate);
        String jti = "jti-redis-timeout";

        given(redisTemplate.hasKey(anyString()))
            .willThrow(new QueryTimeoutException("Redis command timed out"));

        // when
        boolean result = repository.isBlacklisted(jti);

        // then
        assertThat(result).isFalse();
    }
}
