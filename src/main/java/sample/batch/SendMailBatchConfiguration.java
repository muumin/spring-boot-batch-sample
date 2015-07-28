package sample.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import sample.domain.Person;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SendMailBatchConfiguration {
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

    @Bean(name = "sendMailJobParametersValidator")
    public JobParametersValidator sendMailJobParametersValidator() {
        return parameters -> {
            if (parameters.getLong("time") == 0) {
                throw new JobParametersInvalidException("time parameter is not found.");
            }
        };
    }

    @Bean(name = "sendMailJob")
    public Job sendMailJob(JobBuilderFactory jobs, Step sendMailStep, Step insertDataStep, Step taskletlStep, JobParametersValidator sendMailJobParametersValidator) throws Exception {
        return jobs.get("sendMailJob")
                .validator(sendMailJobParametersValidator)
                .incrementer(new RunIdIncrementer())
                .start(taskletlStep)
                .next(insertDataStep)
                .next(sendMailStep)
                .build();
    }

    @Bean(name = "insertDataStep")
    public Step insertDataStep(StepBuilderFactory steps,
                               ItemReader<Person> csvItemReader,
                               ItemProcessor<Person, Person> personValidationProcessor,
//                               ItemProcessor<Person, Person> validatingItemProcessor, // ValidationExceptionでバッチ処理が中断するItemProcessor
                               ItemWriter<Person> jpaItemWriter) throws Exception {
        return steps.get("insertDataStep")
                .<Person, Person>chunk(10)
                .reader(csvItemReader)
                .processor(personValidationProcessor)
//                .processor(validatingItemProcessor)
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

    @Bean(name = "taskletlStepListener")
    public StepExecutionListener taskletlStepListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.debug("taskletlStepListener beforeStep");
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.debug("taskletlStepListener afterStep");
                return null;
            }
        };
    }

    @Bean(name = "taskletlStep")
    public Step taskletlStep(StepBuilderFactory steps, StepExecutionListener taskletlStepListener) {
        return steps.get("taskletlStep")
                .listener(taskletlStepListener)
                .tasklet((contribution, chunkContext) -> {
                    log.debug("Initialize tasklet step!!");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
