package org.sinoc.solidity.compiler;

import static org.sinoc.solidity.compiler.ContractException.assembleError;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Sources {

    private final Map<String, SourceArtifact> artifacts = new HashMap<>();
    private String targetArtifact;

    public Sources(File[] files) {
        for (File file : files) {
            artifacts.put(file.getName(), new SourceArtifact(file));
        }
    }

    public void resolveDependencies() {
        for (String srcName : artifacts.keySet()) {
            SourceArtifact src = artifacts.get(srcName);
            for (String dep : src.getDependencies()) {
                SourceArtifact depArtifact = artifacts.get(dep);
                if (depArtifact == null) {
                    throw assembleError("can't resolve dependency: dependency '%s' not found.", dep);
                }
                src.injectDependency(depArtifact);
            };
        }

        for (SourceArtifact artifact : artifacts.values()) {
            if (!artifact.hasDependentArtifacts()) {
                targetArtifact = artifact.getName();
            }
        }
    }
    
    public String plainSource() {
        return artifacts.get(targetArtifact).plainSource();
    }
}
