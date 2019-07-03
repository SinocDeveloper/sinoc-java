package org.sinoc.mine.plot;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.sinoc.crypto.cryptohash.Shabal256;

public class PlotAlgo {
	

	public static int calculateScoop(byte[] genSig, long height) {
		ByteBuffer posbuf = ByteBuffer.allocate(32 + 8);
		posbuf.put(genSig);
		posbuf.putLong(height);
		Shabal256 md = new Shabal256();
		md.update(posbuf.array());
		BigInteger hashnum = new BigInteger(1, md.digest());
		return hashnum.mod(BigInteger.valueOf(MiningPlot.SCOOPS_PER_PLOT)).intValue();
	}

	public static BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, long blockHeight) {
		MiningPlot plot = new MiningPlot(accountId, nonce);
		Shabal256 md = new Shabal256();
		md.update(genSig);
		plot.hashScoop(md, scoop);
		byte[] hash = md.digest();
		return new BigInteger(1, new byte[] { hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0] });
	}

	public static BigInteger calculateDeadline(long accountId, long nonce, byte[] genSig, int scoop, long baseTarget,long blockHeight) {
		BigInteger hit = calculateHit(accountId, nonce, genSig, scoop, blockHeight);
		return hit.divide(BigInteger.valueOf(baseTarget));
	}

	public static BigInteger calculateHit(long accountId, long nonce, byte[] genSig, byte[] scoopData) {
		Shabal256 md = new Shabal256();
		md.update(genSig);
		md.update(scoopData);
		byte[] hash = md.digest();
		return new BigInteger(1, new byte[] { hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0] });
	}
	
}
