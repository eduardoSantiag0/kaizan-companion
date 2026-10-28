package io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.dtos;

public record DgsSgfResponseDTO(String content, boolean error, String errorMessage) {

    private static final String ERROR_MARKER = "[#Error:";

    public static DgsSgfResponseDTO from(String rawResponse) {
        if (rawResponse == null) {
            return new DgsSgfResponseDTO(null, true, null);
        }

        String trimmed = rawResponse.strip();

        if (trimmed.startsWith(ERROR_MARKER)) {
            return new DgsSgfResponseDTO(null, true, trimmed);
        }

        return new DgsSgfResponseDTO(rawResponse, false, null);
    }
}
