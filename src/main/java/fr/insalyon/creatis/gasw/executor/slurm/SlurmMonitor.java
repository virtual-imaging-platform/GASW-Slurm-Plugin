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
public class SlurmMonitor extends GaswMonitor {

    private static SlurmMonitor instance;

    @Getter @Setter
    private SlurmManager  manager;
    private List<SlurmJob>      finishedJobs;
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
        finishedJobs = new ArrayList<SlurmJob>();
        stop = false;
    }

    private void statusChecker() {
        List<SlurmJob> jobs = manager.getUnfinishedJobs();

        for (SlurmJob j : jobs) {
            GaswStatus stus = j.getStatus();

            System.err.println("job : " + j.getJobID() + " : " + stus.toString());
            if (stus != GaswStatus.RUNNING && stus != GaswStatus.QUEUED && stus != GaswStatus.UNDEFINED && stus != GaswStatus.NOT_SUBMITTED) {
                j.setTerminated(true);
                finishedJobs.add(j);
            } else if (stus ==  GaswStatus.RUNNING) {
                updateJob(j.getJobID(), stus);
            }
        }
    }

    @Override
    public void run() {
        while (!stop) {
            System.err.println("je fais le check des jobs en cours !");
            statusChecker();
            try {
                while (hasFinishedJobs()) {
                    SlurmJob sJob = pullFinishedJobID();
                    GaswStatus status = sJob.getStatus();
                    Job job = jobDAO.getJobByID(sJob.getJobID());
                    
                    if (status == GaswStatus.ERROR || status == GaswStatus.COMPLETED) {
                        job.setExitCode(sJob.getExitCode());
                        job.setStatus(job.getExitCode() == 0 ? GaswStatus.COMPLETED : GaswStatus.ERROR);
                    } else {
                        job.setStatus(status);
                    }
                    System.err.println("job : " + sJob.getJobID() + " final : " + job.getStatus());
                    
                    jobDAO.update(job);
                    new SlurmOutputParser(sJob).start();
                }

                Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());

            } catch (GaswException ex) {
            } catch (DAOException ex) {
                log.error(ex);
            } catch (InterruptedException ex) {
                log.error(ex);
            }
        }
    }

    @Override
    public synchronized void add(String jobID, String symbolicName, String fileName, String parameters) throws GaswException {
        Job job = new Job(jobID, GaswConfiguration.getInstance().getSimulationID(),
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
        SlurmJob lastJob = finishedJobs.get(0);

        finishedJobs.remove(lastJob);
        return lastJob;
    }

    public boolean hasFinishedJobs() {
        return ! finishedJobs.isEmpty();
    }

    public synchronized void addFinishedJob(SlurmJob job) {
        finishedJobs.add(job);
    }

    public synchronized void finish() {
        if (instance != null) {
            System.err.println("Monitor is off !");
            instance.stop = true;
            instance = null;
        }
    }

    public void updateJob(String jobID, GaswStatus status) {
        try {
            var job = jobDAO.getJobByID(jobID);

            if (job.getStatus() != status) {
                job.setStatus(status);
                jobDAO.update(job);
                System.err.println("je viens de mettre à jour le job " + job.getId() + " sur le statut " + status.toString());
            }
        } catch (DAOException e) {
            System.err.println("ICI j'ai une dao exeception! " + e.getMessage());
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