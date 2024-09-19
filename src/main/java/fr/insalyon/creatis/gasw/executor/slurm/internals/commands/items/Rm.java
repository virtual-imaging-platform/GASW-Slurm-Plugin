package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Rm extends RemoteCommand {

    public Rm (String elementToDelete, String options) {
        super("rm " + options + " " + elementToDelete);
    }

    public boolean failed() {
        if (output.getExitCode() != 0)
            return true;
        if ( ! output.getStderr().getContent().isEmpty())
            return true;
        return false;
    }

    public String result() {
        return "";
    }
}
