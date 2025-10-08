package com.evilcorp.api.aspect;

import com.evilcorp.api.metric.LoginTotalCountMetric;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginMetricAspect {

    private final LoginTotalCountMetric loginTotalCountMetric;

    @AfterReturning(value = "execution(public com.evilcorp.api.service.TokenService.login(..))")
    public void afterLogin() {
        loginTotalCountMetric.increment();
    }

}
