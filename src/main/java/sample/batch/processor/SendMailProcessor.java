package sample.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import sample.domain.model.Person;
import sample.util.VelocityEngineSupport;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SendMailProcessor implements ItemProcessor<Person, SimpleMailMessage> {
    @Autowired
    private VelocityEngineSupport velocityEngineSupport;

    @Value("${spring.mail.properties.from}")
    private String from;

    @Override
    public SimpleMailMessage process(Person item) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();

        log.debug("{} {} email:{}", item.getFirstName(), item.getLastName(), item.getMail());

        message.setFrom(from);
        message.setTo(item.getMail());
        message.setSubject("Welcome!");

        Map<String, Object> map = new HashMap<>();
        map.put("person", item);
        String body = velocityEngineSupport.mergeTemplate("testTemplate1", map);
        message.setText(body);

        return message;
    }
}
