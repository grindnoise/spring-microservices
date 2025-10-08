package com.evilcorp.api.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserIdExtractor {

    // This pattern is commonly used to extract the last segment of a path or URL.
    // .*/ - Matches everything up to and including the last slash
    // ([^/]+) - Captures everything after the last slash until the next slash (or end of string)
    public final String REGEX_GET_SUBSTRING_AFTER_LAST_SLASH = ".*/([^/+])";

    public String extractUserId(String path) {
        return path.replaceAll(REGEX_GET_SUBSTRING_AFTER_LAST_SLASH, "$1");
    }
}
