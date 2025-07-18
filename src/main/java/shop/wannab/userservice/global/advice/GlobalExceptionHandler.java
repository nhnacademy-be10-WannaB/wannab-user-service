package shop.wannab.userservice.global.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.wannab.userservice.global.Response;
import shop.wannab.userservice.point.exception.FeignClientException;
import shop.wannab.userservice.user.domain.dto.response.ErrorResponse;
import shop.wannab.userservice.user.exception.UserAlreadyExistsException;
import shop.wannab.userservice.user.exception.UserNotFoundException;
import shop.wannab.userservice.utils.ResponseCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(RuntimeException e) {
        log.info("Exception: handleUserNotFoundException");
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler({UserAlreadyExistsException.class})
    public Response<Void> handleUserAlreadyExistsException(RuntimeException e) {
        log.info("Exception: handleUserAlreadyExistsException");
        return new Response<>(null, ResponseCode.USER_ALREADY_EXISTS, e.getMessage());
    }

    @ExceptionHandler({FeignClientException.class})
    public Response<Void> feignClientException(RuntimeException e) {
        log.info("Exception: feignClientException");
        return new Response<>(null, ResponseCode.FEIGN_CLIENT_EXCEPTION, e.getMessage());
    }

}
