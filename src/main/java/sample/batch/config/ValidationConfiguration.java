package sample.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import sample.domain.model.Person;

@Configuration
@Slf4j
public class ValidationConfiguration {
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ItemProcessor<Person, Person> validatingItemProcessor(Validator validator) {
        SpringValidator<Person> springValidator = new SpringValidator<>();
        springValidator.setValidator(validator);
        ValidatingItemProcessor<Person> ValidatingItemProcessor = new ValidatingItemProcessor<>();
        ValidatingItemProcessor.setValidator(springValidator);

        return ValidatingItemProcessor;
    }
}
