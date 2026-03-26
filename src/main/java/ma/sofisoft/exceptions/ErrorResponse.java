package ma.sofisoft.exceptions;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

// ErrorResponse is the standard error body returned to the client on every error.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String, Object> params;

    public static ErrorResponse of(int status, String errorCode, String message, String path, Map<String, Object> params) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .params(params)
                .build();
    }

    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return of(status, errorCode, message, path, null);
    }
}
