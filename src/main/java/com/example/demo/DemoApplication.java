package com.example.demo;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.TracingObservationHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@RestController
class DemoController {

	private final ObservationRegistry observationRegistry;

    DemoController(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @GetMapping("/")
	String demo() {
		return Observation.createNotStarted("demo-observation", DemoObservationContext::new, observationRegistry)
				.observe(() -> "Hello World");
	}

}

class DemoObservationContext extends Observation.Context {}

@Component
class DemoObservationHandler implements ObservationHandler<DemoObservationContext> {

	private static final Logger logger = LoggerFactory.getLogger(DemoObservationHandler.class);

	/**
	 * If https://github.com/micrometer-metrics/tracing/issues/808 is solved,
	 * then this would be the implementation of this logic.
	 * @param context an {@link Observation.Context}
	 */
//	@Override
//	public void onStop(DemoObservationContext context) {
//		TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
//		Span otelSpan = OtelSpan.toOtel(tracingContext.getSpan());
//		otelSpan.addEvent("demo.event", Attributes.of(
//				AttributeKey.stringKey("demo.attribute1"), "value of attribute 1"
//		));
//	}

	@Override
	public void onStop(DemoObservationContext context) {
		TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
		if (tracingContext == null) {
			return;
		}

		io.micrometer.tracing.Span micrometerSpan = tracingContext.getSpan();
		try {
			Method toOtelMethod = tracingContext.getSpan().getClass().getDeclaredMethod("toOtel", io.micrometer.tracing.Span.class);
			toOtelMethod.setAccessible(true);
			Object otelSpanObject = toOtelMethod.invoke(null, micrometerSpan);
			if (otelSpanObject instanceof Span otelSpan) {
				otelSpan.addEvent("demo.event", Attributes.of(
						AttributeKey.stringKey("demo.attribute1"), "value of attribute 1"
				));
			}
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
			logger.warn("It wasn't possible to add the chat prompt content as a span event", ex);
		}

	}

    @Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof DemoObservationContext;
	}

}
