package fr.insalyon.creatis.gasw.executor.slurm.config.json.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Config {

    @JsonProperty(value = "credentials", required = true)
    private Credentials credentials;

    @JsonProperty(value = "options", required =  true)
    private Options options;
}