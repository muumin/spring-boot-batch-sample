package sample;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.args4j.Option;

import java.util.Optional;

@Setter
@ToString
public class CommandLineOptions {
    enum JobName {
        sendMailJob, conditionalJob
    }

    @Option(name = "-job", aliases = "--job", required = true, metaVar = "<job>", usage = "実行するJob")
    @Getter
    private JobName jobName;

    @Option(name = "-id", aliases = "--id", required = false, metaVar = "<id>", usage = "再実行するJobExecutionId")
    private Long id;

    @Option(name = "-restart", aliases = "--restart", required = false, usage = "再実行")
    @Getter
    private boolean restart;

    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }
}
