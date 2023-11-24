package rabbit.flt.test.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rabbit.flt.common.exception.FltException;

@RestController
@RequestMapping("/mvc")
public class BootController {

    private Logger logger  = LoggerFactory.getLogger(getClass());

    @GetMapping("/hello")
    public String hello() {
        logger.info("invoke hello");
        return "abc";
    }

    @GetMapping("/hello1")
    public String hello1() {
        return "abc";
    }

    @GetMapping("/error")
    public void error() {
        throw new FltException("hello");
    }
}
