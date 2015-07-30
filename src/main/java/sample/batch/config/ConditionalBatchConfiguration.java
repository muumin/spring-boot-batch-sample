package sample.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class ConditionalBatchConfiguration {

    @Bean
    public Job conditionalJob(JobBuilderFactory jobs, Step conditionalStep1, Step conditionalStep2, Step conditionalStep3, Step conditionalStep4, Step conditionalStep5) throws Exception {
        return jobs.get("conditionalJob")
                .incrementer(new RunIdIncrementer())
                .flow(conditionalStep1).on(ExitStatus.FAILED.getExitCode()).to(conditionalStep3)
                .from(conditionalStep1).on("HOGE").to(conditionalStep4)
                .from(conditionalStep1).on("*").to(conditionalStep2)
                .from(conditionalStep4).next(conditionalStep5).on("*").stop()
                .end()
                .build();
    }

    @Bean
    public Step conditionalStep1(StepBuilderFactory steps) {
        return steps.get("conditionalStep1")
                .tasklet((contribution, chunkContext) -> {
                    Object fail = chunkContext.getStepContext().getJobParameters().get("fail");
                    if (fail instanceof String) {
                        switch ((String) fail) {
                            case "1":
                                contribution.setExitStatus(ExitStatus.FAILED);
                                break;
                            case "2":
                                contribution.setExitStatus(new ExitStatus("HOGE"));
                                break;
                        }
                    }
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step conditionalStep2(StepBuilderFactory steps) {
        return steps.get("conditionalStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("conditionalStep2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step conditionalStep3(StepBuilderFactory steps) {
        return steps.get("conditionalStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("conditionalStep3");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step conditionalStep4(StepBuilderFactory steps) {
        return steps.get("conditionalStep4")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("conditionalStep4");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step conditionalStep5(StepBuilderFactory steps) {
        return steps.get("conditionalStep5")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("conditionalStep5");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
