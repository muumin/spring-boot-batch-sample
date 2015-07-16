package sample.batch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.args4j.Option;

import java.util.Optional;

@Setter
@ToString
public class CommandLineOptions {
    enum Mode {
        DAY, WEEK
    }

    @Option(name = "-m", aliases = "--mode", required = false, metaVar = "<mode>", usage = "モード")
    private Mode mode;

    @Option(name = "-id", aliases = "--id", required = false, metaVar = "<id>", usage = "再実行するJobExecutionId")
    private Long id;

    @Option(name = "-restart", aliases = "--restart", required = false, usage = "再実行")
    @Getter
    private boolean restart;

    public Optional<Mode> getMode() {
        return Optional.ofNullable(mode);
    }
    public Optional<Long> getId() {
        return Optional.ofNullable(id);
    }
}
