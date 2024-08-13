package com.example.demo;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.TracingObservationHandler;
//import io.micrometer.tracing.otel.bridge.OtelSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;

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
		io.micrometer.tracing.Span span = tracingContext.getSpan();

		try {
			// Get the class object for OtelSpan implementation
			Class<?> otelSpanClass = span.getClass();

			// Access the OpenTelemetry native Span object
			Field privateField = otelSpanClass.getDeclaredField("delegate");

			// Make the private field accessible
			privateField.setAccessible(true);

			// Retrieve the value of the OpenTelemetry native Span object
			Object field = privateField.get(span);

			if (field instanceof Span otelSpan) {
				otelSpan.addEvent("demo.event", Attributes.of(
						AttributeKey.stringKey("demo.attribute1"), "value of attribute 1"
				));
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

    @Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof DemoObservationContext;
	}

}
