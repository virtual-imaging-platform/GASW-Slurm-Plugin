package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.alternatives;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommandAlternative;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Sbatch;

public class SubmitJob extends RemoteCommandAlternative {

    public SubmitJob(String jobFile, boolean evaluator) {
        super(jobFile, evaluator);
    }

    public RemoteCommand getCommand() {
        if (evaluator) {
            return new Sbatch(data); // change with PBS
        } else {
            return new Sbatch(data);
        }
    }
}
