package shop.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import shop.auth.exception.tokenErrors.TokenValidationException;

import java.time.LocalDateTime;

@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorDetails> appExceptionHandler(AppException e, WebRequest request) {
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                e.getStatus().value(),
                e.getStatus().getReasonPhrase(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, e.getStatus());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> globalExceptionHandler(Exception e, WebRequest request) {
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorDetails> tokenValidationExceptionHandler(TokenValidationException e,
                                                                        WebRequest request) {

        HttpStatus status;
        String message;

        switch (e.getError()) {
            case TOKEN_BLACKLISTED -> {
                status = HttpStatus.UNAUTHORIZED;
                message = "User is blacklisted.";
            }
            case TOKEN_EXPIRED -> {
                status = HttpStatus.UNAUTHORIZED;
                message = "Token expired.";
            }
            case TOKEN_NOT_FOUND -> {
                status = HttpStatus.BAD_REQUEST;
                message = "Token not found.";
            }
            default -> {
                status = HttpStatus.UNAUTHORIZED;
                message = "Invalid token.";
            }
        }

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getDescription(false)
        );

        return new ResponseEntity<>(error, status);
    }
}
