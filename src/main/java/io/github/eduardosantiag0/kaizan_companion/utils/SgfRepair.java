package io.github.eduardosantiag0.kaizan_companion.utils;

import io.github.eduardosantiag0.sgf.parser.SgfParseException;
import lombok.experimental.UtilityClass;

import java.util.Map;
@UtilityClass
public class SgfRepair {

    private static final Map<String, String> REPLACEMENTS = Map.of(
            "CoPyright[", "CP["
    );

    public static String repair(SgfParseException e, String sgf) {

        String errorMessage = e.getMessage();

        if (errorMessage == null) {
            throw e;
        }

        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {

            String invalid = entry.getKey();
            String replacement = entry.getValue();

            if (errorMessage.contains(
                    invalid.substring(0, invalid.length() - 1))
                    && sgf.contains(invalid)) {

                return sgf.replace(invalid, replacement);
            }
        }

        throw e;
    }
}
