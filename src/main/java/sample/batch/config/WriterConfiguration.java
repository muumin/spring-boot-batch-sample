package sample.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.mail.SimpleMailMessageItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import sample.domain.model.Person;

import javax.persistence.EntityManagerFactory;

@Configuration
@Slf4j
public class WriterConfiguration {
    @Bean
    public ItemWriter<SimpleMailMessage> simpleEmailWriter(MailSender javaMailSender) {
        SimpleMailMessageItemWriter writer = new SimpleMailMessageItemWriter();
        writer.setMailSender(javaMailSender);

        return writer;
    }

    @Bean
    public ItemWriter<Person> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Person> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);

        return writer;
    }
}
