package fr.insalyon.creatis.gasw.executor.slurm;

import java.io.File;

import fr.insalyon.creatis.gasw.GaswConstants;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.execution.GaswOutputParser;
import fr.insalyon.creatis.gasw.executor.slurm.internals.SlurmJob;

public class SlurmOutputParser extends GaswOutputParser{

    final private SlurmJob  job;

    public SlurmOutputParser(final SlurmJob job) {
        super(job.getData().getJobID());
        this.job = job;
    }

    @Override
    public GaswOutput getGaswOutput() throws GaswException {
        final File stdOut = getAppStdFile(GaswConstants.OUT_EXT, GaswConstants.OUT_ROOT);
        final File stdErr = getAppStdFile(GaswConstants.ERR_EXT, GaswConstants.ERR_ROOT);
        GaswExitCode gaswExitCode = GaswExitCode.EXECUTION_CANCELED;
        int exitCode;

        job.download();
        moveProvenanceFile(".");

        exitCode = parseStdOut(stdOut);
        exitCode = parseStdErr(stdErr, exitCode);

        switch (exitCode) {
            case 0:
                gaswExitCode = GaswExitCode.SUCCESS;
                break;
            case 1:
                gaswExitCode = GaswExitCode.ERROR_READ_GRID;
                break;
            case 2:
                gaswExitCode = GaswExitCode.ERROR_WRITE_GRID;
                break;
            case 6:
                gaswExitCode = GaswExitCode.EXECUTION_FAILED;
                break;
            case 7:
                gaswExitCode = GaswExitCode.ERROR_WRITE_LOCAL;
                break;
            default:
                gaswExitCode = GaswExitCode.UNDEFINED;
        }

        return new GaswOutput(job.getData().getJobID(), gaswExitCode, "", uploadedResults,
                appStdOut, appStdErr, stdOut, stdErr);
    }

    @Override
    protected void resubmit() throws GaswException {
        throw new GaswException("");
    }
}
