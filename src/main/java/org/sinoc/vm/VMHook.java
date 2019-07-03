package org.sinoc.vm;

import org.sinoc.vm.program.Program;

public interface VMHook {
    void startPlay(Program program);
    void step(Program program, OpCode opcode);
    void stopPlay(Program program);
}
