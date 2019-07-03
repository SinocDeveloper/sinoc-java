package org.sinoc.contrdata.storage.dictionary;


import java.io.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import javax.annotation.*;
import org.sinoc.util.*;
import org.sinoc.datasource.*;

@Service
public class StorageDictionaryDb implements Flushable, Closeable
{
    private DbSource<byte[]> db;
    
    public StorageDictionaryDb(@Qualifier("storageDict") DbSource<byte[]> dataSource) {
        this.db = dataSource;
    }
    
    @Override
    public void flush() {
        this.db.flush();
    }
    
    @PreDestroy
    @Override
    public void close() {
        this.db.flush();
    }
    
    public StorageDictionary getDictionaryFor(Layout.Lang lang, byte[] contractAddress) {
        byte[] key = ByteUtil.xorAlignRight(lang.getFingerprint(), contractAddress);
        XorDataSource<byte[]> dataSource = new XorDataSource<>(this.db, key);
        return new StorageDictionary((Source<byte[], byte[]>)dataSource);
    }
}
