package fr.insalyon.creatis.gasw.executor.slurm.config.json.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Credentials {
    
    @JsonProperty(value = "host", required = true)
    private String  host;

    @JsonProperty(value = "port", required = true)
    private int     port;

    @JsonProperty(value = "username", required = true)
    private String  username;

    @JsonProperty(value = "password", required = true)
    private String  password;
}
