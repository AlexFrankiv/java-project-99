package hexlet.code.app.exceptions;

import java.util.Map;

public record ValidationErrorResponse(String message, Map<String, String> errors) {
}
