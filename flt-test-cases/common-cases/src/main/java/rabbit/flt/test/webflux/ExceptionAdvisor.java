package rabbit.flt.test.webflux;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import rabbit.flt.common.exception.FltException;

@ControllerAdvice
public class ExceptionAdvisor {

    @ExceptionHandler(FltException.class)
    @ResponseBody
    public String onException(FltException e, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return "error";
    }
}
