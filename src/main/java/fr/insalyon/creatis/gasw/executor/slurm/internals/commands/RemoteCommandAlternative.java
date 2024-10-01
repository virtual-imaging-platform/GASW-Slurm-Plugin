package fr.insalyon.creatis.gasw.executor.slurm.internals.commands;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RemoteCommandAlternative {
    
    final protected String      data;
    final protected boolean     evaluator;

    public abstract RemoteCommand getCommand();
}
