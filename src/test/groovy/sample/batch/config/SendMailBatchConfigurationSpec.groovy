package sample.batch.config

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import sample.BaseSpecification

class SendMailBatchConfigurationSpec extends BaseSpecification {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job sendMailJob;

    def "Normal"() {
        setup:
        Map<String, JobParameter> m = new HashMap<>()
        m.put("time", new JobParameter(System.currentTimeMillis()))
        JobParameters jobParameters = new JobParameters(m)

        when:
        JobExecution jobExecution = jobLauncher.run(sendMailJob, jobParameters)

        then:
        jobExecution.getStatus() == BatchStatus.COMPLETED

    }
}
