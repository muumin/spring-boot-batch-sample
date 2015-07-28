package sample

import org.kohsuke.args4j.CmdLineException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.FailsWith

class SampleBatchApplicationSpec extends BaseSpecification {
    @Autowired
    SampleBatchApplication sampleBatchApplication

    @FailsWith(CmdLineException)
    def "Run"() {
        expect:
        sampleBatchApplication.run("start")
//        sampleBatchApplication.run("-restart") // command line option.
    }
}
