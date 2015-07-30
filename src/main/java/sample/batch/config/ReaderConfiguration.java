package sample.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import sample.domain.model.Person;

import javax.persistence.EntityManagerFactory;

@Configuration
@Slf4j
public class ReaderConfiguration {
    private final static String FILE_NAME = "sample-data.csv";

    @Bean(name = "csvItemReader")
    public ItemReader<Person> csvItemReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(FILE_NAME));
//        reader.setResource(new FileSystemResource(FILE_NAME)); // read system file.
//        reader.setLinesToSkip(1); // skip header.
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
//                setDelimiter(DelimitedLineTokenizer.DELIMITER_TAB); // read tvs.
                setNames(new String[]{"firstName", "lastName", "mail"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});

        return reader;
    }

    @Bean(name = "jpaItemReader", destroyMethod = "")
    public ItemReader<Person> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Person> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("select emp from Person emp");

        return reader;
    }
}
