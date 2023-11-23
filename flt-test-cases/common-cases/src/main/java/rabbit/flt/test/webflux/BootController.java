package rabbit.flt.test.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mvc")
public class BootController {

    private Logger logger  = LoggerFactory.getLogger(getClass());

    @GetMapping("/hello")
    public Mono<String> hello() {
        logger.info("invoke web-flux hello");
        return Mono.just("abc");
    }

    @GetMapping("/hello1")
    public Mono<String> hello1() {
        return Mono.just("abc");
    }
}
