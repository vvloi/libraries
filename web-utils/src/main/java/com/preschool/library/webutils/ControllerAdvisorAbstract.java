package com.preschool.library.webutils;

import com.preschool.library.webutils.response.Response;
import jakarta.ws.rs.NotAuthorizedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
public abstract class ControllerAdvisorAbstract extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Response<Void>> handleLoginUnsuccessful(
            NotAuthorizedException notAuthorizedException, WebRequest request) {
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        log.error("Not authorized exception");
        return ResponseEntity.status(unauthorized).body((Response.error(unauthorized.name(), null)));
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<Map<String, String>> fieldsErrors = fieldsErrors(ex);
        log.error("ARGUMENT_NOT_VALID: {}", fieldsErrors);
        return new ResponseEntity<>(
                Response.error("ARGUMENT_NOT_VALID", "Some argument fields not valid", fieldsErrors(ex)),
                HttpStatus.BAD_REQUEST);
    }

    private List<Map<String, String>> fieldsErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(
                        fieldError -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("field", fieldError.getField());
                            map.put("message", fieldError.getDefaultMessage());
                            return map;
                        })
                .collect(Collectors.toList());
    }
}
