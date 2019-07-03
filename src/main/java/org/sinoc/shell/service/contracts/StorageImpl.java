package org.sinoc.shell.service.contracts;

import org.sinoc.contrdata.storage.Storage;
import org.sinoc.contrdata.storage.dictionary.Layout;
import org.sinoc.contrdata.storage.dictionary.StorageDictionaryDb;
import org.sinoc.facade.Repository;
import org.sinoc.vm.DataWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StorageImpl implements Storage {

    private final Repository repository;
    private final StorageDictionaryDb dictionaryDb;

    @Autowired
    public StorageImpl(Repository repository, StorageDictionaryDb dictionaryDb) {
        this.repository = repository;
        this.dictionaryDb = dictionaryDb;
    }


    @Override
    public int size(byte[] address) {
        return repository.getStorageSize(address);
    }

    @Override
    public Map<DataWord, DataWord> entries(byte[] address, List<DataWord> keys) {
        return repository.getStorage(address, keys);
    }

    @Override
    public Set<DataWord> keys(byte[] address) {
        return dictionaryDb.getDictionaryFor(Layout.Lang.solidity, address).allKeys();
    }

    @Override
    public DataWord get(byte[] address, DataWord key) {
        return repository.getStorageValue(address, key);
    }
}
