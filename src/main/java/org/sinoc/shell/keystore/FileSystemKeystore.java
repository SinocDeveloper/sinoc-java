package org.sinoc.shell.keystore;

import org.sinoc.crypto.ECKey;
import org.sinoc.net.swarm.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Key store manager working in user file system. Can store and load keys.
 * Comply to go-ethereum key store format.
 * https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition
 *
 */
@Component
public class FileSystemKeystore implements Keystore {
	Logger log = LoggerFactory.getLogger("keystore");
	
//    @Value("#{ ${keystore.dir} != #null ? ${keystore.dir} : #null }")
    @Value("${keystore.dir}")
    public String keystoreDir;

    public KeystoreFormat keystoreFormat = new KeystoreFormat();

    @Override
    public void removeKey(String address) {
        getFiles().stream()
                .filter(f -> hasAddressInName(address, f))
                .findFirst()
                .ifPresent(f -> f.delete());
    }

    @Override
    public void storeKey(ECKey key, String password) throws RuntimeException {
        final String address = Hex.toHexString(key.getAddress());
        if (hasStoredKey(address)) {
            throw new RuntimeException("Keystore is already exist for address: " + address +
                    " Please remove old one first if you want to add with new password.");
        }

        final File keysFolder = getKeyStoreLocation().toFile();
        keysFolder.mkdirs();

        String content = keystoreFormat.toKeystore(key, password);
        storeRawKeystore(content, address);
    }

    @Override
    public void storeRawKeystore(String content, String address) throws RuntimeException {
        String fileName = "UTC--" + getISODate(Util.curTime()) + "--" + address;
        try {
            Files.write(getKeyStoreLocation().resolve(fileName), Arrays.asList(content));
        } catch (IOException e) {
            throw new RuntimeException("Problem storing key for address");
        }
    }

    /**
     * @return array of addresses in format "0x123abc..."
     */
    @Override
    public String[] listStoredKeys() {
        return getFiles().stream()
                .filter(f -> !f.isDirectory())
                .map(f -> f.getName().split("--"))
                .filter(n -> n != null && n.length == 3)
                .map(a -> "0x" + a[2])
                .toArray(size -> new String[size]);
    }

    /**
     * @return some loaded key or null
     */
    @Override
    public ECKey loadStoredKey(String address, String password) throws RuntimeException {
        return getFiles().stream()
                .filter(f -> hasAddressInName(address, f))
                .map(f -> {
                    try {
                        return Files.readAllLines(f.toPath())
                                .stream()
                                .collect(Collectors.joining(""));
                    } catch (IOException e) {
                        throw new RuntimeException("Problem reading keystore file for address:" + address);
                    }
                })
                .map(content -> keystoreFormat.fromKeystore(content, password))
                .findFirst()
                .orElse(null);
    }

    private boolean hasAddressInName(String address, File file) {
        return !file.isDirectory() && file.getName().toLowerCase().endsWith("--" + address.toLowerCase());
    }

    @Override
    public boolean hasStoredKey(String address) {
        return getFiles().stream()
                .filter(f -> hasAddressInName(address, f))
                .findFirst()
                .isPresent();
    }

    private List<File> getFiles() {
        final File dir = getKeyStoreLocation().toFile();
        final File[] files = dir.listFiles();
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    private String getISODate(long milliseconds) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date(milliseconds));
    }

    /**
     * @return platform dependent path to Ethereum folder
     */
    public Path getKeyStoreLocation() {
        if (!StringUtils.isEmpty(keystoreDir)) {
            return Paths.get(keystoreDir);
        }

        final String keystoreDir = "keystore";
        final String osName = System.getProperty("os.name").toLowerCase();

        if (osName.indexOf("win") >= 0) {
            return Paths.get(System.getenv("APPDATA") + "/sinoc/" + keystoreDir);
        } else if (osName.indexOf("mac") >= 0) {
            return Paths.get(System.getProperty("user.home") + "/Library/sinoc/" + keystoreDir);
        } else {    // must be linux/unix
            return Paths.get(System.getProperty("user.home") + "/.sinoc/" + keystoreDir);
        }
    }
}
