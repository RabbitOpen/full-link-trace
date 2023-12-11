package rabbit.flt.test.mvc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import rabbit.flt.common.exception.FltException;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ExceptionAdvisor {

    @ExceptionHandler(FltException.class)
    @ResponseBody
    public String onException(FltException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error";
    }
}
