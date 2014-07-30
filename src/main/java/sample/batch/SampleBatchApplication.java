package sample.batch;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@EnableAutoConfiguration
@ComponentScan
@Configuration
public class SampleBatchApplication {

    @Value("${java.mail.host}")
    private String host;

    @Value("${java.mail.port}")
    private Integer port;

    @Value("${java.mail.username}")
    private String username;

    @Value("${java.mail.password}")
    private String password;

    public static void main(String[] args) throws Exception {
        System.exit(SpringApplication.exit(SpringApplication.run(
                SampleBatchApplication.class, args)));
    }

    @Bean
    public MailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", true);
        javaMailProperties.put("mail.smtp.starttls.enable", true);
        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }

    @Bean
    public ResourceLoader templateLoader(DataSource dataSource) {
        DataSourceResourceLoader loader = new DataSourceResourceLoader();
        loader.setDataSource(dataSource);
        return loader;
    }

    @Bean
    public VelocityEngine velocityDBEngine(ResourceLoader templateLoader) throws IOException {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        velocityEngineFactoryBean.setPreferFileSystemAccess(false);

        Map<String, Object> map = new HashMap<>();
        map.put("resource.loader", "ds");
        map.put("ds.resource.loader.instance", templateLoader);
        map.put("ds.resource.loader.resource.table", "velocity_template");
        map.put("ds.resource.loader.resource.keycolumn", "id");
        map.put("ds.resource.loader.resource.templatecolumn", "def");
        map.put("ds.resource.loader.resource.timestampcolumn", "last_modified");

        velocityEngineFactoryBean.setVelocityPropertiesMap(map);

        return velocityEngineFactoryBean.createVelocityEngine();
    }
}
