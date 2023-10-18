package rabbit.flt.plugins.springmvc.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/base")
public class Controller1 {

    @GetMapping("/valueGet")
    public void valueGet() {

    }

    @GetMapping(name = "/nameGet")
    public void nameGet() {

    }

    @GetMapping(path = "/pathGet")
    public void pathGet() {

    }
}
