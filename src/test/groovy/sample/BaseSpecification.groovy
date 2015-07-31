package sample

import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [SampleBatchApplication], loader = SpringApplicationContextLoader)
@ActiveProfiles("integration")
abstract class BaseSpecification extends Specification {
    def getJobParameters(Map<String, JobParameter> m = null) {
        if (!m) m = [:]
        m << ["time" : new JobParameter(System.currentTimeMillis())]
        return new JobParameters(m)
    }
}
