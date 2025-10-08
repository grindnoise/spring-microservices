package com.evilcorp.api.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class LoginTotalCountMetric {

    public static final String METRIC_NAME = "individual_app_login_count_total";

    private final Counter counter;

    public LoginTotalCountMetric(MeterRegistry registry) {
        this.counter = Counter.builder(METRIC_NAME).register(registry);
    }

    public void increment() {
        counter.increment();
    }
}
