package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Squeue extends RemoteCommand {
    
    public Squeue(String jobID) {
        super("squeue -n " + jobID + " --format '$T'");
    }

    public String result() {
        if (getOutput().getStdout().getLines().length == 1) {
            return null;
        } else {
            return getOutput().getStdout().getRow(1)[0];
        }
    }
}
