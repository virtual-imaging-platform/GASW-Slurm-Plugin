package fr.insalyon.creatis.gasw.executor.slurm.config.json.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Options {
    
    /* value in seconds */
    @JsonProperty(value = "commandExecutionTimeout")
    private int commandExecutionTimeout = 10;

    /* value in seconds */
    @JsonProperty(value = "sshEventTimeout")
    private int sshEventTimeout = 10;

    @JsonProperty(value = "statusRetry")
    private int statusRetry = 10;

    /* value in seconds */
    @JsonProperty(value = "statusRetryWait")
    private int statusRetryWait = 10;
}
