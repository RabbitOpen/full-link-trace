package rabbit.flt.test.common;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;

@MapperScan(basePackages = {"rabbit.flt.test.common.mybatis"})
@SpringBootApplication(exclude = {CassandraAutoConfiguration.class})
public class SpringBootEntry {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootEntry.class);
    }
}
