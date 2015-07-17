package sample

import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [SampleBatchApplication], loader = SpringApplicationContextLoader)
@ActiveProfiles("integration")
abstract class BaseSpecification extends Specification {
}
