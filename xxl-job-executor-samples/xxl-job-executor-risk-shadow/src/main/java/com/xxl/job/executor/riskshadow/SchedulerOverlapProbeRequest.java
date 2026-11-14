package com.xxl.job.executor.riskshadow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.regex.Pattern;

public record SchedulerOverlapProbeRequest(String probeId, int sleepSeconds) {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern SAFE_PROBE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    public static SchedulerOverlapProbeRequest parse(String raw) {
        String input = raw == null || raw.isBlank() ? "{}" : raw;
        final JsonNode object;
        try {
            object = JSON.readTree(input);
        } catch (Exception error) {
            throw new IllegalArgumentException("probe parameter must be valid JSON", error);
        }
        if (object == null || !object.isObject()) {
            throw new IllegalArgumentException("probe parameter must be a JSON object");
        }
        Iterator<String> fields = object.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            if (!"probe_id".equals(field) && !"sleep_seconds".equals(field)) {
                throw new IllegalArgumentException("unsupported field: " + field);
            }
        }

        String probeId = object.path("probe_id").asText("");
        if (!SAFE_PROBE_ID.matcher(probeId).matches()) {
            throw new IllegalArgumentException("probe_id must be 1-64 safe characters");
        }
        JsonNode sleepValue = object.get("sleep_seconds");
        if (sleepValue == null || !sleepValue.isIntegralNumber()) {
            throw new IllegalArgumentException("sleep_seconds must be an integer");
        }
        int sleepSeconds = sleepValue.asInt();
        if (sleepSeconds < 1 || sleepSeconds > 120) {
            throw new IllegalArgumentException("sleep_seconds must be between 1 and 120");
        }
        return new SchedulerOverlapProbeRequest(probeId, sleepSeconds);
    }
}
