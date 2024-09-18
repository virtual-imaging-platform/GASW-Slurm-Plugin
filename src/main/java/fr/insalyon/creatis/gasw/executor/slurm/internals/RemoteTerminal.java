package fr.insalyon.creatis.gasw.executor.slurm.internals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import lombok.RequiredArgsConstructor;

/**
 * RemoteTerminal
 */
@RequiredArgsConstructor
public class RemoteTerminal {

    final private RemoteConfiguration   config;
    private SshClient                   client;
    private ClientSession               session;

    private void exec(String command) {
        try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                 ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                 ChannelExec channel = session.createExecChannel(command)) {

                channel.setOut(stdout);
                channel.setErr(stderr);

                channel.open().verify(10, TimeUnit.SECONDS);
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 1000);

                System.out.println("Standard Output: " + stdout.toString());
                System.out.println("Standard Error: " + stderr.toString());
        } catch (IOException e) {}
    }

    public void init() {
        client = SshClient.setUpDefaultClient();
        client.start();
    }

    public RemoteTerminal connect() {
        try {
            session = client.connect(config.getUser(), config.getHost(), config.getPort())
                .verify(10, TimeUnit.SECONDS)
                .getClientSession();

            session.addPasswordIdentity(config.getPassword());
            session.auth().verify(10, TimeUnit.SECONDS);
            
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void destroy() {
        client.stop();
    }

    public static void main(String[] args) {
        String command = "echo $USER est vraiment situé ici $PWD";
        RemoteConfiguration config = new RemoteConfiguration("192.168.122.152", 22, "almalinux", "ethaniel");
        try {
            RemoteTerminal term = new RemoteTerminal(config);
            term.init();
            term.connect();
            term.exec("sleep 500");
            term.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}