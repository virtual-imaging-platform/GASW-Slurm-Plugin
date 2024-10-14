package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Rm extends RemoteCommand {

    public Rm (final String elementToDelete, final String options) {
        super("rm " + options + " " + elementToDelete);
    }

    public String result() {
        return "";
    }
}
