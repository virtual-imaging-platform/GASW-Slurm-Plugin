package fr.insalyon.creatis.gasw.executor.slurm.internals;

import java.rmi.Remote;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.RemoteCommand;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Cat;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Sbatch;
import fr.insalyon.creatis.gasw.executor.slurm.internals.commands.items.Squeue;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteOutput;
import fr.insalyon.creatis.gasw.executor.slurm.internals.terminal.RemoteTerminal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RequiredArgsConstructor @Log4j
public class SlurmJob {
    
    final private String    jobID;
    final private String    command;
    final private Config    config;
    final private String    workingDir;

    private GaswStatus      status = GaswStatus.UNDEFINED;

    private void createBatchFile() throws GaswException {
        StringBuilder builder = new StringBuilder();
        RemoteOutput output;

        builder.append("#!/bin/sh");
        builder.append("#SBATCH --job-name=" + jobID);
        builder.append("#SBATCH --output=" + jobID + ".out");
        builder.append("#SBATCH --error=" + jobID + ".err");
        builder.append("cd " + workingDir);
        builder.append(command);
        builder.append("echo $? > " + jobID + ".exit");

        output = RemoteTerminal.oneCommand(config.getCredentials(), "echo \"" + builder.toString() + "\" > " + workingDir + jobID + ".batch");

        if ( output == null || output.getExitCode() != 1 || ! output.getStderr().getContent().isEmpty())
            throw new GaswException("Impossible to create the batch file");
    }

    public void submit() throws GaswException {
        RemoteCommand command = new Sbatch(workingDir + jobID + ".batch");

        try {
            command.execute(config.getCredentials());

            if (command.failed())
                throw new GaswException("Command failed !");
            
        } catch (GaswException e) {
            throw new GaswException("Failed subbmit the job " + e.getMessage());
        }
    }

    private GaswStatus getStatusRequest() {
        RemoteCommand command = new Squeue(jobID);
        String result = null;

        try {
            result = command.execute(config.getCredentials()).result();

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
        RemoteCommand command = new Cat(workingDir + jobID + ".exit");
        
        try {
            command.execute(config.getCredentials());

            if (command.failed()) {
                System.err.println("FAILED TO CAT THE FILE");
                return 1;
            }
            return Integer.parseInt(command.result());

        } catch (GaswException e){
            log.error("Can't retrieve exitcode " + e.getMessage());
            return 1;
        }
    }

    public GaswStatus getStatus() {
        GaswStatus rawStatus = null;

        if (status == GaswStatus.NOT_SUBMITTED || status == GaswStatus.UNDEFINED || status == GaswStatus.STALLED)
            return status;
        for (int i = 0; i < config.getOptions().getStatusRetry(); i++) {
            rawStatus = getStatusRequest();

            if (rawStatus != GaswStatus.UNDEFINED)
                return rawStatus;
            Utils.sleepNException(config.getOptions().getStatusRetryWait());
        }
        return GaswStatus.STALLED;
    }
}
