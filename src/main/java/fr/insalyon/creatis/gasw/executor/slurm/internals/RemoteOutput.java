package fr.insalyon.creatis.gasw.executor.slurm.internals;

import lombok.Data;

@Data
public class RemoteOutput {
    
    final private String  content;
    final private int     exitCode;

    // parse
    // getNextLine
    // split columns
}
