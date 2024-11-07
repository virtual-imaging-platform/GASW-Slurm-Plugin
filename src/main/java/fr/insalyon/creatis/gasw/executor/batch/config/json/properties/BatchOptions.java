package fr.insalyon.creatis.gasw.executor.batch.config.json.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class BatchOptions {
    
    /* value in millis */
    @JsonProperty(value = "commandExecutionTimeout")
    private int commandExecutionTimeout = 10;

    /* value in millis */
    @JsonProperty(value = "sshEventTimeout")
    private int sshEventTimeout = 1000;

    @JsonProperty(value = "statusRetry")
    private int statusRetry = 10;

    /* value in millis */
    @JsonProperty(value = "statusRetryWait")
    private int statusRetryWait = 10;

    /* By default the system use slurn */
    @JsonProperty(value = "usePBS")
    private boolean usePBS = false;

    @JsonProperty("timeToBeReady")
    private int timeToBeReady = 120;
}
