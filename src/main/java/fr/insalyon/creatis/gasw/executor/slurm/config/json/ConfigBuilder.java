package fr.insalyon.creatis.gasw.executor.slurm.config.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.gasw.executor.slurm.config.json.properties.Config;
import lombok.extern.log4j.Log4j;

@Log4j
public class ConfigBuilder {
    
    final private String filePath;

    public ConfigBuilder(final String filePath) {
        this.filePath = filePath;
    }

    public Config get() {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(new File(filePath), Config.class);

        } catch (IOException e) {
            log.error("Failed to read the configuration file", e);
            return null;
        }
    }
}
