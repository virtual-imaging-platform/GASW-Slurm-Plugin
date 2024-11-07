package fr.insalyon.creatis.gasw.executor.slurm;

import fr.insalyon.creatis.gasw.execution.GaswMinorStatusServiceGenerator;
import lombok.NoArgsConstructor;

/**
 * @deprecated
 */
@NoArgsConstructor
public class SlurmMinorStatusGenerator extends GaswMinorStatusServiceGenerator {

    @Override
    public String getClient() { return null; }

    @Override
    public String getServiceCall() { return null; }
}
