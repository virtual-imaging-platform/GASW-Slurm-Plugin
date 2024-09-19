package fr.insalyon.creatis.gasw.executor.slurm.internals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.insalyon.creatis.gasw.GaswConstants;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Mkdir;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Rm;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Getter @Log4j
public class SlurmManager {
    
    final private String    workflowId;
    final private Config    config;
    final private String    workingDir;
    private List<SlurmJob>  jobs = new ArrayList<>();

    private boolean         init = false;

    public SlurmManager(String workflowId, Config config) {
        this.workflowId = workflowId;
        this.config = config;
        this.workingDir = config.getCredentials().getWorkingDir() + workflowId;
    }

    /**
     * Create directory inside the workflow folder
     */
    public void init() {
        RemoteCommand remoteCommand = new Mkdir(getWorkingDir(), "");

        try {
            if (remoteCommand.execute(config.getCredentials()).failed())
                throw new GaswException("");

            checkLocalOutputsDir();
            init = true;
        } catch (GaswException e) {
            log.error("Failed to init the slurm manager !");
        }
    }

    public void checkLocalOutputsDir() {
        String[] dirs = { GaswConstants.OUT_ROOT, GaswConstants.ERR_ROOT, "./cache" };
        
        for (String dirName : dirs) {
            File dir = new File(dirName);

            if ( ! dir.exists())
                dir.mkdirs();
        }
    }

    /**
     * Clean files created inside the workflow folder
     */
    public void destroy() {
        RemoteCommand remoteCommand = new Rm(config.getCredentials().getWorkingDir() + workflowId, "-rf");
        
        try {
            if (remoteCommand.execute(config.getCredentials()).failed())
                throw new GaswException("");
            
        } catch (GaswException e) {
            log.error("Failed to desroy the slurm manager !");
        }
    }
}
