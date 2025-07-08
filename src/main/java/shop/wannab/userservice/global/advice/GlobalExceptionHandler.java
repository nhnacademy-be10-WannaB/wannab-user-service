package shop.wannab.userservice.global.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.user.domain.dto.ErrorResponse;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    public Response<Throwable> handleException(RuntimeException e) {

        return new Response<>(e.getCause(), null, e.getMessage());
    }


}
