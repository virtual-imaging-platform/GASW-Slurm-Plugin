package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Mkdir extends RemoteCommand {

    public Mkdir (String folderToCreate, String options) {
        super("mkdir " + options + " " + folderToCreate);
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
