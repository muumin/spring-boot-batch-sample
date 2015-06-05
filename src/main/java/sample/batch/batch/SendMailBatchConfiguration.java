package sample.batch.batch;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.mail.SimpleMailMessageItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import sample.batch.domain.Person;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SendMailBatchConfiguration {
    @Bean(name = "csvItemReader")
    public ItemReader<Person> csvItemReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"firstName", "lastName", "mail"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});

        return reader;
    }

    @Bean(name = "jpaItemReader")
    public ItemReader<Person> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Person> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("select emp from Person emp");

        return reader;
    }

    @Bean(name = "simpleEmailWriter")
    public ItemWriter<SimpleMailMessage> simpleEmailWriter(MailSender javaMailSender) {
        SimpleMailMessageItemWriter writer = new SimpleMailMessageItemWriter();
        writer.setMailSender(javaMailSender);

        return writer;
    }

    @Bean(name = "jpaItemWriter")
    public ItemWriter<Person> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Person> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);

        return writer;
    }

    @Bean(name = "sendMailJob")
    public Job sendMailJob(JobBuilderFactory jobs, Step sendMailStep, Step insertDataStep) throws Exception {
        return jobs.get("sendMailJob")
                .incrementer(new RunIdIncrementer())
                .start(insertDataStep)
                .next(sendMailStep)
                .build();
    }

    @Bean(name = "insertDataStep")
    public Step insertDataStep(StepBuilderFactory steps,
                               ItemReader<Person> csvItemReader,
                               ItemWriter<Person> jpaItemWriter) throws Exception {
        return steps.get("insertDataStep")
                .<Person, Person>chunk(10)
                .reader(csvItemReader)
                .writer(jpaItemWriter)
                .build();
    }

    @Bean(name = "sendMailStep")
    public Step sendMailStep(StepBuilderFactory steps,
                             ItemReader<Person> jpaItemReader,
                             ItemProcessor<Person, SimpleMailMessage> sendMailProcessor,
                             ItemWriter<SimpleMailMessage> simpleEmailWriter) throws Exception {
        return steps.get("sendMailStep")
                .<Person, SimpleMailMessage>chunk(10)
                .reader(jpaItemReader)
                .processor(sendMailProcessor)
                .writer(simpleEmailWriter)
                .build();
    }
}
