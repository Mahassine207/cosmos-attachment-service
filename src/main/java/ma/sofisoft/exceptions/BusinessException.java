package ma.sofisoft.exceptions;

import lombok.Getter;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;
    private final Map<String, Object> params;

    public BusinessException(String message, String errorCode, int statusCode) {
        this(message, errorCode, statusCode, null);
    }

    public BusinessException(String message, String errorCode, int statusCode, Map<String, Object> params) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.params = params;
    }
}