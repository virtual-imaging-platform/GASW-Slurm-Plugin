package fr.insalyon.creatis.gasw.executor.slurm.internals.terminal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.executor.slurm.internals.RemoteConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RequiredArgsConstructor @Log4j
public class RemoteTerminal {

    final private RemoteConfiguration   config;
    private SshClient                   client;
    private ClientSession               session;

    private void init() {
        client = SshClient.setUpDefaultClient();
        client.start();
    }

    public void connect() throws GaswException {
        init();

        try {
            session = client.connect(config.getUser(), config.getHost(), config.getPort())
                .verify(10, TimeUnit.SECONDS)
                .getClientSession();

            session.addPasswordIdentity(config.getPassword());
            session.auth().verify(10, TimeUnit.SECONDS);
            
        } catch (IOException e) {
            log.error(e);
            throw new GaswException("Failed to connect to ssh");
        }
    }
    
    /**
     * @see RFC 4253
     */
    public void disconnect() throws GaswException {
        try {
            session.disconnect(11, "Session ended");
            client.stop();
        } catch (IOException e) {
            log.error(e);
            client.stop();
            throw new GaswException("Failed to disconnect");
        }
    }

    public RemoteOutput executeCommand(String command) {
        try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                 ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                 ChannelExec channel = session.createExecChannel(command)) {

                channel.setOut(stdout);
                channel.setErr(stderr);

                channel.open().verify(10, TimeUnit.SECONDS);
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 1000);

                return (new RemoteOutput(stdout.toString(), stderr.toString(), channel.getExitStatus()));
        } catch (IOException e) {
            return null;
        }
    }

    public static RemoteOutput oneCommand(RemoteConfiguration config, String command) {
        RemoteTerminal term = new RemoteTerminal(config);
        RemoteOutput result = null;

        try {
            term.connect();
            result = term.executeCommand(command);
            term.disconnect();

            return result;
        } catch (GaswException e) {
            log.error("Failed to execute oneCommand !");
            return null;
        }
    }
}