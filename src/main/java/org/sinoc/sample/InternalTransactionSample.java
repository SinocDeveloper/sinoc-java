package org.sinoc.sample;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.sinoc.core.BlockSummary;
import org.sinoc.core.TransactionExecutionSummary;
import org.sinoc.listener.CompositeEthereumListener;
import org.sinoc.listener.EthereumListenerAdapter;
import org.sinoc.util.ByteUtil;
import org.sinoc.vm.program.InternalTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InternalTransactionSample {
	
	@Autowired
	CompositeEthereumListener compositeEthereumListener;
	
	@PostConstruct
	public void init() {
		compositeEthereumListener.addListener(new EthereumListenerAdapter() {
			@Override
			public void onBlock(BlockSummary blockSummary) {
				
				long blockNumber = blockSummary.getBlock().getNumber();	
				
				List<TransactionExecutionSummary> summaries = blockSummary.getSummaries();
				if(CollectionUtils.isNotEmpty(summaries)) {
					for(TransactionExecutionSummary summary:summaries) {
						List<InternalTransaction> internalTransactions = summary.getInternalTransactions();
						if(CollectionUtils.isNotEmpty(internalTransactions)) {
							for(InternalTransaction internalTransaction:internalTransactions) {
								
								System.out.println(blockNumber);
								System.out.println(ByteUtil.toHexString(internalTransaction.getParentHash()));
								System.out.println(new Date(blockSummary.getBlock().getTimestamp()*1000));
								System.out.println(ByteUtil.toHexString(internalTransaction.getSender()));
								System.out.println(ByteUtil.toHexString(internalTransaction.getReceiveAddress()));
								System.out.println(ByteUtil.toHexString(internalTransaction.getValue()));
							}
						}
					}
				}
			}
		});
	}
	
}
