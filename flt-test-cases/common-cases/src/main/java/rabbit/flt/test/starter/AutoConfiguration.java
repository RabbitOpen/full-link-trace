package rabbit.flt.test.starter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动配置
 */
@Configuration
@Import({ SpringBootInitializer.class })
public class AutoConfiguration {
}
