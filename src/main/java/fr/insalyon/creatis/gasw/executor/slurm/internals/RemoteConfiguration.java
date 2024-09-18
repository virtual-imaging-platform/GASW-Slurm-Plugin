package fr.insalyon.creatis.gasw.executor.slurm.internals;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class RemoteConfiguration {

    final private String  host;
    final private int     port;
    final private String  user;
    final private String  password;
}