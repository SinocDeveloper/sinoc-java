package org.sinoc.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Strategy to generate the nodeId and the nodePrivateKey from a nodeId.properties file.
 * <p>
 * If the nodeId.properties file doesn't exist, it uses the
 * {@link GetNodeIdFromPropsFile#fallbackGenerateNodeIdStrategy} as a fallback strategy
 * to generate the nodeId and nodePrivateKey.
 *
 */
public class GetNodeIdFromPropsFile implements GenerateNodeIdStrategy {

    private String databaseDir;
    private GenerateNodeIdStrategy fallbackGenerateNodeIdStrategy;

    GetNodeIdFromPropsFile(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    @Override
    public String getNodePrivateKey() {
        Properties props = new Properties();
        File file = new File(databaseDir, "nodeId.properties");
        if (file.canRead()) {
            try (Reader r = new FileReader(file)) {
              props.load(r);
              return props.getProperty("nodeIdPrivateKey");
            } catch (IOException e) {
              throw new RuntimeException("Error reading 'nodeId.properties' file", e);
            }
        } else {
            if (fallbackGenerateNodeIdStrategy != null) {
                return fallbackGenerateNodeIdStrategy.getNodePrivateKey();
            } else {
                throw new RuntimeException("Can't read 'nodeId.properties' and no fallback method has been set");
            }
        }
    }

    public GenerateNodeIdStrategy withFallback(GenerateNodeIdStrategy generateNodeIdStrategy) {
        this.fallbackGenerateNodeIdStrategy = generateNodeIdStrategy;
        return this;
    }
}
