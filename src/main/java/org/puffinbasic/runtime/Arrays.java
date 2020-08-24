package org.puffinbasic.runtime;

import org.puffinbasic.parser.PuffinBasicIR.Instruction;
import org.puffinbasic.domain.PuffinBasicSymbolTable;

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
    public static void resetIndex(ArrayState state, PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        state.reset();
        symbolTable.get(instruction.op1).getValue().resetArrayIndex();
    }

    public static void setIndex(ArrayState state, PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        int index = symbolTable.get(instruction.op2).getValue().getInt32();
        symbolTable.get(instruction.op1).getValue().setArrayIndex(state.getAndIncrement(), index);
    }

    public static void arrayref(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var index = symbolTable.get(instruction.op1).getValue().getArrayIndex1D();
        symbolTable.get(instruction.result).getValue().setArrayReferenceIndex1D(index);
    }
}
