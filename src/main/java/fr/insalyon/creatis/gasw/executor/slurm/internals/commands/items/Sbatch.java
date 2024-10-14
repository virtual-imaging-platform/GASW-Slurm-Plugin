package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Sbatch extends RemoteCommand {
    
    public Sbatch(final String batchFile) {
        super("sbatch --parsable " + batchFile);
    }

    public String result() {
        final String[] line = getOutput().getStdout().getRow(0);

        return line[line.length - 1];
    }
}
