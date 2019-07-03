package org.sinoc.shell.service;

import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;

import java.util.Optional;

public class BlockchainConsts {

    /**
     * Return pair of name and explorer url.
     */
    public static Pair<String, Optional<String>> getNetworkInfo(Environment env, String genesisHash) {
        final String networkNameKey = String.format("network.%s.networkName", genesisHash);
        final String explorerUrlKey = String.format("network.%s.explorerUrl", genesisHash);

        return Optional.ofNullable(env.getProperty(networkNameKey))
                .map(name -> Pair.of(name, Optional.ofNullable(env.getProperty(explorerUrlKey))))
                .orElse(Pair.of("Unknown network", Optional.empty()));
    }
}
