package org.puffinbasic.domain;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.puffinbasic.domain.Variable.VariableName;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.runtime.Formatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.ARRAY_INDEX_OUT_OF_BOUNDS;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.BAD_FIELD;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.ILLEGAL_FUNCTION_PARAM;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.NOT_INITIALIZED;

public class STObjects {

    public enum PuffinBasicTypeId {
        SCALAR,
        ARRAY,
        UDF,
        STRUCT,
        LIST,
        SET,
        DICT,
    }

    public enum PuffinBasicAtomTypeId {

        INT32('%') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                if (variable.isArray()) {
                    return new STVariable(new STInt32ArrayValue(), variable);
                } else if (variable.isUDF()) {
                    return new STUDF(new STInt32ScalarValue(), variable);
                } else if (variable.isScalar()) {
                    return new STVariable(new STInt32ScalarValue(), variable);
                } else {
                    throw new PuffinBasicInternalError("Variable type not supported: " + variable);
                }
            }

            @Override
            public STTmp createTmpEntry() {
                return new STTmp(new STInt32ScalarValue(), new ScalarType(INT32));
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == INT32 || other == INT64 || other == FLOAT || other == DOUBLE;
            }
        },
        INT64('@') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                if (variable.isArray()) {
                    return new STVariable(new STInt64ArrayValue(), variable);
                } else if (variable.isUDF()) {
                    return new STUDF(new STInt64ScalarValue(), variable);
                } else if (variable.isScalar()) {
                    return new STVariable(new STInt64ScalarValue(), variable);
                } else {
                    throw new PuffinBasicInternalError("Variable type not supported: " + variable);
                }
            }

            @Override
            public STTmp createTmpEntry() {
                return new STTmp(new STInt64ScalarValue(), new ScalarType(INT64));
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == INT32 || other == INT64 || other == FLOAT || other == DOUBLE;
            }
        },
        FLOAT('!') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                if (variable.isArray()) {
                    return new STVariable(new STFloat32ArrayValue(), variable);
                } else if (variable.isUDF()) {
                    return new STUDF(new STFloat32ScalarValue(), variable);
                } else if (variable.isScalar()) {
                    return new STVariable(new STFloat32ScalarValue(), variable);
                } else {
                    throw new PuffinBasicInternalError("Variable type not supported: " + variable);
                }
            }

            @Override
            public STTmp createTmpEntry() {
                return new STTmp(new STFloat32ScalarValue(), new ScalarType(FLOAT));
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == INT32 || other == INT64 || other == FLOAT || other == DOUBLE;
            }
        },
        DOUBLE('#') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                if (variable.isArray()) {
                    return new STVariable(new STFloat64ArrayValue(), variable);
                } else if (variable.isUDF()) {
                    return new STUDF(new STFloat64ScalarValue(), variable);
                } else if (variable.isScalar()){
                    return new STVariable(new STFloat64ScalarValue(), variable);
                } else {
                    throw new PuffinBasicInternalError("Variable type not supported: " + variable);
                }
            }

            @Override
            public STTmp createTmpEntry() {
                return new STTmp(new STFloat64ScalarValue(), new ScalarType(DOUBLE));
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == INT32 || other == INT64 || other == FLOAT || other == DOUBLE;
            }
        },
        STRING('$') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                if (variable.isArray()) {
                    return new STVariable(new STStringArrayValue(), variable);
                } else if (variable.isUDF()) {
                    return new STUDF(new STStringScalarValue(), variable);
                } else if (variable.isScalar()) {
                    String varname = variable.getVariableName().getVarname();
                    if (varname.equalsIgnoreCase("date")) {
                        return new STVariable(new STStringScalarDateValue(), variable);
                    } else if (varname.equalsIgnoreCase("time")) {
                        return new STVariable(new STStringScalarTimeValue(), variable);
                    } else {
                        return new STVariable(new STStringScalarValue(), variable);
                    }
                } else {
                    throw new PuffinBasicInternalError("Variable type not supported: " + variable);
                }
            }

            @Override
            public STTmp createTmpEntry() {
                return new STTmp(new STStringScalarValue(), new ScalarType(STRING));
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == STRING;
            }
        },
        COMPOSITE('?') {
            @Override
            public STVariable createVariableEntry(Variable variable) {
                return null;
            }

            @Override
            public STTmp createTmpEntry() {
                return null;
            }

            @Override
            public boolean isCompatibleWith(PuffinBasicAtomTypeId other) {
                return other == COMPOSITE;
            }
        }
        ;

        private static final Int2ObjectMap<PuffinBasicAtomTypeId> mapping;

        static {
            mapping = new Int2ObjectOpenHashMap<>();
            for (PuffinBasicAtomTypeId value : PuffinBasicAtomTypeId.values()) {
                mapping.put(value.repr, value);
            }
        }

        public final char repr;

        PuffinBasicAtomTypeId(char repr) {
            this.repr = repr;
        }

        public abstract STVariable createVariableEntry(Variable variable);

        public abstract STTmp createTmpEntry();

        public abstract boolean isCompatibleWith(PuffinBasicAtomTypeId other);

        public static PuffinBasicAtomTypeId lookup(String repr) {
            if (repr == null || repr.length() != 1) {
                throw new PuffinBasicInternalError(
                        "Variable suffix: '" + repr + "' is null or length != 1"
                );
            }
            var dataType = mapping.get(repr.charAt(0));
            if (dataType == null) {
                throw new PuffinBasicInternalError(
                        "Variable suffix '" + repr + "' is invalid"
                );
            }
            return dataType;
        }
    }

    public interface PuffinBasicType {
        PuffinBasicTypeId getTypeId();
        PuffinBasicAtomTypeId getAtomTypeId();
        STValue newInstance(PuffinBasicSymbolTable symbolTable);
        default boolean isCompatibleWith(PuffinBasicType other) {
            return this.equals(other);
        }
        default StructType asStruct() {
            if (getTypeId() != PuffinBasicTypeId.STRUCT) {
                throw new PuffinBasicRuntimeError(
                        BAD_FIELD,
                        "Type is not struct!"
                );
            }
            return (StructType) this;
        }
    }

    public static class ScalarType implements PuffinBasicType {
        private final PuffinBasicAtomTypeId atomType;

        public ScalarType(PuffinBasicAtomTypeId atomType) {
            this.atomType = atomType;
        }

        @Override
        public PuffinBasicTypeId getTypeId() {
            return PuffinBasicTypeId.SCALAR;
        }

        @Override
        public PuffinBasicAtomTypeId getAtomTypeId() {
            return atomType;
        }

        @Override
        public STValue newInstance(PuffinBasicSymbolTable symbolTable) {
            throw new PuffinBasicInternalError("Not implemented!");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != ScalarType.class) {
                return false;
            }
            ScalarType o = (ScalarType) obj;
            return getTypeId() == o.getTypeId()
                    && getAtomTypeId() == o.getAtomTypeId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTypeId(), getAtomTypeId());
        }

        @Override
        public boolean isCompatibleWith(PuffinBasicType other) {
            return getAtomTypeId().isCompatibleWith(other.getAtomTypeId());
        }
    }

    public static class ArrayType implements PuffinBasicType {
        private final PuffinBasicAtomTypeId atomType;

        public ArrayType(PuffinBasicAtomTypeId atomType) {
            this.atomType = atomType;
        }

        @Override
        public PuffinBasicTypeId getTypeId() {
            return PuffinBasicTypeId.ARRAY;
        }

        @Override
        public PuffinBasicAtomTypeId getAtomTypeId() {
            return atomType;
        }

        @Override
        public STValue newInstance(PuffinBasicSymbolTable symbolTable) {
            throw new PuffinBasicInternalError("Not implemented!");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != ArrayType.class) {
                return false;
            }
            ArrayType o = (ArrayType) obj;
            return getTypeId() == o.getTypeId()
                    && getAtomTypeId() == o.getAtomTypeId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTypeId(), getAtomTypeId());
        }

        @Override
        public boolean isCompatibleWith(PuffinBasicType other) {
            return getAtomTypeId().isCompatibleWith(other.getAtomTypeId());
        }
    }

    public static class UDFType implements PuffinBasicType {
        private final PuffinBasicAtomTypeId atomType;

        public UDFType(PuffinBasicAtomTypeId atomType) {
            this.atomType = atomType;
        }

        @Override
        public PuffinBasicTypeId getTypeId() {
            return PuffinBasicTypeId.UDF;
        }

        @Override
        public PuffinBasicAtomTypeId getAtomTypeId() {
            return atomType;
        }

        @Override
        public STValue newInstance(PuffinBasicSymbolTable symbolTable) {
            throw new PuffinBasicInternalError("Not implemented!");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != ArrayType.class) {
                return false;
            }
            ArrayType o = (ArrayType) obj;
            return getTypeId() == o.getTypeId()
                    && getAtomTypeId() == o.getAtomTypeId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTypeId(), getAtomTypeId());
        }

        @Override
        public boolean isCompatibleWith(PuffinBasicType other) {
            return getTypeId() == other.getTypeId()
                    && getAtomTypeId().isCompatibleWith(other.getAtomTypeId());
        }
    }

    public static final class StructType implements PuffinBasicType {
        private final String typeName;
        private final Int2ObjectMap<PuffinBasicType> memberRefIdToTypeMap;
        private final Object2IntMap<VariableName> memberNameToMemberRefIdMap;
        private int counter;

        public StructType(String typeName) {
            this.typeName = typeName;
            this.memberRefIdToTypeMap = new Int2ObjectOpenHashMap<>();
            this.memberNameToMemberRefIdMap = new Object2IntOpenHashMap<>();
        }

        public String getTypeName() {
            return typeName;
        }

        public PuffinBasicType getMemberType(VariableName memberName) {
            return memberRefIdToTypeMap.get(getMemberRefId(memberName));
        }

        public int getMemberRefId(VariableName memberName) {
            var memberRefId = memberNameToMemberRefIdMap.getOrDefault(memberName, -1);
            if (memberRefId == -1) {
                throw new PuffinBasicRuntimeError(
                        BAD_FIELD,
                        "Missing field " + typeName + "." + memberName
                );
            }
            return memberRefId;
        }

        public void declareField(VariableName memberName, PuffinBasicType type) {
            final int refId = counter++;
            memberRefIdToTypeMap.put(refId, type);
            memberNameToMemberRefIdMap.put(memberName, refId);
        }

        @Override
        public PuffinBasicTypeId getTypeId() {
            return PuffinBasicTypeId.STRUCT;
        }

        @Override
        public PuffinBasicAtomTypeId getAtomTypeId() {
            return PuffinBasicAtomTypeId.COMPOSITE;
        }

        @Override
        public STValue newInstance(PuffinBasicSymbolTable symbolTable) {
            return new STStruct(symbolTable, this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != StructType.class) {
                return false;
            }
            StructType o = (StructType) obj;
            return getTypeId() == o.getTypeId()
                    && getAtomTypeId() == o.getAtomTypeId()
                    && getTypeName().equals(o.getTypeName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTypeId(), getAtomTypeId());
        }
    }

    static final class ListType implements PuffinBasicType {

        private final PuffinBasicType type;
        private final PuffinBasicAtomTypeId atomType;

        public ListType(
                PuffinBasicType type,
                PuffinBasicAtomTypeId atomType)
        {
            this.type = type;
            this.atomType = atomType;
        }

        @Override
        public PuffinBasicTypeId getTypeId() {
            return PuffinBasicTypeId.LIST;
        }

        @Override
        public PuffinBasicAtomTypeId getAtomTypeId() {
            return atomType;
        }

        @Override
        public STValue newInstance(PuffinBasicSymbolTable symbolTable) {
            return new STList(getAtomTypeId());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != ListType.class) {
                return false;
            }
            ListType o = (ListType) obj;
            return getTypeId() == o.getTypeId()
                    && getAtomTypeId() == o.getAtomTypeId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTypeId(), getAtomTypeId());
        }
    }

    public interface STEntry {
        default boolean isLValue() {
            return false;
        }
        STValue getValue();
        PuffinBasicType getType();
    }

    public static abstract class AbstractSTEntry implements STEntry {
        private final PuffinBasicType type;
        private STValue value;

        AbstractSTEntry(STValue value, PuffinBasicType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public PuffinBasicType getType() {
            return type;
        }

        public void setValue(STValue value) {
            this.value = value;
        }

        @Override
        public STValue getValue() {
            if (value == null) {
                throw new PuffinBasicInternalError("Value is not set for type: " + getType());
            }
            return value;
        }

        public void createAndSetInstance(PuffinBasicSymbolTable symbolTable) {
            setValue(getType().newInstance(symbolTable));
        }
    }

    static class STLValue extends AbstractSTEntry {
        STLValue(STValue value, PuffinBasicType type) {
            super(value, type);
        }

        @Override
        public boolean isLValue() {
            return true;
        }
    }

    public static class STVariable extends STLValue {
        private final Variable variable;

        public STVariable(STValue value, Variable variable) {
            super(value, variable.getType());
            this.variable = variable;
        }

        public Variable getVariable() {
            return variable;
        }

    }

    static final class STTmp extends AbstractSTEntry {
        STTmp(STValue value, PuffinBasicType type) {
            super(value, type);
        }
    }

    public static final class STUDF extends STVariable {

        private final IntList paramIds;

        STUDF(STValue value, Variable variable) {
            super(value, variable);
            this.paramIds = new IntArrayList();
        }

        public void declareParam(int paramId) {
            paramIds.add(paramId);
        }

        public int getNumDeclaredParams() {
            return paramIds.size();
        }

        public int getDeclaredParam(int i) {
            return paramIds.getInt(i);
        }
    }

    static final class STLabel extends AbstractSTEntry {
        STLabel() {
            super(new STInt32ScalarValue(), null);
        }

        @Override
        public PuffinBasicType getType() {
            throw new PuffinBasicInternalError("Labels don't have a type!");
        }
    }

    public interface STValue {
        String printFormat();
        String writeFormat();
        void assign(STValue entry);
        int getInt32();
        long getInt64();
        float getFloat32();
        double getFloat64();
        int getRoundedInt32();
        long getRoundedInt64();
        String getString();
        void setInt32(int value);
        void setInt64(long value);
        void setFloat32(float value);
        void setFloat64(double value);
        void setString(String value);
        default int getFieldLength() {
            return 0;
        }
        default void setFieldLength(int fieldLength) {}
        default void setArrayDimensions(IntList dims) {}
        default IntList getArrayDimensions() {
            return new IntArrayList();
        }
        default int getTotalLength() {
            return 0;
        }
        default int getNumArrayDimensions() {
            return 0;
        }
        default void setArrayIndex(int dim, int index) {}
        default void resetArrayIndex() {}
        default int getArrayIndex1D() {
            return 0;
        }
        default void setArrayReferenceIndex1D(int index1d) {
            throw new PuffinBasicInternalError("Unsupported");
        }
        default int[] getInt32Array1D() {
            throw new PuffinBasicInternalError("Unsupported");
        }
        default void fill(Number fill) {
            throw new PuffinBasicInternalError("Unsupported");
        }
        default void fillString(String fill) {
            throw new PuffinBasicInternalError("Unsupported");
        }
        default boolean isInitialized() {
            return true;
        }
        default void checkInitialized() {
            if (!isInitialized()) {
                throw new PuffinBasicRuntimeError(
                        NOT_INITIALIZED,
                        "Value cannot be read without initializing"
                );
            }
        }
        default void setInitialized() {}
    }

    private static final class STInt32ScalarValue implements STValue {

        private boolean isSet;
        private int value;

        @Override
        public boolean isInitialized() {
            return isSet;
        }

        @Override
        public void setInitialized() {
            isSet = true;
        }

        @Override
        public String printFormat() {
            checkInitialized();
            return Formatter.printFormatInt32(value);
        }

        @Override
        public String writeFormat() {
            checkInitialized();
            return Formatter.writeFormatInt32(value);
        }

        @Override
        public void assign(STValue entry) {
            setInitialized();
            this.value = entry.getInt32();
        }

        @Override
        public int getInt32() {
            checkInitialized();
            return value;
        }

        @Override
        public long getInt64() {
            checkInitialized();
            return value;
        }

        @Override
        public float getFloat32() {
            checkInitialized();
            return value;
        }

        @Override
        public double getFloat64() {
            checkInitialized();
            return value;
        }

        @Override
        public int getRoundedInt32() {
            checkInitialized();
            return value;
        }

        @Override
        public long getRoundedInt64() {
            checkInitialized();
            return value;
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt32(int value) {
            setInitialized();
            this.value = value;
        }

        @Override
        public void setInt64(long value) {
            this.isSet = true;
            this.value = (int) value;
        }

        @Override
        public void setFloat32(float value) {
            this.isSet = true;
            this.value = (int) value;
        }

        @Override
        public void setFloat64(double value) {
            this.isSet = true;
            this.value = (int) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int32: '" + value + "'");
        }
    }

    private static final class STInt64ScalarValue implements STValue {

        private boolean isSet;
        private long value;

        @Override
        public boolean isInitialized() {
            return isSet;
        }

        @Override
        public void setInitialized() {
            isSet = true;
        }

        @Override
        public String printFormat() {
            checkInitialized();
            return Formatter.printFormatInt64(value);
        }

        @Override
        public String writeFormat() {
            checkInitialized();
            return Formatter.writeFormatInt64(value);
        }

        @Override
        public void assign(STValue entry) {
            this.isSet = true;
            this.value = entry.getInt64();
        }

        @Override
        public int getInt32() {
            checkInitialized();
            return (int) value;
        }

        @Override
        public long getInt64() {
            checkInitialized();
            return value;
        }

        @Override
        public float getFloat32() {
            checkInitialized();
            return value;
        }

        @Override
        public double getFloat64() {
            checkInitialized();
            return value;
        }

        @Override
        public int getRoundedInt32() {
            checkInitialized();
            return (int) value;
        }

        @Override
        public long getRoundedInt64() {
            checkInitialized();
            return value;
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int64 to String");
        }

        @Override
        public void setInt32(int value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setInt64(long value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setFloat32(float value) {
            this.isSet = true;
            this.value = (long) value;
        }

        @Override
        public void setFloat64(double value) {
            this.isSet = true;
            this.value = (long) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int64: '" + value + "'");
        }
    }

    private static final class STFloat32ScalarValue implements STValue {

        private boolean isSet;
        private float value;

        @Override
        public boolean isInitialized() {
            return isSet;
        }

        @Override
        public void setInitialized() {
            isSet = true;
        }

        @Override
        public String printFormat() {
            checkInitialized();
            return Formatter.printFormatFloat32(value);
        }

        @Override
        public String writeFormat() {
            checkInitialized();
            return Formatter.writeFormatFloat32(value);
        }

        @Override
        public void assign(STValue entry) {
            this.isSet = true;
            this.value = entry.getFloat32();
        }

        @Override
        public int getInt32() {
            checkInitialized();
            return (int) value;
        }

        @Override
        public long getInt64() {
            checkInitialized();
            return (long) value;
        }

        @Override
        public float getFloat32() {
            checkInitialized();
            return value;
        }

        @Override
        public double getFloat64() {
            checkInitialized();
            return value;
        }

        @Override
        public int getRoundedInt32() {
            checkInitialized();
            return Math.round(value);
        }

        @Override
        public long getRoundedInt64() {
            checkInitialized();
            return Math.round(value);
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast float32 to String");
        }

        @Override
        public void setInt32(int value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setInt64(long value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setFloat32(float value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setFloat64(double value) {
            this.isSet = true;
            this.value = (float) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to float32: '" + value + "'");
        }
    }

    private static final class STFloat64ScalarValue implements STValue {

        private boolean isSet;
        private double value;

        @Override
        public boolean isInitialized() {
            return isSet;
        }

        @Override
        public void setInitialized() {
            isSet = true;
        }

        @Override
        public String printFormat() {
            checkInitialized();
            return Formatter.printFormatFloat64(value);
        }

        @Override
        public String writeFormat() {
            checkInitialized();
            return Formatter.writeFormatFloat64(value);
        }

        @Override
        public void assign(STValue entry) {
            this.isSet = true;
            this.value = entry.getFloat64();
        }

        @Override
        public int getInt32() {
            checkInitialized();
            return (int) value;
        }

        @Override
        public long getInt64() {
            checkInitialized();
            return (long) value;
        }

        @Override
        public float getFloat32() {
            checkInitialized();
            return (float) value;
        }

        @Override
        public double getFloat64() {
            checkInitialized();
            return value;
        }

        @Override
        public int getRoundedInt32() {
            checkInitialized();
            return (int) Math.round(value);
        }

        @Override
        public long getRoundedInt64() {
            checkInitialized();
            return Math.round(value);
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast float64 to String");
        }

        @Override
        public void setInt32(int value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setInt64(long value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setFloat32(float value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setFloat64(double value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to float64: '" + value + "'");
        }
    }

    private static final class STStringScalarValue implements STValue {

        private boolean isSet;
        private int fieldLength;
        private String value = "";

        @Override
        public boolean isInitialized() {
            return isSet;
        }

        @Override
        public void setInitialized() {
            isSet = true;
        }

        @Override
        public String printFormat() {
            checkInitialized();
            return Formatter.printFormatString(value);
        }

        @Override
        public String writeFormat() {
            checkInitialized();
            return Formatter.writeFormatString(value);
        }

        @Override
        public void assign(STValue entry) {
            this.isSet = true;
            this.value = entry.getString();
        }

        @Override
        public int getInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public float getFloat32() {
            throw new PuffinBasicInternalError("Can't cast String to float32");
        }

        @Override
        public double getFloat64() {
            throw new PuffinBasicInternalError("Can't cast String to float64");
        }

        @Override
        public int getRoundedInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getRoundedInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public String getString() {
            checkInitialized();
            return value;
        }

        @Override
        public void setInt32(int value) {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt64(long value) {
            throw new PuffinBasicInternalError("Can't cast int64 to String");
        }

        @Override
        public void setFloat32(float value) {
            throw new PuffinBasicInternalError("Can't cast float32 to String");
        }

        @Override
        public void setFloat64(double value) {
            throw new PuffinBasicInternalError("Can't cast float64 to String");
        }

        @Override
        public void setString(String value) {
            this.isSet = true;
            this.value = value;
        }

        @Override
        public int getFieldLength() {
            return fieldLength;
        }

        @Override
        public void setFieldLength(int fieldLength) {
            this.fieldLength = fieldLength;
        }
    }

    private static final class STStringScalarTimeValue implements STValue {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
        private LocalTime time;

        @Override
        public String printFormat() {
            return getString();
        }

        @Override
        public String writeFormat() {
            return getString();
        }

        @Override
        public void assign(STValue entry) {
            setString(entry.getString());
        }

        @Override
        public int getInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public float getFloat32() {
            throw new PuffinBasicInternalError("Can't cast String to float32");
        }

        @Override
        public double getFloat64() {
            throw new PuffinBasicInternalError("Can't cast String to float64");
        }

        @Override
        public int getRoundedInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getRoundedInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public String getString() {
            return formatLocalTime(time != null ? time : LocalTime.now());
        }

        private String formatLocalTime(LocalTime time) {
            return time.format(FORMATTER);
        }

        @Override
        public void setInt32(int value) {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt64(long value) {
            throw new PuffinBasicInternalError("Can't cast int64 to String");
        }

        @Override
        public void setFloat32(float value) {
            throw new PuffinBasicInternalError("Can't cast float32 to String");
        }

        @Override
        public void setFloat64(double value) {
            throw new PuffinBasicInternalError("Can't cast float64 to String");
        }

        @Override
        public void setString(String value) {
            this.time = LocalTime.parse(value, FORMATTER);
        }

        @Override
        public int getFieldLength() {
            return 0;
        }

        @Override
        public void setFieldLength(int fieldLength) {
            throw new PuffinBasicRuntimeError(
                    ILLEGAL_FUNCTION_PARAM,
                    "TIME$ cannot be used for setting field length!"
            );
        }
    }

    private static final class STStringScalarDateValue implements STValue {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
        private LocalDate date;

        @Override
        public String printFormat() {
            return getString();
        }

        @Override
        public String writeFormat() {
            return getString();
        }

        @Override
        public void assign(STValue entry) {
            setString(entry.getString());
        }

        @Override
        public int getInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public float getFloat32() {
            throw new PuffinBasicInternalError("Can't cast String to float32");
        }

        @Override
        public double getFloat64() {
            throw new PuffinBasicInternalError("Can't cast String to float64");
        }

        @Override
        public int getRoundedInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getRoundedInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public String getString() {
            return formatLocalDate(date != null ? date : LocalDate.now());
        }

        private String formatLocalDate(LocalDate date) {
            return date.format(FORMATTER);
        }

        @Override
        public void setInt32(int value) {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt64(long value) {
            throw new PuffinBasicInternalError("Can't cast int64 to String");
        }

        @Override
        public void setFloat32(float value) {
            throw new PuffinBasicInternalError("Can't cast float32 to String");
        }

        @Override
        public void setFloat64(double value) {
            throw new PuffinBasicInternalError("Can't cast float64 to String");
        }

        @Override
        public void setString(String value) {
            this.date = LocalDate.parse(value, FORMATTER);
        }

        @Override
        public int getFieldLength() {
            return 0;
        }

        @Override
        public void setFieldLength(int fieldLength) {
            throw new PuffinBasicRuntimeError(
                    ILLEGAL_FUNCTION_PARAM,
                    "DATE$ cannot be used for setting field length!"
            );
        }
    }

    static class ArrayReferenceValue implements STValue {

        private final STVariable variable;
        private int index1d;

        ArrayReferenceValue(STVariable variable) {
            this.variable = variable;
        }

        private AbstractSTArrayValue getValue() {
            return (AbstractSTArrayValue) variable.getValue();
        }

        @Override
        public void setArrayReferenceIndex1D(int index1d) {
            this.index1d = index1d;
        }

        @Override
        public String printFormat() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.printFormat();
        }

        @Override
        public String writeFormat() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.writeFormat();
        }

        @Override
        public void assign(STValue entry) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.assign(entry);
        }

        @Override
        public int getInt32() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getInt32();
        }

        @Override
        public long getInt64() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getInt64();
        }

        @Override
        public float getFloat32() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getFloat32();
        }

        @Override
        public double getFloat64() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getFloat64();
        }

        @Override
        public int getRoundedInt32() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getRoundedInt32();
        }

        @Override
        public long getRoundedInt64() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getRoundedInt64();
        }

        @Override
        public String getString() {
            var array = getValue();
            array.setArrayIndexID(index1d);
            return array.getString();
        }

        @Override
        public void setInt32(int value) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.setInt32(value);
        }

        @Override
        public void setInt64(long value) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.setInt64(value);
        }

        @Override
        public void setFloat32(float value) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.setFloat32(value);
        }

        @Override
        public void setFloat64(double value) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.setFloat64(value);
        }

        @Override
        public void setString(String value) {
            var array = getValue();
            array.setArrayIndexID(index1d);
            array.setString(value);
        }
    }

    static abstract class AbstractSTArrayValue implements STValue {

        private IntList dimensions;
        private int totalLength;
        private int index1d;
        private int ndim;

        @Override
        public int getTotalLength() {
            return totalLength;
        }

        @Override
        public int getNumArrayDimensions() {
            return ndim;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            this.dimensions = new IntArrayList(dims);
            this.ndim = dimensions.size();
            int totalLen = 1;
            for (int i = 0; i < ndim; i++) {
                totalLen *= dimensions.getInt(i);
            }
            totalLength = totalLen;
        }

        @Override
        public IntList getArrayDimensions() {
            return dimensions;
        }

        @Override
        public void resetArrayIndex() {
            this.index1d = 0;
        }

        @Override
        public void setArrayIndex(int dim, int index) {
            if (dim < 0 || dim >= dimensions.size()) {
                throw new PuffinBasicRuntimeError(
                        ARRAY_INDEX_OUT_OF_BOUNDS,
                        "Dimension index " + dim + " is out of range, #dims=" + dimensions.size()
                );
            }
            if (index < 0 || index >= dimensions.getInt(dim)) {
                throw new PuffinBasicRuntimeError(
                        ARRAY_INDEX_OUT_OF_BOUNDS,
                        "Index " + index + " is out of range for dimension["
                                + dim + "]=" + dimensions.getInt(dim)
                );
            }
            this.index1d = this.index1d + (dim + 1 < ndim ? dimensions.getInt(dim + 1) : 1) * index;
        }

        @Override
        public int getArrayIndex1D() {
            return index1d;
        }

        public void setArrayIndexID(int index1d) {
            this.index1d = index1d;
        }
    }

    public static final class STInt32ArrayValue extends AbstractSTArrayValue {

        private int[] value;

        @Override
        public void fill(Number fill) {
            Arrays.fill(value, fill.intValue());
        }

        public int[] getValue() {
            return value;
        }

        @Override
        public int[] getInt32Array1D() {
            return value;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            super.setArrayDimensions(dims);
            this.value = new int[getTotalLength()];
        }

        @Override
        public String printFormat() {
            return Formatter.printFormatInt32(value[getArrayIndex1D()]);
        }

        @Override
        public String writeFormat() {
            return Formatter.writeFormatInt32(value[getArrayIndex1D()]);
        }

        @Override
        public void assign(STValue entry) {
            value[getArrayIndex1D()] = entry.getInt32();
        }

        @Override
        public int getInt32() {
            return value[getArrayIndex1D()];
        }

        @Override
        public long getInt64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public float getFloat32() {
            return value[getArrayIndex1D()];
        }

        @Override
        public double getFloat64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public int getRoundedInt32() {
            return value[getArrayIndex1D()];
        }

        @Override
        public long getRoundedInt64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt32(int value) {
            this.value[getArrayIndex1D()] = value;
        }

        @Override
        public void setInt64(long value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat32(float value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat64(double value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int32: '" + value + "'");
        }
    }

    public static final class STInt64ArrayValue extends AbstractSTArrayValue {

        private long[] value;

        @Override
        public void fill(Number fill) {
            Arrays.fill(value, fill.longValue());
        }

        public long[] getValue() {
            return value;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            super.setArrayDimensions(dims);
            this.value = new long[getTotalLength()];
        }

        @Override
        public String printFormat() {
            return Formatter.printFormatInt64(value[getArrayIndex1D()]);
        }

        @Override
        public String writeFormat() {
            return Formatter.writeFormatInt64(value[getArrayIndex1D()]);
        }

        @Override
        public void assign(STValue entry) {
            value[getArrayIndex1D()] = entry.getInt64();
        }

        @Override
        public int getInt32() {
            return (int) value[getArrayIndex1D()];
        }

        @Override
        public long getInt64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public float getFloat32() {
            return value[getArrayIndex1D()];
        }

        @Override
        public double getFloat64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public int getRoundedInt32() {
            return (int) value[getArrayIndex1D()];
        }

        @Override
        public long getRoundedInt64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt32(int value) {
            this.value[getArrayIndex1D()] = value;
        }

        @Override
        public void setInt64(long value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat32(float value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat64(double value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int32: '" + value + "'");
        }
    }

    public static final class STFloat32ArrayValue extends AbstractSTArrayValue {

        private float[] value;

        @Override
        public void fill(Number fill) {
            Arrays.fill(value, fill.floatValue());
        }

        public float[] getValue() {
            return value;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            super.setArrayDimensions(dims);
            this.value = new float[getTotalLength()];
        }

        @Override
        public String printFormat() {
            return Formatter.printFormatFloat32(value[getArrayIndex1D()]);
        }

        @Override
        public String writeFormat() {
            return Formatter.writeFormatFloat32(value[getArrayIndex1D()]);
        }

        @Override
        public void assign(STValue entry) {
            value[getArrayIndex1D()] = entry.getFloat32();
        }

        @Override
        public int getInt32() {
            return (int) value[getArrayIndex1D()];
        }

        @Override
        public long getInt64() {
            return (long) value[getArrayIndex1D()];
        }

        @Override
        public float getFloat32() {
            return value[getArrayIndex1D()];
        }

        @Override
        public double getFloat64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public int getRoundedInt32() {
            return Math.round(value[getArrayIndex1D()]);
        }

        @Override
        public long getRoundedInt64() {
            return Math.round(value[getArrayIndex1D()]);
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt32(int value) {
            this.value[getArrayIndex1D()] = value;
        }

        @Override
        public void setInt64(long value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat32(float value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat64(double value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int32: '" + value + "'");
        }
    }

    public static final class STFloat64ArrayValue extends AbstractSTArrayValue {

        private double[] value;

        @Override
        public void fill(Number fill) {
            Arrays.fill(value, fill.doubleValue());
        }

        public double[] getValue() {
            return value;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            super.setArrayDimensions(dims);
            this.value = new double[getTotalLength()];
        }

        @Override
        public String printFormat() {
            return Formatter.printFormatFloat64(value[getArrayIndex1D()]);
        }

        @Override
        public String writeFormat() {
            return Formatter.writeFormatFloat64(value[getArrayIndex1D()]);
        }

        @Override
        public void assign(STValue entry) {
            value[getArrayIndex1D()] = entry.getFloat64();
        }

        @Override
        public int getInt32() {
            return (int) value[getArrayIndex1D()];
        }

        @Override
        public long getInt64() {
            return (long) value[getArrayIndex1D()];
        }

        @Override
        public float getFloat32() {
            return (float) value[getArrayIndex1D()];
        }

        @Override
        public double getFloat64() {
            return value[getArrayIndex1D()];
        }

        @Override
        public int getRoundedInt32() {
            return (int) Math.round(value[getArrayIndex1D()]);
        }

        @Override
        public long getRoundedInt64() {
            return Math.round(value[getArrayIndex1D()]);
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt32(int value) {
            this.value[getArrayIndex1D()] = value;
        }

        @Override
        public void setInt64(long value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat32(float value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setFloat64(double value) {
            this.value[getArrayIndex1D()] = (int) value;
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Can't cast String to int32: '" + value + "'");
        }
    }

    public static final class STStringArrayValue extends AbstractSTArrayValue {

        private String[] value;

        @Override
        public void fillString(String fill) {
            Arrays.fill(value, fill);
        }

        public String[] getValue() {
            return value;
        }

        @Override
        public void setArrayDimensions(IntList dims) {
            super.setArrayDimensions(dims);
            this.value = new String[getTotalLength()];
            Arrays.fill(value, 0, value.length, "");
        }

        @Override
        public String printFormat() {
            return Formatter.printFormatString(value[getArrayIndex1D()]);
        }

        @Override
        public String writeFormat() {
            return Formatter.writeFormatString(value[getArrayIndex1D()]);
        }

        @Override
        public void assign(STValue entry) {
            value[getArrayIndex1D()] = entry.getString();
        }

        @Override
        public int getInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public float getFloat32() {
            throw new PuffinBasicInternalError("Can't cast String to float32");
        }

        @Override
        public double getFloat64() {
            throw new PuffinBasicInternalError("Can't cast String to float64");
        }

        @Override
        public int getRoundedInt32() {
            throw new PuffinBasicInternalError("Can't cast String to int32");
        }

        @Override
        public long getRoundedInt64() {
            throw new PuffinBasicInternalError("Can't cast String to int64");
        }

        @Override
        public String getString() {
            return value[getArrayIndex1D()];
        }

        @Override
        public void setInt32(int value) {
            throw new PuffinBasicInternalError("Can't cast int32 to String");
        }

        @Override
        public void setInt64(long value) {
            throw new PuffinBasicInternalError("Can't cast int64 to String");
        }

        @Override
        public void setFloat32(float value) {
            throw new PuffinBasicInternalError("Can't cast float32 to String");
        }

        @Override
        public void setFloat64(double value) {
            throw new PuffinBasicInternalError("Can't cast float64 to String");
        }

        @Override
        public void setString(String value) {
            this.value[getArrayIndex1D()] = value;
        }
    }

    static abstract class STCompositeValue implements STValue {
        private final PuffinBasicTypeId type;
        private final PuffinBasicAtomTypeId atomType;

        STCompositeValue(
                PuffinBasicTypeId type,
                PuffinBasicAtomTypeId atomType)
        {
            this.type = type;
            this.atomType = atomType;
        }

        @Override
        public String printFormat() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public String writeFormat() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void assign(STValue entry) {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public int getInt32() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public long getInt64() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public float getFloat32() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public double getFloat64() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public int getRoundedInt32() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public long getRoundedInt64() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public String getString() {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void setInt32(int value) {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void setInt64(long value) {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void setFloat32(float value) {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void setFloat64(double value) {
            throw new PuffinBasicInternalError("Not implemented");
        }

        @Override
        public void setString(String value) {
            throw new PuffinBasicInternalError("Not implemented");
        }
    }

    static final class STList extends STCompositeValue {
        private final List<Object> list;

        public STList(PuffinBasicAtomTypeId atomType) {
            super(PuffinBasicTypeId.LIST, atomType);
            this.list = new ArrayList<>();
        }
    }

    public static final class STStruct extends STCompositeValue {
        private final StructType structType;
        private final Int2IntMap memberRefIdToValueId;

        public STStruct(PuffinBasicSymbolTable symbolTable, StructType type) {
            super(PuffinBasicTypeId.STRUCT, PuffinBasicAtomTypeId.COMPOSITE);
            this.structType = type;
            this.memberRefIdToValueId = new Int2IntOpenHashMap();
            for (var entry : structType.memberNameToMemberRefIdMap.object2IntEntrySet()) {
                var memberRefId = entry.getIntValue();
                var valueType = structType.memberRefIdToTypeMap.get(memberRefId);
                var valueId = symbolTable.addTmp(valueType, valueType.getAtomTypeId(), e -> e.getValue().setInitialized());
                this.memberRefIdToValueId.put(memberRefId, valueId);
            }
        }

        public int getMember(int memberRefId) {
            return memberRefIdToValueId.getOrDefault(memberRefId, -1);
        }

        @Override
        public void assign(STValue entry) {
            this.memberRefIdToValueId.clear();
            this.memberRefIdToValueId.putAll(((STStruct) entry).memberRefIdToValueId);
        }
    }
}
