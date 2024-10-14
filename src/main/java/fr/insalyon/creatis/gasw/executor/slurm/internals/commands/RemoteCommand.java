package fr.insalyon.creatis.gasw.executor.slurm.internals.commands;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteOutput;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteTerminal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RemoteCommand {
    
    final private String    command;

    @Getter
    private RemoteOutput    output;

    public RemoteCommand execute(final Config config) throws GaswException {
        output = RemoteTerminal.oneCommand(config, command);
        return this;
    }

    public boolean failed() {
        if (output != null) {
            return (output.getExitCode() != 0 || ( ! output.getStderr().getContent().isEmpty()));
        } else {
            return true;
        }
    }

    public abstract String result();
}
