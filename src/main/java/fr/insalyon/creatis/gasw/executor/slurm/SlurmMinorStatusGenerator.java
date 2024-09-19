package fr.insalyon.creatis.gasw.executor.slurm;

import fr.insalyon.creatis.gasw.execution.GaswMinorStatusServiceGenerator;

/**
 * @version DEPRECRATED CLASS
 */
public class SlurmMinorStatusGenerator extends GaswMinorStatusServiceGenerator {

    @Override
    public String getClient() { return null; }

    @Override
    public String getServiceCall() { return null; }
}
