package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.alternatives;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommandAlternative;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Scontrol;

public class RetrieveStatus extends RemoteCommandAlternative {

    public RetrieveStatus(String jobID, boolean evaluator) {
        super(jobID, evaluator);
    }

    public RemoteCommand getCommand() {
        if (evaluator) {
            return new Scontrol(data); // replace for pbs
        } else {
            return new Scontrol(data);
        }
    }
}
