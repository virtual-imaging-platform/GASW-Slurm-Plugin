package fr.insalyon.creatis.gasw.executor.slurm.internals;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Cat;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Sbatch;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Scontrol;
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
        StringBuilder builder = new StringBuilder();
        RemoteOutput output;

        builder.append("#!/bin/sh\n");
        builder.append("#SBATCH --job-name=" + data.getJobID() + "\n");
        builder.append("#SBATCH --output=" + data.getStdoutPath() + "\n");
        builder.append("#SBATCH --error=" + data.getStderrPath() + "\n");
        builder.append("cd " + data.getWorkingDir() + "\n");
        builder.append(data.getCommand() + "\n");
        builder.append("echo $? > " + data.getExitCodePath() + "\n");

        output = RemoteTerminal.oneCommand(data.getConfig(), "echo -en '" + builder.toString() + "' > " + data.getWorkingDir() + data.getJobID() + ".batch");

        if ( output == null || output.getExitCode() != 0 || ! output.getStderr().getContent().isEmpty()) {
            System.out.println(output.getStderr().getContent());
            throw new GaswException("Impossible to create the batch file");
        }
    }

    /**
     * Upload all the data to the job directory.
     * @throws GaswException
     */
    public void prepare() throws GaswException {
        RemoteTerminal rt = new RemoteTerminal(data.getConfig());

        rt.connect();
        for (RemoteFile file : data.getFilesUpload()) {
            rt.upload(file.getSource(), file.getDest());
        }
        rt.disconnect();
    }

    /**
     * Download all the data created by the jobs (not the app output) but the logs.
     * @throws GaswException
     */
    public void download() throws GaswException {
        RemoteTerminal rt = new RemoteTerminal(data.getConfig());

        rt.connect();
        for (RemoteFile file : data.getFilesDownload()) {
            System.err.println("je dois telecharger " + file.getSource());
            rt.download(file.getSource(), file.getDest());
        }
        rt.disconnect();
        System.err.println("j'ai telecharger les outputs");
    }



    public void submit() throws GaswException {
        RemoteCommand command = new Sbatch(data.getWorkingDir() + data.getJobID() + ".batch");

        try {
            command.execute(data.getConfig());

            if (command.failed())
                throw new GaswException("Command failed !");
            data.setSlurmJobID(command.result());
            System.err.println("VOICI LE BTACH JOB " + command.result() + " | " + command.getOutput().getStdout().getContent());
            
        } catch (GaswException e) {
            throw new GaswException("Failed subbmit the job " + e.getMessage());
        }
    }

    public void start() throws GaswException {
        System.err.println("je prepare");
        prepare();
        System.err.println("je batch");
        createBatchFile();
        System.err.println("je submit");
        submit();
        setStatus(GaswStatus.SUCCESSFULLY_SUBMITTED);
    }

    private GaswStatus getStatusRequest() {
        RemoteCommand command = new Scontrol(data.getSlurmJobID());
        String result = null;

        try {
            command.execute(data.getConfig());

            System.out.println("ici bb " + command.getOutput().getStderr().getContent());
            if (command.failed())
                return GaswStatus.UNDEFINED;
            result = command.result();
            if (result == null)
                return GaswStatus.UNDEFINED;

            switch (result) {
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
        } catch (GaswException e) {
            log.error("Failed to retrieve job status !");
            return GaswStatus.UNDEFINED;
        }
    }

    public int getExitCode() {
        RemoteCommand command = new Cat(data.getWorkingDir() + data .getExitCodePath());
        
        try {
            command.execute(data.getConfig());

            if (command.failed()) {
                System.err.println("FAILED TO CAT THE FILE");
                return 1;
            }
            return Integer.parseInt(command.result().trim());

        } catch (GaswException e){
            log.error("Can't retrieve exitcode " + e.getMessage());
            return 1;
        }
    }

    public GaswStatus getStatus() {
        GaswStatus rawStatus = null;

        if (status == GaswStatus.NOT_SUBMITTED || status == GaswStatus.UNDEFINED || status == GaswStatus.STALLED)
            return status;
        for (int i = 0; i < data.getConfig().getOptions().getStatusRetry(); i++) {
            rawStatus = getStatusRequest();

            if (rawStatus != GaswStatus.UNDEFINED)
                return rawStatus;
            Utils.sleepNException(data.getConfig().getOptions().getStatusRetryWait());
        }
        return GaswStatus.STALLED;
    }
}
