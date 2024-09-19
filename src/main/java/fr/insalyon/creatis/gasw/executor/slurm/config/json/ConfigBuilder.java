package fr.insalyon.creatis.gasw.executor.slurm.config.json;

import java.io.File;
import java.io.ObjectInputFilter.Config;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j;

@Log4j
public class ConfigBuilder {
    
    private String filePath;

    public ConfigBuilder(String filePath) {
        this.filePath = filePath;
    }

    public Config get() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Config loadedConfig = mapper.readValue(new File(filePath), Config.class);

            return loadedConfig;
        } catch (Exception e) {
            log.error("Failed to read the configuration file", e);
            return null;
        }
    }
}
