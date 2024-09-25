package fr.insalyon.creatis.gasw.executor.slurm.internals;

import java.util.List;

import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter @RequiredArgsConstructor
public class SlurmJobData {
    
    final private String     jobID;
    final private Config     config;
    
    private String           slurmJobID = null;
    private String           command;
    private String           workingDir;
    private List<RemoteFile> filesUpload;
    private List<RemoteFile> filesDownload;

    public String getExitCodePath() {
        return getJobID() + ".exit";
    }

    public String getStdoutPath() {
        return getWorkingDir() + "out/" + getJobID() + ".out";
    }

    public String getStderrPath() {
        return getWorkingDir() + "err/" + getJobID() + ".err";
    }
}
