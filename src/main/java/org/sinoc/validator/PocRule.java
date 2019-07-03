package org.sinoc.validator;

import java.math.BigInteger;
import java.util.List;

import org.sinoc.core.BlockHeader;
import org.sinoc.mine.plot.PlotAlgo;
import org.sinoc.util.Convert;
import org.sinoc.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocRule extends BlockHeaderRule{
	private static Logger logger = LoggerFactory.getLogger(PocRule.class);
	List<String> forkTrusts;
	
	public PocRule(List<String> forkTrusts) {
		this.forkTrusts = forkTrusts;
	}
	
	@Override
	public ValidationResult validate(BlockHeader header) {
		if (header.isGenesis()){
			return Success;
		}
		BigInteger deadLine = PocRule.calculateValidateDeadline(header.getGenSign(),header.getNumber(),header.getCoinbase(),header.getNonce(),header.getBaseTarget());
		if(deadLine.equals(new BigInteger(1,header.getDeadLine()))){
			return Success;
		}else{
			// deal for update node right fork
			if(forkTrusts!=null) {
				for(String forkInfo:forkTrusts) {
					String[] forkInfoArr = forkInfo.split(",");
					long forkNum = Long.parseLong(forkInfoArr[0]);
					byte[] forkDeadLine = new BigInteger(forkInfoArr[1]).toByteArray();
					if(header.getNumber() == forkNum 
							&& FastByteComparisons.equal(forkDeadLine, header.getDeadLine())) {
						return Success;
					}
				}
			}
			logger.error("Failed to verify poc work for block:valid deadline is {},but the block deadline is {}",deadLine,new BigInteger(1,header.getDeadLine()));
			return fault("Failed to verify poc work for block " + header.getShortDescr());
		}
	}
	
	public static BigInteger calculateValidateDeadline(byte[] genSign,long hight,byte[] coinbase,long nonce,long baseTarget) {
		if(hight == 0) {
			return BigInteger.ONE;
		}
		int scoop = PlotAlgo.calculateScoop(genSign, hight);
		return PlotAlgo.calculateDeadline(Convert.addressToNumberic(coinbase), nonce, genSign, scoop, baseTarget, hight);
	}
}
