package sample.batch;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@Slf4j
public class SampleBatchApplication implements CommandLineRunner {
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job sendMailJob;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SampleBatchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLineOptions option = getOptions(args);
        option.getMode().ifPresent(s -> log.debug("Command line option = {}", s));

        JobExecution jobExecution = jobLauncher.run(sendMailJob, createInitialJobParameterMap());
        executionTimeLog(jobExecution);
    }

    private void executionTimeLog(JobExecution jobExecution) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Execution time {} in {} ms", jobExecution.getJobInstance().getJobName(),
                jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime());
        jobExecution.getStepExecutions().forEach(s ->
                        log.debug("Execution time {} in {} ms", s.getStepName(), s.getEndTime().getTime() - s.getStartTime().getTime())
        );
    }

    private CommandLineOptions getOptions(String... args) throws CmdLineException {
        // gradlew bootRun -Pargs="-m DAY"
        CommandLineOptions option = new CommandLineOptions();
        CmdLineParser parser = new CmdLineParser(option);
        try {
            parser.parseArgument(args);
            return option;
        } catch (CmdLineException ex) {
            log.error("Exception: {}", ex.getMessage());
            System.out.println(ex.getMessage());
            parser.printUsage(System.err);
            throw ex;
        }
    }

    private JobParameters createInitialJobParameterMap() {
        Map<String, JobParameter> m = new HashMap<>();
        // 同じパラメーターのバッチは実行されないのでミリ秒で再実行
        m.put("time", new JobParameter(System.currentTimeMillis()));
        return new JobParameters(m);
    }
}
