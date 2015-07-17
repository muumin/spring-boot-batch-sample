package sample

import org.springframework.beans.factory.annotation.Autowired

class SampleBatchApplicationTest extends BaseSpecification {
    @Autowired
    SampleBatchApplication sampleBatchApplication

    def "Run"() {
        expect:
        sampleBatchApplication.run()
    }
}
