package rabbit.flt.test.webflux;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionAdvisor {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String onException(Exception e) {
        return "error";
    }
}
