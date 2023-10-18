package rabbit.flt.plugins.springmvc.test;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller2 {

    @PostMapping("/valuePost")
    public void valuePost() {

    }

    @PostMapping(name = "/namePost")
    public void namePost() {

    }

    @PostMapping(path = "/pathPost")
    public void pathPost() {

    }
}
