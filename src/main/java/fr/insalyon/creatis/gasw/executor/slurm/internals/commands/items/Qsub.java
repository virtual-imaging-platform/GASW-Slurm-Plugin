package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Qsub extends RemoteCommand {

    public Qsub(String batchFile) {
        super("qsub " + batchFile);
    }
    
    public String result() {
        String[] line = getOutput().getStdout().getRow(0);
    
        return line[line.length - 1];
    }
}
