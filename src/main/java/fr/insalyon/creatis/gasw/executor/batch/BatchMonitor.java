package fr.insalyon.creatis.gasw.executor.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.execution.GaswMonitor;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.batch.config.Constants;
import fr.insalyon.creatis.gasw.executor.batch.internals.BatchJob;
import fr.insalyon.creatis.gasw.executor.batch.internals.BatchManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
final public class BatchMonitor extends GaswMonitor {

    private static BatchMonitor instance;

    final private List<BatchJob>      finishedJobs;
    @Getter @Setter
    private BatchManager        manager;
    private boolean 		    stop;

    public BatchMonitor() {
        super();
        finishedJobs = new ArrayList<>();
        stop = false;
    }

    private void statusChecker() {
        final List<BatchJob> jobs = manager.getUnfinishedJobs();

        for (final BatchJob j : jobs) {
            final GaswStatus stus = j.getStatus();

            log.debug("Job ID : " + j.getData().getJobID() + " -> " + stus.toString());
            if (stus != GaswStatus.RUNNING && stus != GaswStatus.QUEUED && stus != GaswStatus.UNDEFINED && stus != GaswStatus.NOT_SUBMITTED) {
                j.setTerminated(true);
                finishedJobs.add(j);
            } else if (stus ==  GaswStatus.RUNNING) {
                updateJob(j.getData().getJobID(), stus);
            }
        }
    }

    @Override
    public void run() {
        while (!stop) {
            statusChecker();
            try {
                while (hasFinishedJobs()) {
                    final BatchJob sJob = pullFinishedJobID();
                    final GaswStatus status = sJob.getStatus();
                    final Job job = jobDAO.getJobByID(sJob.getData().getJobID());
                    
                    if (status == GaswStatus.ERROR || status == GaswStatus.COMPLETED) {
                        job.setExitCode(sJob.getExitCode());
                        job.setStatus(job.getExitCode() == 0 ? GaswStatus.COMPLETED : GaswStatus.ERROR);
                    } else {
                        job.setStatus(status);
                    }
                    
                    jobDAO.update(job);
                    new BatchOutputParser(sJob).start();
                }

                Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());

            } catch (GaswException | DAOException ex) {
                log.error("Ignored exception !", ex);
            } catch (InterruptedException ex) {
                log.error("Interrupted exception, stopping the worker !");
                finish();
            }
        }
    }

    @Override
    public synchronized void add(final String jobID, final String symbolicName, final String fileName, final String parameters) throws GaswException {
        final Job job = new Job(jobID, GaswConfiguration.getInstance().getSimulationID(),
            GaswStatus.QUEUED, symbolicName, fileName, parameters,
            Constants.EXECUTOR_NAME);

        add(job);
        log.info("Adding job: " + jobID);
        try {
            job.setQueued(new Date());
            jobDAO.update(job);

        } catch (DAOException ex) {
            log.error(ex);
        }
    }

    public BatchJob pullFinishedJobID() {
        final BatchJob lastJob = finishedJobs.get(0);

        finishedJobs.remove(lastJob);
        return lastJob;
    }

    public boolean hasFinishedJobs() {
        return ! finishedJobs.isEmpty();
    }

    public synchronized void addFinishedJob(final BatchJob job) {
        finishedJobs.add(job);
    }

    public synchronized void finish() {
        if (instance != null) {
            log.trace("Monitor is off !");
            instance.stop = true;
            instance = null;
        }
    }

    public void updateJob(final String jobID, final GaswStatus status) {
        try {
            final Job job = jobDAO.getJobByID(jobID);

            if (job.getStatus() != status) {
                job.setStatus(status);
                jobDAO.update(job);
            }
        } catch (DAOException e) {
            log.error(e);
        }
    }

    @Override
    protected void kill(Job job) {
        // to implements
    }

    @Override
    protected void reschedule(Job job) {}

    @Override
    protected void replicate(Job job) {}

    @Override
    protected void killReplicas(Job job) {}

    @Override
    protected void resume(Job job) {}
}
