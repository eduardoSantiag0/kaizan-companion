package io.github.eduardosantiag0.kaizan_companion.features.analysis.errors;


//! {"id":"test-1ee0f405-f3d6-41d3-bd32-2f92c1f014d8","status":"FAILED","error":"Could not parse player: T}"

public record KatagoErrorMessage(
        String id,
        String status,
        String error
) {
}
