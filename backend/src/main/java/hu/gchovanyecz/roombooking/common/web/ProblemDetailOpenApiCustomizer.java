package hu.gchovanyecz.roombooking.common.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Gives every 4xx and 5xx response that a controller declares with {@code @ApiResponse} the
 * {@link ProblemDetailDoc} body, because all of them are RFC 9457 problems. The controllers therefore only state the
 * status codes. Examples are added for the two responses that carry extra fields: 400 for request body validation
 * errors and 409 for booking conflicts.
 */
@Component
public class ProblemDetailOpenApiCustomizer implements OpenApiCustomizer {

	static final String PROBLEM_JSON = "application/problem+json";

	private static final String SCHEMA_NAME = "ProblemDetail";

	private static final String SCHEMA_REF = "#/components/schemas/" + SCHEMA_NAME;

	@Override
	public void customise(OpenAPI openApi) {
		if (openApi.getComponents() == null) {
			openApi.setComponents(new Components());
		}
		Components components = openApi.getComponents();
		ModelConverters.getInstance().readAll(ProblemDetailDoc.class)
			.forEach((name, schema) -> components.addSchemas(name, schema));

		if (openApi.getPaths() != null) {
			openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations()
				.forEach(ProblemDetailOpenApiCustomizer::documentErrorResponses));
		}
	}

	private static void documentErrorResponses(Operation operation) {
		if (operation.getResponses() == null) {
			return;
		}
		// Springdoc fills every declared response with the operation's success body (as "*/*"), so the error
		// responses are replaced rather than left alone: all 4xx and 5xx responses are RFC 9457 problems.
		operation.getResponses().forEach((code, response) -> {
			if (isError(code)) {
				response.setContent(problemContent(examplesFor(code, operation)));
			}
		});
	}

	private static boolean isError(String code) {
		return code.startsWith("4") || code.startsWith("5");
	}

	private static Map<String, Example> examplesFor(String code, Operation operation) {
		Map<String, Example> examples = new LinkedHashMap<>();
		if ("400".equals(code) && operation.getRequestBody() != null) {
			examples.put("validationErrors", new Example()
				.summary("A foglalás nem felel meg a szabályoknak")
				.value(validationErrorExample()));
		}
		if ("409".equals(code)) {
			examples.put("bookingConflict", new Example()
				.summary("Átfedő foglalás ugyanabban a tárgyalóban")
				.value(conflictExample()));
		}
		return examples;
	}

	private static Content problemContent(Map<String, Example> examples) {
		MediaType mediaType = new MediaType().schema(new Schema<>().$ref(SCHEMA_REF));
		examples.forEach(mediaType::addExamples);
		return new Content().addMediaType(PROBLEM_JSON, mediaType);
	}

	private static Map<String, Object> validationErrorExample() {
		Map<String, Object> problem = problem(400, "Bad Request",
				"A foglalás nem felel meg a foglalási szabályoknak.");
		problem.put("errors", List.of(
				object("field", "startTime", "message", "Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15)."),
				object("field", "endTime", "message", "A foglalás legalább 15 perc legyen.")));
		return problem;
	}

	private static Map<String, Object> conflictExample() {
		Map<String, Object> problem = problem(409, "Conflict", "A tárgyaló ebben az idősávban már foglalt.");
		problem.put("conflictingBookingId", 3);
		problem.put("conflictingStartTime", "2030-01-07T07:30:00Z");
		problem.put("conflictingEndTime", "2030-01-07T08:30:00Z");
		return problem;
	}

	private static Map<String, Object> problem(int status, String title, String detail) {
		return object("type", "about:blank", "title", title, "status", status, "detail", detail);
	}

	/** An ordered map, so that the generated examples are the same on every start. */
	private static Map<String, Object> object(Object... keysAndValues) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return map;
	}

}
