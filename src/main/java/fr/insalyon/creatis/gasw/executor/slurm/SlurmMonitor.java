package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.execution.GaswMonitor;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.config.Constants;
import fr.insalyon.creatis.gasw.executor.slurm.internals.SlurmJob;
import fr.insalyon.creatis.gasw.executor.slurm.internals.SlurmManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
final public class SlurmMonitor extends GaswMonitor {

    private static SlurmMonitor instance;

    final private List<SlurmJob>      finishedJobs;
    @Getter @Setter
    private SlurmManager        manager;
    private boolean 		    stop;

    public synchronized static SlurmMonitor getInstance() {
        if (instance == null) {
            instance = new SlurmMonitor();
            instance.start();
        }
        return instance;
    }

    private SlurmMonitor() {
        super();
        finishedJobs = new ArrayList<>();
        stop = false;
    }

    private void statusChecker() {
        final List<SlurmJob> jobs = manager.getUnfinishedJobs();

        for (final SlurmJob j : jobs) {
            final GaswStatus stus = j.getStatus();

            System.err.println("job : " + j.getData().getJobID() + " : " + stus.toString());
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
                    final SlurmJob sJob = pullFinishedJobID();
                    final GaswStatus status = sJob.getStatus();
                    final Job job = jobDAO.getJobByID(sJob.getData().getJobID());
                    
                    if (status == GaswStatus.ERROR || status == GaswStatus.COMPLETED) {
                        job.setExitCode(sJob.getExitCode());
                        job.setStatus(job.getExitCode() == 0 ? GaswStatus.COMPLETED : GaswStatus.ERROR);
                    } else {
                        job.setStatus(status);
                    }
                    System.err.println("job : " + sJob.getData().getJobID() + " final : " + job.getStatus());
                    
                    jobDAO.update(job);
                    new SlurmOutputParser(sJob).start();
                }

                Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());

            } catch (GaswException | DAOException | InterruptedException ex) {
                log.error(ex);
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
            System.err.println(ex.getMessage());
        }
    }

    public SlurmJob pullFinishedJobID() {
        final SlurmJob lastJob = finishedJobs.get(0);

        finishedJobs.remove(lastJob);
        return lastJob;
    }

    public boolean hasFinishedJobs() {
        return ! finishedJobs.isEmpty();
    }

    public synchronized void addFinishedJob(final SlurmJob job) {
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
