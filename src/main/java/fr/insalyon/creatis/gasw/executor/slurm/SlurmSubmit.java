package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.Date;

import fr.insalyon.creatis.gasw.GaswConstants;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.DAOFactory;
import fr.insalyon.creatis.gasw.dao.JobDAO;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.execution.GaswSubmit;
import fr.insalyon.creatis.gasw.executor.slurm.internals.SlurmManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class SlurmSubmit extends GaswSubmit {

    final private SlurmManager manager;

    public SlurmSubmit(final GaswInput gaswInput, final SlurmMinorStatusGenerator minorStatusServiceGenerator, final SlurmManager manager) throws GaswException {
        super(gaswInput, minorStatusServiceGenerator);
        scriptName = generateScript();
        this.manager = manager;
    }

    @Override
    public String submit() throws GaswException {
        final String fileName = scriptName.substring(0, scriptName.lastIndexOf("."));
        final StringBuilder params = new StringBuilder();

        for (final String p : gaswInput.getParameters()) {
            params.append(p + " ");
        }

        SlurmMonitor.getInstance().add(fileName, gaswInput.getExecutableName(), fileName, params.toString());
        wrappedSubmit(fileName);
        log.info("K8s Executor Job ID: " + fileName);

        return fileName;
    }

    private void wrappedSubmit(String jobID) throws GaswException {
        try {
            // GAWS DAO
            final JobDAO jobDAO = DAOFactory.getDAOFactory().getJobDAO();
            final Job job = jobDAO.getJobByID(jobID);

            job.setStatus(GaswStatus.QUEUED);
            job.setDownload(new Date());
            jobDAO.update(job);
            manager.submitter(job.getId(), "bash " + GaswConstants.SCRIPT_ROOT + "/" + scriptName);

        } catch (DAOException e) {
            log.error(e.getStackTrace());
            throw new GaswException("Failed to submit the job (wrapped command)");
        }
    }
    
}
