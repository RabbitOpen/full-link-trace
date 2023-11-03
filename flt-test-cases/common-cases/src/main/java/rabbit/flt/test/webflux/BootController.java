package rabbit.flt.test.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
