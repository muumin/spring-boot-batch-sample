package sample.batch.batch.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import sample.batch.domain.Person;
import sample.batch.util.VelocityEngineSupport;

import java.util.HashMap;
import java.util.Map;

@Component
public class SendMailProcessor implements ItemProcessor<Person, SimpleMailMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(SendMailProcessor.class);

    @Autowired
    private VelocityEngineSupport velocityEngineSupport;

    @Value("${java.mail.from}")
    private String from;

    @Override
    public SimpleMailMessage process(Person item) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();

        LOGGER.debug("{} {} email:{}", item.getFirstName(), item.getLastName(), item.getMail());

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
