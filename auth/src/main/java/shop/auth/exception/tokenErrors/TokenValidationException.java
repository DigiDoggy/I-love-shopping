package shop.auth.exception.tokenErrors;

import shop.auth.enums.TokenError;

public class TokenValidationException extends RuntimeException {
    private final TokenError errorType;

    public TokenValidationException(TokenError errorType) {
        super(errorType.name());
        this.errorType = errorType;
    }

    public TokenError getError() {
        return errorType;
    }
}
