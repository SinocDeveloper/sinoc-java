package org.sinoc.contrdata.contract;


import java.util.*;
import org.sinoc.vm.*;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.math.*;
import java.util.regex.*;

public class Member
{
    public static final int BYTES_IN_SLOT = 32;
    private static final int BITS_IN_BYTE = 8;
    private static final Pattern BYTES_TYPE_PATTERN;
    private static final Pattern INT_TYPE_PATTERN;
    private static final int BITS_IN_SLOT = 256;
    private final Member prev;
    private final int position;
    private final Ast.Type type;
    private final String name;
    private final boolean packed;
    private final ContractData contractData;
    private final int slotFreeSpace;
    
    public Member(final Member prev, final Ast.Variable variable, final ContractData contractData) {
        this.contractData = contractData;
        this.name = variable.getName();
        this.type = variable.getType();
        this.prev = prev;
        final int typeSize = size(this.getType());
        if (this.hasPrev()) {
            this.packed = (this.getPrev().getSlotFreeSpace() >= typeSize);
            this.slotFreeSpace = (this.isPacked() ? this.getPrev().getSlotFreeSpace() : 32) - typeSize;
            this.position = this.getPrev().getPosition() + 1;
        }
        else {
            this.packed = false;
            this.slotFreeSpace = 32 - typeSize;
            this.position = 0;
        }
    }
    
    public boolean hasPrev() {
        return Objects.nonNull(this.prev);
    }
    
    public int reservedSlotsCount() {
        if (this.type.isStruct()) {
            return this.contractData.getStructFields(this.type.asStruct()).reservedSlotsCount();
        }
        if (!this.type.isStaticArray()) {
            return this.isPacked() ? 0 : 1;
        }
        final int arrSize = this.type.asArray().getSize();
        final Ast.Type nestedType = this.type.asArray().getElementType();
        if (nestedType.isStruct()) {
            final Ast.Type.Struct struct = this.type.asArray().getElementType().asStruct();
            return arrSize * this.contractData.getStructFields(struct).reservedSlotsCount();
        }
        return (int)Math.ceil(arrSize * size(nestedType) / 32.0f);
    }
    
    public int getStorageIndex() {
        int result = 0;
        if (this.hasPrev()) {
            result = this.getPrev().getStorageIndex() + (this.isPacked() ? 0 : this.getFirstPrevNonPacked().reservedSlotsCount());
        }
        return result;
    }
    
    private Member getFirstPrevNonPacked() {
        Member result = null;
        if (this.hasPrev()) {
            for (result = this.getPrev(); result.isPacked(); result = result.getPrev()) {}
        }
        return result;
    }
    
    public DataWord extractValue(DataWord slot) {
        if (slot == null) {
            return null;
        }
        final int size = size(this.getType());
        final int from = this.getSlotFreeSpace();
        return new DataWord(ArrayUtils.subarray(slot.getData(), from, from + size));
    }
    
    static int size(final Ast.Type type) {
        int result = 32;
        if (type.isEnum()) {
            result = 1;
        }
        else if (type.isElementary()) {
            if (type.is("bool")) {
                result = 1;
            }
            else if (type.is("address")) {
                result = 20;
            }
            else if (type.is(name -> name.startsWith("bytes"))) {
                result = size(type, Member.BYTES_TYPE_PATTERN, 32);
            }
            else if (type.is(name -> name.contains("int"))) {
                result = size(type, Member.INT_TYPE_PATTERN, BITS_IN_SLOT) / BITS_IN_BYTE;
            }
        }
        return result;
    }
    
    private static int size(final Ast.Type type, final Pattern pattern, final int defaultSize) {
        int result = defaultSize;
        final Matcher matcher = pattern.matcher(type.getName());
        if (matcher.matches()) {
            result = NumberUtils.toInt(matcher.group(1), defaultSize);
        }
        return result;
    }
    
    public Member getPrev() {
        return this.prev;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public Ast.Type getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isPacked() {
        return this.packed;
    }
    
    public ContractData getContractData() {
        return this.contractData;
    }
    
    public int getSlotFreeSpace() {
        return this.slotFreeSpace;
    }
    
    static {
        BYTES_TYPE_PATTERN = Pattern.compile("^bytes(\\d{0,2})$");
        INT_TYPE_PATTERN = Pattern.compile("^u?int(\\d{0,3})$");
    }
}
