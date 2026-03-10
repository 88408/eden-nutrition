package eden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Eden营养品商城启动类
 */
@SpringBootApplication(scanBasePackages = "eden")
@EnableScheduling
public class EdenApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdenApplication.class, args);
        System.out.println("============================================");
        System.out.println("    Eden Nutrition Mall Started!");
        System.out.println("    伊甸滋补线上商城启动成功！");
        System.out.println("============================================");
        System.out.println("    API文档：http://localhost:8080/doc.html");
        System.out.println("============================================");
    }
}
