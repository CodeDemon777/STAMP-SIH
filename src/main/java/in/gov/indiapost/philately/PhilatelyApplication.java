package in.gov.indiapost.philately;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class PhilatelyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhilatelyApplication.class, args);
    }
}

