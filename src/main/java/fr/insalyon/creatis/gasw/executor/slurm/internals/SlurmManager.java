package fr.insalyon.creatis.gasw.executor.slurm.internals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswConstants;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Mkdir;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Rm;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteFile;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Getter @Log4j
public class SlurmManager {
    
    final private String    workflowId;
    final private Config    config;
    final private String    workingDir;
    private List<SlurmJob>  jobs = new ArrayList<>();

    private boolean         init = false;
    private Boolean         end;

    public SlurmManager(String workflowId, Config config) {
        this.workflowId = workflowId;
        this.config = config;
        this.workingDir = config.getCredentials().getWorkingDir() + workflowId;
    }

    public void init() {
        try {
            checkRemoteDirs();
            checkLocalOutputsDir();

            init = true;
        } catch (GaswException e) {
            log.error("Failed to init the slurm manager !");
        }
    }

    /**
     * Create the necessary directories on the remote (slurm cluster)
     */
    public void checkRemoteDirs() throws GaswException {
        List<RemoteCommand> commands = new ArrayList<RemoteCommand>();

        commands.add(new Mkdir(getWorkingDir(), ""));
        commands.add(new Mkdir(getWorkingDir() + "/out", "-p"));
        commands.add(new Mkdir(getWorkingDir() + "/err", "-p"));
        commands.add(new Mkdir(getWorkingDir() + "/sh", "-p"));

        for (RemoteCommand command : commands) {
            command.execute(config);

            if (command.failed())
                throw new GaswException("Failed to create the remotes dirs !");
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

        end = true;
        // try {
            // if (remoteCommand.execute(config).failed())
                // throw new GaswException("");
            
        // } catch (GaswException e) {
            // log.error("Failed to desroy the slurm manager !");
        // }
    }

    private void submitter(SlurmJob exec) {
        if (end == null) {
            end = false;
            new Thread(this.new SlurmRunner()).start();
        }
        synchronized (this) {
            jobs.add(exec);
        }
    }

    public void submitter(String jobID, String command) {
        SlurmJobData jobData = new SlurmJobData(jobID, config);
        String workingDirectoryJob = config.getCredentials().getWorkingDir() + workflowId + "/";
        List<RemoteFile> filesUpload = new ArrayList<>();
        List<RemoteFile> filesDownload = new ArrayList<>();

        System.err.println("voici le working dir " + workingDirectoryJob);
        jobData.setWorkingDir(workingDirectoryJob);
        jobData.setCommand(command);

        filesUpload.add(new RemoteFile("./inv/" + jobID + "-invocation.json", workingDirectoryJob));
        filesUpload.add(new RemoteFile("./config/" + jobID + "-configuration.sh", workingDirectoryJob));
        filesUpload.add(new RemoteFile("./sh/" + jobID + ".sh", workingDirectoryJob + "/sh"));
        filesUpload.add(new RemoteFile("./workflow.json", workingDirectoryJob));

        filesDownload.add(new RemoteFile(jobData.getStderrPath(), "./err/" + jobID + ".sh.err"));
        filesDownload.add(new RemoteFile(jobData.getStdoutPath(), "./out/" + jobID + ".sh.out"));
        filesDownload.add(new RemoteFile(workingDirectoryJob + jobID + ".sh.provenance.json", "./" + jobID + ".sh.provenance.json"));
        filesDownload.add(new RemoteFile(workingDirectoryJob + jobID + ".batch", "./" + jobID + ".batch"));

        jobData.setFilesDownload(filesDownload);
        jobData.setFilesUpload(filesUpload);

        SlurmJob job = new SlurmJob(jobData);
        submitter(job);
    }

    public SlurmJob getJob(String jobID) {
        return jobs.stream()
                .filter(job -> job.getData().getJobID().equals(jobID))
                .findFirst()
                .orElse(null);
    }

    public List<SlurmJob> getUnfinishedJobs() {
        return jobs.stream()
               .filter(job -> !job.isTerminated())
               .collect(Collectors.toCollection(ArrayList::new));
    }

    class SlurmRunner implements Runnable {
        private DateTime startedTime;

        @Override
        public void run() {
            try {
                startedTime = DateTime.now();
                loop();
            } catch (GaswException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error("Something bad happened during the K8sRunner", e);
            }
        }

        private void loop() throws InterruptedException, GaswException {
            while (init != true) {
                System.err.println("je wait to be init");
                Duration diff = new Duration(startedTime, DateTime.now());
                
                if (diff.getStandardSeconds() > 120)
                    throw new GaswException("SlurmManager wasn't ready in 2 minutes, aborting");
                else {
                    Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());
                }
            }
            while (end == false) {
                synchronized (this) {
                    for (SlurmJob exec : getUnfinishedJobs()) {
                        if (exec.getStatus() == GaswStatus.NOT_SUBMITTED) {
                            exec.setStatus(GaswStatus.QUEUED);
                            exec.start();
                            System.err.println("je fais le start");
                        }
                    }
                }
                Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());
            }
        }
    }
}
