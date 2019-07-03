package org.sinoc.config;

/**
 * Strategy interface to generate the nodeId and the nodePrivateKey.
 * <p>
 * Two strategies are available:
 * <ul>
 * <li>{@link GetNodeIdFromPropsFile}: searches for a nodeId.properties
 * and uses the values in the file to set the nodeId and the nodePrivateKey.</li>
 * <li>{@link GenerateNodeIdRandomly}: generates a nodeId.properties file
 * with a generated nodeId and nodePrivateKey.</li>
 * </ul>
 *
 */
public interface GenerateNodeIdStrategy {

    String getNodePrivateKey();

}
