package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Sbatch extends RemoteCommand {
    
    public Sbatch(String batchFile) {
        super("sbatch --parsable " + batchFile);
    }

    public String result() {
        return "";
    }
}
