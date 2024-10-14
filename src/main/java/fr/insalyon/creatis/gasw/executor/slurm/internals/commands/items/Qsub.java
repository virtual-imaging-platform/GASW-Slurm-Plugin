package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Qsub extends RemoteCommand {

    public Qsub(final String batchFile) {
        super("qsub " + batchFile);
    }
    
    public String result() {
        final String[] line = getOutput().getStdout().getRow(0);
    
        return line[line.length - 1];
    }
}
