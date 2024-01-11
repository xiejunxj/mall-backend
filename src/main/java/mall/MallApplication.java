package mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MallApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MallApplication.class);
        app.run(args);
    }
}