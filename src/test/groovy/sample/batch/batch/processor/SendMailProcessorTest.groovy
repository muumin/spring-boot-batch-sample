package sample.batch.batch.processor

import sample.batch.domain.Person
import sample.batch.util.VelocityEngineSupport
import spock.lang.Specification
import spock.lang.Unroll

class SendMailProcessorTest extends Specification {
    @Unroll
    def "メールに「#firstName #lastName」が設定されていること"() {
        setup:
        def person = new Person()
        person.setFirstName(firstName)
        person.setLastName(lastName)
        person.setMail(email)

        and:
        def processor = new SendMailProcessor()
        processor.from = "test@example"
        processor.velocityEngineSupport = Mock(VelocityEngineSupport) {
            1 * mergeTemplate("testTemplate1", ["person": person]) >> "body"
        }

        when:
        def ret = processor.process(person)

        then:
        ret.from == "test@example"
        ret.subject == "Welcome!"
        ret.text == "body"
        ret.to == [email]

        where:
        // @formatter:off
        firstName | lastName | email
        'tarou'   | 'yamada' | 'yamada@example.com'
        'hanako'  | 'takata' | 'takata@example.com'
        // @formatter:on
    }
}
