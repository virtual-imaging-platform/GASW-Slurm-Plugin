package fr.insalyon.creatis.gasw.executor.slurm.internals;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommandAlternative;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Cat;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Qsub;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Sbatch;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Scontrol;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Tracejob;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteFile;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteOutput;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteTerminal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j @RequiredArgsConstructor @Setter
public class SlurmJob {

    @Getter
    final private SlurmJobData  data;

    @Getter
    private boolean             terminated = false;
    private GaswStatus          status = GaswStatus.NOT_SUBMITTED;

    private void createBatchFile() throws GaswException {
        final BatchFile batchFile = new BatchFile(data);
        final RemoteOutput output;

        output = RemoteTerminal.oneCommand(data.getConfig(), "echo -en '" + batchFile.build().toString() + "' > " + data.getWorkingDir() + data.getJobID() + ".batch");

        if ( output == null || output.getExitCode() != 0 || ! output.getStderr().getContent().isEmpty()) {
            throw new GaswException("Impossible to create the batch file");
        }
    }

    /**
     * Upload all the data to the job directory.
     * @throws GaswException
     */
    public void prepare() throws GaswException {
        final RemoteTerminal rt = new RemoteTerminal(data.getConfig());

        rt.connect();
        for (final RemoteFile file : data.getFilesUpload()) {
            log.info("Uploading file : " + file.getSource());
            rt.upload(file.getSource(), file.getDest());
        }
        rt.disconnect();
    }

    /**
     * Download all the data created by the jobs (not the app output) but the logs.
     */
    public void download() {
        final RemoteTerminal rt = new RemoteTerminal(data.getConfig());

        try {
            rt.connect();
            for (final RemoteFile file : data.getFilesDownload()) {
                log.info("Downloading file : " + file.getSource());
                rt.download(file.getSource(), file.getDest());
            }
            rt.disconnect();

        } catch (GaswException e) {
            log.error("Failed to download the files !", e);
        }
    }



    public void submit() throws GaswException {
        final boolean isPBS = data.getConfig().getOptions().isUsePBS();
        final RemoteCommandAlternative<Qsub, Sbatch> alternative = new RemoteCommandAlternative<>(isPBS, 
            Qsub.class, Sbatch.class,
            data.getWorkingDir() + data.getJobID() + ".batch");
        final RemoteCommand command = alternative.getCommand();

        try {
            command.execute(data.getConfig());

            if (command.failed()) {
                throw new GaswException("Command failed !");
            }
            data.setSlurmJobID(command.result());
            log.debug("Job ID inside the Cluter : " + command.result());
            
        } catch (GaswException e) {
            log.error("Failed to submit the job " + getData().getJobID());
            throw e;
        }
    }

    public void start() throws GaswException {
        prepare();
        createBatchFile();
        submit();
        setStatus(GaswStatus.SUCCESSFULLY_SUBMITTED);
    }

    private GaswStatus convertStatus(final String status) {
        if (status == null) {
            return GaswStatus.UNDEFINED;
        }
        switch (status) {
            case "COMPLETE":
                return GaswStatus.COMPLETED;
            case "COMPLETED":
                return GaswStatus.COMPLETED;
            case "PENDING":
                return GaswStatus.QUEUED;
            case "CONFIGURING":
                return GaswStatus.QUEUED;
            case "RUNNING":
                return GaswStatus.RUNNING;
            case "FAILED":
                return GaswStatus.ERROR;
            case "NODE_FAIL":
                return GaswStatus.ERROR;
            case "BOOT_FAIL":
                return GaswStatus.ERROR;
            case "OUT_OF_MEMORY":
                return GaswStatus.ERROR;
            default:
                return GaswStatus.UNDEFINED;
        }
    }

    private GaswStatus getStatusRequest() {
        final boolean isPBS = data.getConfig().getOptions().isUsePBS();
        final RemoteCommand command = new RemoteCommandAlternative<Tracejob, Scontrol>(isPBS, 
            Tracejob.class, Scontrol.class, 
            data.getSlurmJobID()).getCommand();
        final String result;

        try {
            command.execute(data.getConfig());

            if (command.failed()) {
                return GaswStatus.UNDEFINED;
            }
            result = command.result();
            return convertStatus(result);

        } catch (GaswException e) {
            log.error("Failed to retrieve job status !");
            return GaswStatus.UNDEFINED;
        }
    }

    public int getExitCode() {
        final RemoteCommand command = new Cat(data.getWorkingDir() + data .getExitCodePath());
        
        try {
            command.execute(data.getConfig());

            if (command.failed()) {
                return 1;
            }
            return Integer.parseInt(command.result().trim());

        } catch (GaswException e){
            log.error("Can't retrieve exitcode " + e.getMessage());
            return 1;
        }
    }

    public GaswStatus getStatus() {
        GaswStatus rawStatus;

        if (status == GaswStatus.NOT_SUBMITTED || status == GaswStatus.UNDEFINED || status == GaswStatus.STALLED) {
            return status;
        }
        for (int i = 0; i < data.getConfig().getOptions().getStatusRetry(); i++) {
            rawStatus = getStatusRequest();

            if (rawStatus != GaswStatus.UNDEFINED) {
                return rawStatus;
            }
            Utils.sleepNException(data.getConfig().getOptions().getStatusRetryWait());
        }
        return GaswStatus.STALLED;
    }
}
