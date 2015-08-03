package sample;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotFailedException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandLineBatch implements CommandLineRunner {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private Collection<Job> jobs = Collections.emptySet();

    @Override
    public void run(String... args) throws Exception {
        Optional<CommandLineOptions> option = getOptions(args);
        if (!option.isPresent()) return;

        Optional<Job> job = jobs.stream().filter(s -> s.getName().equals(option.get().getJobName().name())).findFirst();

        // リスタートなら指定されたJobか未指定なら最新のJOBのパラメータを取得する
        Optional<JobParameters> jobParameters = option.get().isRestart() ? getJobParameter(job.get(), option.get()) : createInitialJobParameterMap();
        if (!jobParameters.isPresent()) {
            throw new JobExecutionNotFailedException("No failed or stopped execution found for job=" + job.get().getName());
        }

        JobExecution jobExecution = jobLauncher.run(job.get(), jobParameters.get());
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

    private Optional<CommandLineOptions> getOptions(String... args) throws CmdLineException {
        CommandLineOptions option = new CommandLineOptions();
        CmdLineParser parser = new CmdLineParser(option);

        if (args.length == 0) {
            parser.printUsage(System.err);
            return Optional.empty();
        }

        try {
            parser.parseArgument(args);
            return Optional.of(option);
        } catch (CmdLineException ex) {
            log.error("Exception: {}", ex.getMessage());
            parser.printUsage(System.err);
            throw ex;
        }
    }

    private Optional<JobParameters> createInitialJobParameterMap() {
        Map<String, JobParameter> m = new HashMap<>();
        // 同じパラメーターのバッチは実行されないのでミリ秒で再実行
        m.put("time", new JobParameter(System.currentTimeMillis()));
        return Optional.ofNullable(new JobParameters(m));
    }

    private Optional<JobParameters> getJobParameter(Job job, CommandLineOptions option) {
        List<JobExecution> list = getJobExecutionsWithStatusGreaterThan(job.getName(), option, BatchStatus.STOPPED);
        if (list.isEmpty()) {
            return Optional.empty();
        }

        JobExecution jobExecution = list.stream().sorted((s1, s2) -> (int) (s2.getId() - s1.getId())).findFirst().get();
        log.debug("jobExecution: JobExecutionId={}, Status={}", jobExecution.getId(), jobExecution.getStatus());
        return Optional.ofNullable(jobExecution.getJobParameters());
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
