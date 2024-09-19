package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Rm extends RemoteCommand {

    public Rm (String elementToDelete, String options) {
        super("rm " + options + " " + elementToDelete);
    }

    public String result() {
        return "";
    }
}
