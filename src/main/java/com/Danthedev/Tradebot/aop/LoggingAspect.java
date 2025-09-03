package com.Danthedev.Tradebot.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.Danthedev.Tradebot.service..*(..)) || " +
            "execution(* com.Danthedev.Tradebot.repository..*(..)) || " +
            "execution(* com.danthedev.tradebot.TelegramBot.*(..))")
    public Object loggingServiceMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        Object[] args = proceedingJoinPoint.getArgs();

        Instant startMethod = Instant.now();
        try {
            log.info("➡️ Started {} with args: {}", methodName, Arrays.toString(args));
            Object result = proceedingJoinPoint.proceed();

            Instant endMethod = Instant.now();
            Duration duration = Duration.between(startMethod, endMethod);

            log.info("Completed {} | Result: {} | Duration: {} ms",
                    methodName,
                    summarize(result),
                    duration.toMillis());

            return result;
        } catch (Throwable e) {
            Instant endMethod = Instant.now();
            Duration duration = Duration.between(startMethod, endMethod);

            log.error("❌ Exception in {} | Args: {} | Duration: {} ms | Error: {}",
                    methodName,
                    Arrays.toString(args),
                    duration.toMillis(),
                    e.getMessage(),
                    e);

            throw e;
        }
    }

    private String summarize(Object result) {
        if (result == null) return "null";
        String str = result.toString();
        return str.length() > 200 ? str.substring(0, 200) + "...(truncated)" : str;
    }
}

