package org.sinoc.vm.trace;

import java.math.BigInteger;

import org.sinoc.vm.OpCode;

public class Op {

    private OpCode code;
    private int deep;
    private int pc;
    private BigInteger gas;
    private OpActions actions;

    public OpCode getCode() {
        return code;
    }

    public void setCode(OpCode code) {
        this.code = code;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public OpActions getActions() {
        return actions;
    }

    public void setActions(OpActions actions) {
        this.actions = actions;
    }
}
