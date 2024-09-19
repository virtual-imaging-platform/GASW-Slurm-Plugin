package fr.insalyon.creatis.gasw.executor.slurm.internals;

import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Mkdir;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Rm;

public class Main {
    
    public static void main(String[] args) {
        String command = "sinfo";
        RemoteConfiguration config = new RemoteConfiguration("192.168.122.152", 22, "almalinux", "ethaniel");
        try {
            RemoteCommand commandRemote = new Mkdir("migouel", "").execute(config);
            System.err.println(commandRemote.failed());

            RemoteCommand commandRemote2 = new Rm("migouel", "-d").execute(config);
            System.err.println(commandRemote2.failed());
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
