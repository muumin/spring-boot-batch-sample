package sample.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import sample.domain.model.Person;

@Component
@Slf4j
public class PersonValidationProcessor implements ItemProcessor<Person, Person> {
    @Autowired
    private Validator validator;

    @Override
    public Person process(Person item) throws Exception {
        DataBinder binder = new DataBinder(item);
        binder.setValidator(validator);
        binder.validate();
        BindingResult result = binder.getBindingResult();

        if (result.hasErrors()) {
            log.warn(item.toString());
            result.getAllErrors().forEach(s -> log.warn(s.toString()));
            return null; // ItemProcessorでnullを返すと後続のWriterで処理されない
        }

        return item;
    }
}
