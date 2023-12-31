package rabbit.flt.test.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rabbit.flt.common.exception.FltException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mvc")
public class BootController {

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("abc");
    }

    @GetMapping("/hello1")
    public Mono<String> hello1() {
        return Mono.just("abc");
    }

    @GetMapping("/error")
    public void error() {
        throw new FltException("hello");
    }

    /**
     * 未处理的异常
     */
    @GetMapping("/unHandledError")
    public String unHandledError() {
        throw new RuntimeException("hello");
    }
}
