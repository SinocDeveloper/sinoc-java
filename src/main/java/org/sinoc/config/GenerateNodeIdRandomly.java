package org.sinoc.config;

import org.sinoc.crypto.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * Strategy to randomly generate the nodeId and the nodePrivateKey.
 */
public class GenerateNodeIdRandomly implements GenerateNodeIdStrategy {

    private static Logger logger = LoggerFactory.getLogger("general");

    private String databaseDir;

    GenerateNodeIdRandomly(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    @Override
    public String getNodePrivateKey() {
        ECKey key = new ECKey();
        Properties props = new Properties();
        props.setProperty("nodeIdPrivateKey", Hex.toHexString(key.getPrivKeyBytes()));
        props.setProperty("nodeId", Hex.toHexString(key.getNodeId()));

        File file = new File(databaseDir, "nodeId.properties");
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            props.store(writer, "Generated NodeID. To use your own nodeId please refer to 'peer.privateKey' config option.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("New nodeID generated: " + props.getProperty("nodeId"));
        logger.info("Generated nodeID and its private key stored in " + file);

        return props.getProperty("nodeIdPrivateKey");
    }
}
