package org.beaglebasic.runtime;

import org.beaglebasic.parser.BeagleBasicIR.Instruction;
import org.beaglebasic.domain.BeagleBasicSymbolTable;

public class Arrays {

    public static final class ArrayState {
        private int dimIndex;

        public int getAndIncrement() {
            return dimIndex++;
        }

        public void reset() {
            dimIndex = 0;
        }
    }

    public static void resetIndex(ArrayState state, BeagleBasicSymbolTable symbolTable, Instruction instruction) {
        state.reset();
        symbolTable.get(instruction.op1).getValue().resetArrayIndex();
    }

    public static void setIndex(ArrayState state, BeagleBasicSymbolTable symbolTable, Instruction instruction) {
        int index = symbolTable.get(instruction.op2).getValue().getInt32();
        symbolTable.get(instruction.op1).getValue().setArrayIndex(state.getAndIncrement(), index);
    }
}
