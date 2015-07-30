package sample.batch.config;

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
import sample.domain.model.Person;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SendMailBatchConfiguration {
    @Bean
    public JobParametersValidator sendMailJobParametersValidator() {
        return parameters -> {
            if (parameters.getLong("time") == 0) {
                throw new JobParametersInvalidException("time parameter is not found.");
            }
        };
    }

    @Bean
    public Job sendMailJob(JobBuilderFactory jobs, Step sendMailStep, Step insertDataStep, Step taskletlStep, JobParametersValidator sendMailJobParametersValidator) throws Exception {
        return jobs.get("sendMailJob")
                .validator(sendMailJobParametersValidator)
                .incrementer(new RunIdIncrementer())
                .start(taskletlStep)
                .next(insertDataStep)
                .next(sendMailStep)
                .build();
    }

    @Bean
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

    @Bean
    public Step sendMailStep(StepBuilderFactory steps,
                             ItemReader<Person> jpaItemReader,
//                             ItemReader<Person> jdbcPagingItemReader,
                             ItemProcessor<Person, SimpleMailMessage> sendMailProcessor,
                             ItemWriter<SimpleMailMessage> simpleEmailWriter) throws Exception {
        return steps.get("sendMailStep")
                .<Person, SimpleMailMessage>chunk(10)
                .reader(jpaItemReader)
                .processor(sendMailProcessor)
                .writer(simpleEmailWriter)
                .build();
    }

    @Bean
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

    @Bean
    public Step taskletlStep(StepBuilderFactory steps, StepExecutionListener taskletlStepListener) {
        return steps.get("taskletlStep")
                .listener(taskletlStepListener)
                .tasklet((contribution, chunkContext) -> {
                    log.debug("Initialize tasklet step!!");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
