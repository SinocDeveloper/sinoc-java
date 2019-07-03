package org.sinoc;

import org.sinoc.config.SystemProperties;
import org.sinoc.shell.config.EthereumHarmonyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@EnableScheduling
@Import({EthereumHarmonyConfig.class})
public class Application {

    public static void main(String[] args) throws Exception {
        SystemProperties config = SystemProperties.getDefault();
        config.overrideParams("mine.start", "false");
        SpringApplication.run(Application.class, args);
    }
}
