package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.Date;

import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.execution.GaswMonitor;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.executor.slurm.config.Constants;
import lombok.extern.log4j.Log4j;

@Log4j
public class SlurmMonitor extends GaswMonitor {

    private static SlurmMonitor instance;
    
    // private List<KJob>      finishedJobs;
    private boolean 		stop;

    public synchronized static SlurmMonitor getInstance() {
        if (instance == null) {
            instance = new SlurmMonitor();
            instance.start();
        }
        return instance;
    }

    private SlurmMonitor() {
        super();
        // finishedJobs = new ArrayList<KJob>();
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            // System.err.println("je fais le check des jobs en cours !");
            // statusChecker();
            // try {
            //     while (hasFinishedJobs()) {
            //         KJob kJob = pullFinishedJobID();
            //         GaswStatus status = kJob.getStatus();
            //         Job job = jobDAO.getJobByID(kJob.getData().getJobID());
                    
            //         if (status == GaswStatus.ERROR || status == GaswStatus.COMPLETED) {
            //             job.setExitCode(kJob.getExitCode());
            //             job.setStatus(job.getExitCode() == 0 ? GaswStatus.COMPLETED : GaswStatus.ERROR);
            //         } else {
            //             job.setStatus(status);
            //         }
            //         System.err.println("job : " + kJob.getData().getJobID() + " final : " + job.getStatus());
                    
            //         jobDAO.update(job);
            //         new KOutputParser(job.getId(), manager).start();
            //     }

            //     Thread.sleep(GaswConfiguration.getInstance().getDefaultSleeptime());

            // } catch (GaswException ex) {
            // } catch (DAOException ex) {
            //     log.error(ex);
            // } catch (InterruptedException ex) {
            //     log.error(ex);
            // }
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

    // public KJob pullFinishedJobID() {
        // KJob lastJob = finishedJobs.get(0);
// 
        // finishedJobs.remove(lastJob);
        // return lastJob;
    // }

    // public boolean hasFinishedJobs() {
        // return ! finishedJobs.isEmpty();
    // }

    // public synchronized void addFinishedJob(KJob job) {
        // finishedJobs.add(job);
    // }

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