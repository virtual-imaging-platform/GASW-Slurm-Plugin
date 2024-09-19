package fr.insalyon.creatis.gasw.executor.slurm.internals.commands;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.executor.slurm.internals.RemoteConfiguration;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteOutput;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteTerminal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RemoteCommand {
    
    final private String    command;

    @Getter
    protected RemoteOutput  output;

    public RemoteCommand execute(RemoteConfiguration config) throws GaswException {
        output = RemoteTerminal.oneCommand(config, command);
        return this;
    }

    public abstract boolean failed();

    public abstract String result();
}
