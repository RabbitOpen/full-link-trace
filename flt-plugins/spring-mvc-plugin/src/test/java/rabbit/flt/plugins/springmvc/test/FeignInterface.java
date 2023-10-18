package rabbit.flt.plugins.springmvc.test;

import org.springframework.web.bind.annotation.PostMapping;

public interface FeignInterface {

    @PostMapping("/valuePost")
    void valuePost();

    @PostMapping(name = "/namePost")
    void namePost();

    @PostMapping(path = "/pathPost")
    void pathPost();
}
