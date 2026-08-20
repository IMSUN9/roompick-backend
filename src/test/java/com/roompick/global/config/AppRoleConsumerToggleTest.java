package com.roompick.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.roompick.domain.payment.consumer.PaymentCompletedEventConsumer;
import com.roompick.domain.specialOffers.consumer.OfferOccupyEventConsumer;

/**
 * app.role.consumer-enabled=false 로 기동한 인스턴스(API 전용 인스턴스)에서는
 * Kafka 컨슈머 Bean이 등록되지 않는지 검증합니다.
 */
@ActiveProfiles("test")
@SpringBootTest(properties = "app.role.consumer-enabled=false")
class AppRoleConsumerToggleTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("app.role.consumer-enabled=false 이면 컨슈머 Bean이 등록되지 않는다")
    void consumerBeansAreDisabledWhenRoleIsApiOnly() {
        assertThat(
            applicationContext.getBeansOfType(PaymentCompletedEventConsumer.class)
        ).isEmpty();

        assertThat(
            applicationContext.getBeansOfType(OfferOccupyEventConsumer.class)
        ).isEmpty();
    }
}
