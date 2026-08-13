package com.novaforum.nova_forum.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private static final String EMAIL = "member@example.com";
    private static final String CODE_KEY = "email:verification:" + EMAIL;
    private static final String RATE_LIMIT_KEY = "email:rate_limit:" + EMAIL;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        emailService = new EmailServiceImpl(mailSender, redisTemplate, "noreply@example.com");
    }

    @Test
    void rejectsRequestWhenAtomicRateLimitCannotBeAcquired() {
        when(valueOperations.setIfAbsent(RATE_LIMIT_KEY, "1", 60L, TimeUnit.SECONDS))
                .thenReturn(false);

        boolean sent = emailService.sendVerificationCode(EMAIL);

        assertThat(sent).isFalse();
        verify(valueOperations).setIfAbsent(RATE_LIMIT_KEY, "1", 60L, TimeUnit.SECONDS);
        verifyNoMoreInteractions(valueOperations);
        verifyNoInteractions(mailSender);
    }

    @Test
    void acquiresRateLimitBeforeCachingAndSendingCode() {
        when(valueOperations.setIfAbsent(RATE_LIMIT_KEY, "1", 60L, TimeUnit.SECONDS))
                .thenReturn(true);

        boolean sent = emailService.sendVerificationCode(EMAIL);

        assertThat(sent).isTrue();
        InOrder order = inOrder(valueOperations, mailSender);
        order.verify(valueOperations).setIfAbsent(RATE_LIMIT_KEY, "1", 60L, TimeUnit.SECONDS);
        order.verify(valueOperations).set(
                eq(CODE_KEY),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES));
        order.verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void removesUnsentCodeButKeepsCooldownWhenMailDeliveryFails() {
        when(valueOperations.setIfAbsent(RATE_LIMIT_KEY, "1", 60L, TimeUnit.SECONDS))
                .thenReturn(true);
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        boolean sent = emailService.sendVerificationCode(EMAIL);

        assertThat(sent).isFalse();
        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate, never()).delete(RATE_LIMIT_KEY);
    }
}
