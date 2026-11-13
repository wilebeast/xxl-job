package com.xxl.job.executor.riskshadow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

public record OfflineRiskShadowRequest(Instant asOf) {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static OfflineRiskShadowRequest parse(String raw, Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("clock is required");
        }
        String input = raw == null || raw.isBlank() ? "{}" : raw;
        final JsonNode object;
        try {
            object = JSON.readTree(input);
        } catch (Exception error) {
            throw new IllegalArgumentException("job parameter must be valid JSON", error);
        }
        if (object == null || !object.isObject()) {
            throw new IllegalArgumentException("job parameter must be a JSON object");
        }
        Iterator<String> fields = object.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            if (!"as_of".equals(field)) {
                throw new IllegalArgumentException("unsupported field: " + field);
            }
        }

        JsonNode asOfValue = object.get("as_of");
        if (asOfValue == null || asOfValue.isNull() || asOfValue.asText().isBlank()) {
            return new OfflineRiskShadowRequest(clock.instant().truncatedTo(ChronoUnit.HOURS));
        }
        final Instant asOf;
        try {
            asOf = Instant.parse(asOfValue.asText());
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("as_of must use RFC3339 UTC format", error);
        }
        if (!asOf.equals(asOf.truncatedTo(ChronoUnit.HOURS))) {
            throw new IllegalArgumentException("as_of must be aligned to an hour boundary");
        }
        return new OfflineRiskShadowRequest(asOf);
    }
}
