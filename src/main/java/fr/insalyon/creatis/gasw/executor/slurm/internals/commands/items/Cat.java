package fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;

public class Cat extends RemoteCommand {

    public Cat(final String file) {
        super("cat " + file);
    }
    
    public String result() {
        return getOutput().getStdout().getContent();
    }
}
