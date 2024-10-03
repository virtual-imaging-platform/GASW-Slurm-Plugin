package fr.insalyon.creatis.gasw.executor.slurm.internals;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BatchFile {
    
    final private SlurmJobData data;

    private StringBuilder builder = new StringBuilder();

    public StringBuilder build() {
        if (data.getConfig().getOptions().isUsePBS())
            doPBS();
        else
            doSlurm();
        doCommon();

        return builder;
    }

    private void doCommon() {
        builder.append("cd " + data.getWorkingDir() + "\n");
        builder.append(data.getCommand() + "\n");
        builder.append("echo $? > " + data.getExitCodePath() + "\n");
    }

    private void doSlurm() {
        builder.append("#!/bin/sh\n");
        builder.append("#SBATCH --job-name=" + data.getJobID() + "\n");
        builder.append("#SBATCH --output=" + data.getStdoutPath() + "\n");
        builder.append("#SBATCH --error=" + data.getStderrPath() + "\n");
    }

    private void doPBS() {
        builder.append("#!/bin/sh\n");
        builder.append("#PBS -N " + data.getJobID() + "\n");
        builder.append("#PBS -o " + data.getStdoutPath() + "\n");
        builder.append("#PBS -e " + data.getStderrPath() + "\n");
    }
}
