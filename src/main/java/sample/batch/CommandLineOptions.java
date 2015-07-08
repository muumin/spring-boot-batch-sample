package sample.batch;

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

    public Optional<Mode> getMode() {
        return Optional.ofNullable(mode);
    }
}
