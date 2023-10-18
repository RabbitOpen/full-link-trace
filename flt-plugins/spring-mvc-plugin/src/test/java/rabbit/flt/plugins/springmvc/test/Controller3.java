package rabbit.flt.plugins.springmvc.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign")
public class Controller3 implements FeignInterface {

    @Override
    public void valuePost() {

    }

    @Override
    public void namePost() {

    }

    @Override
    public void pathPost() {

    }
}
