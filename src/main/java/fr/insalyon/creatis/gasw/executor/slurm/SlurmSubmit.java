package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.Date;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.DAOFactory;
import fr.insalyon.creatis.gasw.dao.JobDAO;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.execution.GaswSubmit;
import lombok.extern.log4j.Log4j;

@Log4j
public class SlurmSubmit extends GaswSubmit {

    public SlurmSubmit(GaswInput gaswInput, SlurmMinorStatusGenerator minorStatusServiceGenerator) throws GaswException {
        super(gaswInput, minorStatusServiceGenerator);
        scriptName = generateScript();
    }

    @Override
    public String submit() throws GaswException {
        // log.info("K8s Executor Job ID: " + fileName);

        // return fileName;
        return null;
    }

    private void wrappedSubmit(String jobID) throws GaswException {
        try {
            // GAWS DAO
            JobDAO jobDAO = DAOFactory.getDAOFactory().getJobDAO();
            Job job = jobDAO.getJobByID(jobID);
            job.setStatus(GaswStatus.QUEUED);
            job.setDownload(new Date());
            jobDAO.update(job);

        } catch (DAOException e) {
            log.error(e.getStackTrace());
            throw new GaswException("Failed to submit the job (wrapped command)");
        }
    }
    
}