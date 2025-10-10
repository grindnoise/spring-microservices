package com.evilcorp.api.aspect;

import com.evilcorp.api.metric.LoginTotalCountMetric;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginMetricAspect {

    private final LoginTotalCountMetric loginTotalCountMetric;

    /**
     * execution - Matches method execution join points
     * public - Only public methods
     * * - Any return type
     * com.evilcorp.api.service.TokenService.login - Fully qualified method name
     * (..) - Any number of parameters (0 or more)
     */
    @AfterReturning(value = "execution(public * com.evilcorp.api.service.TokenService.login(..))")
    public void afterLogin() {
        loginTotalCountMetric.increment();
    }

}
