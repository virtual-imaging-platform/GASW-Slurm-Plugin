package fr.insalyon.creatis.gasw.executor.slurm;

import java.util.ArrayList;
import java.util.List;

import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.executor.slurm.config.Constants;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.ConfigBuilder;
import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import fr.insalyon.creatis.gasw.executor.slurm.internals.SlurmManager;
import fr.insalyon.creatis.gasw.plugin.ExecutorPlugin;
import lombok.NoArgsConstructor;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation @NoArgsConstructor
public class SlurmExecutor implements ExecutorPlugin {

    private SlurmManager    manager;
    private SlurmSubmit     submitter;
    private boolean         loaded = false;

    @Override
    public String getName() {
        return Constants.EXECUTOR_NAME;
    }

    @Override
    public void load(final GaswInput gaswInput) throws GaswException {
        if ( ! loaded) {
            final ConfigBuilder configBuilder = new ConfigBuilder(Constants.PLUGIN_CONFIG);
            final Config config = configBuilder.get();

            manager = new SlurmManager(GaswConfiguration.getInstance().getSimulationID(), config);
            manager.init();

            SlurmMonitor.getInstance().setManager(manager);
            loaded = true;
        }
        submitter = new SlurmSubmit(gaswInput, new SlurmMinorStatusGenerator(), manager);
    }

    @Override
    public List<Class> getPersistentClasses() throws GaswException {
        return new ArrayList<>();
    }

    @Override
    public String submit() throws GaswException {
        System.err.println("je fais une demande de submit ici");
        return submitter.submit();
    }

    @Override
    public void terminate() throws GaswException {
        manager.destroy();
        // Gasw
        SlurmMonitor.getInstance().finish();
    }
}
