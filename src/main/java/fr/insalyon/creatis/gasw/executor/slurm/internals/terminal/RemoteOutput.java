package fr.insalyon.creatis.gasw.executor.slurm.internals.terminal;

import lombok.Getter;

@Getter
public class RemoteOutput {
    
    final private RemoteStream      stdout;
    final private RemoteStream      stderr;
    final private int               exitCode;

    public RemoteOutput(String stdout, String stderr, int exitCode) {
        this.stdout = new RemoteStream(stdout);
        this.stderr = new RemoteStream(stderr);
        this.exitCode = exitCode;
    }
}
