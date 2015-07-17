package sample;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotFailedException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class SampleBatchApplication implements CommandLineRunner {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private Job sendMailJob;

    public static void main(String[] args) {
        try {
            System.exit(SpringApplication.exit(SpringApplication.run(SampleBatchApplication.class, args)));
        } catch (Exception ex) {
            log.error("critical error!!", ex);
            System.out.println("致命的なエラーが発生しました。詳細はログを確認して下さい。");
            System.exit(1);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLineOptions option = getOptions(args);
        option.getMode().ifPresent(s -> log.debug("Command line option = {}", s));
        log.debug("Command line option = {}", option.isRestart());

        Optional<JobParameters> jobParameters = option.isRestart() ? getJobParameter(sendMailJob, option) : createInitialJobParameterMap();
        if (!jobParameters.isPresent()) {
            throw new JobExecutionNotFailedException("No failed or stopped execution found for job=" + sendMailJob.getName());
        }

        JobExecution jobExecution = jobLauncher.run(sendMailJob, jobParameters.get());
        executionTimeLog(jobExecution);
    }

    private void executionTimeLog(JobExecution jobExecution) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("Execution time {}(JobExecutionId:{} status:{}) in {} ms",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobInstance().getId(),
                jobExecution.getStatus(),
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
            parser.printUsage(System.err);
            throw ex;
        }
    }

    private Optional<JobParameters> getJobParameter(Job job, CommandLineOptions option) {
        List<JobExecution> list = getJobExecutionsWithStatusGreaterThan(job.getName(), option, BatchStatus.STOPPED);
        if (list.isEmpty()) {
            return Optional.empty();
        }

        JobExecution jobExecution = list.stream().sorted((s1, s2) -> (int) (s2.getId() - s1.getId())).collect(Collectors.toList()).get(0);
        log.debug("jobExecution: JobExecutionId={}, Status={}", jobExecution.getId(), jobExecution.getStatus());
        return Optional.ofNullable(jobExecution.getJobParameters());
    }

    private Optional<JobParameters> createInitialJobParameterMap() {
        Map<String, JobParameter> m = new HashMap<>();
        // 同じパラメーターのバッチは実行されないのでミリ秒で再実行
        m.put("time", new JobParameter(System.currentTimeMillis()));
        return Optional.ofNullable(new JobParameters(m));
    }

    private List<JobExecution> getJobExecutionsWithStatusGreaterThan(String jobName, CommandLineOptions option, BatchStatus minStatus) {
        return option.getId().map(jobExplorer::getJobExecution)
                .map(s -> s.getStatus().isGreaterThan(minStatus) ? Collections.singletonList(s) : null)
                .orElse(getJobExecutionsWithStatusGreaterThan(jobName, minStatus));
    }

    private List<JobExecution> getJobExecutionsWithStatusGreaterThan(String jobName, BatchStatus minStatus) {
        int start = 0;
        int count = 100;
        List<JobExecution> executions = new ArrayList<>();
        List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobName, start, count);

        while (!lastInstances.isEmpty()) {
            lastInstances.stream().forEach(s ->
                    Optional.ofNullable(jobExplorer.getJobExecutions(s)).ifPresent(t ->
                                    executions.addAll(t.stream().filter(u -> u.getStatus().isGreaterThan(minStatus)).collect(Collectors.toList()))
                    ));
            start += count;
            lastInstances = jobExplorer.getJobInstances(jobName, start, count);
        }

        return executions;

    }
}
