package fr.insalyon.creatis.gasw.executor.slurm.internals;

import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Credentials;

public class Main {
    
    public static void main(String[] args) {
        String command = "sinfo";
        Credentials config = new Credentials();
        config.setHost("192.168.122.152");
        config.setPort(22);
        config.setUsername("almalinux");
        config.setPassword("ethaniel");
        config.setWorkingDir("/home/almalinux/slurm-working-dir");

        try {
            // RemoteTerminal rt = new RemoteTerminal(config);
            // rt.connect();

            // rt.upload("./oui", "/home/almalinux/migouel.txt");
            // rt.download("/home/almalinux/oui", "./");
            // rt.disconnect();
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
