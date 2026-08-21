package com.roompick.domain.member.repository;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * isBlacklisted()는 인증된 모든 요청이 JwtAuthenticationFilter에서 거치는
 * 경로라, 부하가 몰리면 Redis 왕복 자체가 병목이 될 수 있다(실측:
 * docs/performance/SPECIAL_OFFER_PRODUCE_CONSUME_RATE.md).
 *
 * "블랙리스트에 없다"는 결과를 짧게 로컬 캐시해서, 대부분(정상 토큰)인
 * 요청은 Redis를 아예 안 타게 만든다. 로그아웃 처리(consume)는 여전히
 * Redis에 즉시 반영되지만, 다른 인스턴스나 이미 캐시된 결과를 가진
 * 요청은 캐시 TTL만큼(최대 5초) 블랙리스트 반영이 늦게 보일 수 있다 —
 * 로그아웃 즉시성보다 인증 경로의 안정성을 우선한 트레이드오프다.
 *
 * isBlacklisted()는 Redis 장애·타임아웃 시 fail-open(블랙리스트 아님으로
 * 간주)한다. 액세스 토큰 만료(30분)가 짧고, 이 조회의 목적이 "로그아웃한
 * 토큰 차단"이라, Redis가 잠깐 흔들릴 때 인증된 요청 전체가 500으로
 * 죽는 것보다는 최대 30분간 로그아웃 토큰이 통과할 수 있는 쪽이 낫다는
 * 판단이다(실측: docs/performance/SPECIAL_OFFER_REDIS_BLACKLIST_TIMEOUT.md
 * — VU 1200에서 이 경로의 Redis 타임아웃이 인증 요청 전체를 실패시켰다).
 */
@Slf4j
@Repository
@Profile("!test")
@RequiredArgsConstructor
public class RedisTokenBlacklistRepository implements TokenBlacklistRepository {

    private static final String KEY_PREFIX = "blacklist:";
    private static final Duration NOT_BLACKLISTED_CACHE_TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;

    private final Cache<String, Boolean> notBlacklistedCache = Caffeine.newBuilder()
        .expireAfterWrite(NOT_BLACKLISTED_CACHE_TTL)
        .maximumSize(100_000)
        .build();

    @Override
    public boolean consume(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return false;
        }

        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key(jti), "1", Duration.ofSeconds(ttlSeconds));

        boolean blacklisted = Boolean.TRUE.equals(result);

        if (blacklisted) {
            notBlacklistedCache.invalidate(jti);
        }

        return blacklisted;
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (notBlacklistedCache.getIfPresent(jti) != null) {
            return false;
        }

        boolean blacklisted;
        try {
            blacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key(jti)));
        } catch (DataAccessException exception) {
            log.warn(
                "블랙리스트 조회 중 Redis 오류가 발생해 fail-open으로 통과시킵니다. jti={}",
                jti, exception
            );
            return false;
        }

        if (!blacklisted) {
            notBlacklistedCache.put(jti, Boolean.TRUE);
        }

        return blacklisted;
    }

    private String key(String jti) {
        return KEY_PREFIX + jti;
    }
}
