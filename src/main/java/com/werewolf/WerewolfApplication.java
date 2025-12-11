package com.werewolf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 狼人杀多智能体平台 - 主应用类
 *
 * @author Claude & User
 * @version 0.1.0
 */
@SpringBootApplication
public class WerewolfApplication {

    public static void main(String[] args) {
        SpringApplication.run(WerewolfApplication.class, args);
        System.out.println("""

            ═══════════════════════════════════════════════════════════
            🐺 Werewolf Multi-Agent Platform Started Successfully!
            ═══════════════════════════════════════════════════════════

            Server is running on: http://localhost:8021
            Swagger UI: http://localhost:8021/swagger-ui.html
            H2 Console: http://localhost:8021/h2-console

            ═══════════════════════════════════════════════════════════
            """);
    }
}
