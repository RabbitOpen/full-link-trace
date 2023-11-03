package rabbit.flt.test.mvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mvc")
public class BootController {

    @GetMapping("/hello")
    public String hello() {
        return "abc";
    }

    @GetMapping("/hello1")
    public String hello1() {
        return "abc";
    }
}
