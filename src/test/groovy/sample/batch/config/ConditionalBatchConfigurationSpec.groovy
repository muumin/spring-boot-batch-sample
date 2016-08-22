package sample.batch.config

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import sample.BaseSpecification

class ConditionalBatchConfigurationSpec extends BaseSpecification {
    @Autowired
    private JobLauncher jobLauncher

    @Autowired
    private Job conditionalJob

    def "Normal"() {
        when:
        JobExecution jobExecution = jobLauncher.run(conditionalJob, getJobParameters())

        then:
        jobExecution.getStatus() == BatchStatus.COMPLETED

        and:
        def list = []
        jobExecution.getStepExecutions().each { list << it.getStepName() }
        list == ['conditionalStep1', 'conditionalStep2']
    }

    def "Fail1"() {
        setup:
        def m = ["fail" : new JobParameter("1")]

        when:
        JobExecution jobExecution = jobLauncher.run(conditionalJob, getJobParameters(m))

        then:
        jobExecution.getStatus() == BatchStatus.COMPLETED

        and:
        def list = []
        jobExecution.getStepExecutions().each { list << it.getStepName() }
        list == ['conditionalStep1', 'conditionalStep3']
    }

    def "Fail2"() {
        setup:
        def m = ["fail" : new JobParameter("2")]

        when:
        JobExecution jobExecution = jobLauncher.run(conditionalJob, getJobParameters(m))

        then:
        jobExecution.getStatus() == BatchStatus.STOPPED

        and:
        def list = []
        jobExecution.getStepExecutions().each { list << it.getStepName() }
        list == ['conditionalStep1', 'conditionalStep4', 'conditionalStep5']
    }
}
