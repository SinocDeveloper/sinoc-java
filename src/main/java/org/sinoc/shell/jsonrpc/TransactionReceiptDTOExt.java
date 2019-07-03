package org.sinoc.shell.jsonrpc;

import org.sinoc.core.Block;
import org.sinoc.core.TransactionInfo;

import static org.sinoc.shell.jsonrpc.TypeConverter.toJsonHex;

public class TransactionReceiptDTOExt extends TransactionReceiptDTO {

    public String returnData;
    public String error;

    public TransactionReceiptDTOExt(Block block, TransactionInfo txInfo) {
        super(block, txInfo);
        returnData = toJsonHex(txInfo.getReceipt().getExecutionResult());
        error = txInfo.getReceipt().getError();
    }
}
