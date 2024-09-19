package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.ArrayList;
import java.util.List;

import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.executor.slurm.config.Constants;
import fr.insalyon.creatis.gasw.plugin.ExecutorPlugin;

public class SlurmExecutor implements ExecutorPlugin{

    @Override
    public String getName() {
        return Constants.EXECUTOR_NAME;
    }

    @Override
    public void load(GaswInput gaswInput) throws GaswException {

    }

    @Override
    public List<Class> getPersistentClasses() throws GaswException {
        return new ArrayList<Class>();
    }

    @Override
    public String submit() throws GaswException {
        // return GaswSubmit
        return null;
    }

    @Override
    public void terminate() throws GaswException {

        // Gasw
        SlurmMonitor.getInstance().finish();
    }
}
