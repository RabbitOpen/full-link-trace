package rabbit.flt.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;

@MapperScan(basePackages = {"rabbit.flt.test.common.mybatis"})
@SpringBootApplication(exclude = {CassandraAutoConfiguration.class},
        scanBasePackages = {"rabbit.flt.test.common", "rabbit.flt.test.webflux"})
public class SpringBootEntry {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootEntry.class);
    }
}
