package org.sinoc.shell.service.contracts;

import org.sinoc.contrdata.storage.StorageEntry;
import org.sinoc.shell.model.dto.ContractObjects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContractsService {

    boolean deleteContract(String address);

    ContractObjects.ContractInfoDTO addContract(String address, String src) throws Exception;

    List<ContractObjects.ContractInfoDTO> getContracts();

    ContractObjects.ContractInfoDTO uploadContract(String address, MultipartFile[] files) throws Exception;

    ContractObjects.IndexStatusDTO getIndexStatus() throws Exception;

    /**
     * Get contract storage entries.
     *
     * @param hexAddress - address of contract
     * @param path - nested level of fields
     * @param pageable - for paging
     */
    Page<StorageEntry> getContractStorage(String hexAddress, String path, Pageable pageable);

    boolean importContractFromExplorer(String hexAddress) throws Exception;

    /**
     * For testing purpose.
     */
    void clearContractStorage(String hexAddress) throws Exception;
}
