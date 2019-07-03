package org.sinoc.validator;

import org.sinoc.core.BlockHeader;
import org.sinoc.util.ByteUtil;
import org.sinoc.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

public class ExtraDataPresenceRule extends BlockHeaderRule {

    public final byte[] data;

    public final boolean required;

    public ExtraDataPresenceRule(byte[] data, boolean required) {
        this.data = data;
        this.required = required;
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        final byte[] extraData = header.getExtraData() != null ? header.getExtraData() : ByteUtil.EMPTY_BYTE_ARRAY;
        final boolean extraDataMatches = FastByteComparisons.equal(extraData, data);

        if (required && !extraDataMatches) {
            return fault("Block " + header.getNumber() + " is no-fork. Expected presence of: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        } else if (!required && extraDataMatches) {
            return fault("Block " + header.getNumber() + " is pro-fork. Expected no: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        }
        return Success;
    }
}
