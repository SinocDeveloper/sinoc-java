package org.sinoc.vm.program.invoke;

import java.math.BigInteger;

import org.sinoc.core.Block;
import org.sinoc.core.Repository;
import org.sinoc.core.Transaction;
import org.sinoc.db.BlockStore;
import org.sinoc.vm.DataWord;
import org.sinoc.vm.program.Program;

public interface ProgramInvokeFactory {

    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, BlockStore blockStore);

    ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore,
                                            boolean staticCall, boolean byTestingSuite);


}
