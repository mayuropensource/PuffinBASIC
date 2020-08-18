package org.puffinbasic.domain;

import com.google.common.base.Preconditions;
import org.puffinbasic.error.PuffinBasicSemanticError;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class Variable {

    public static final class VariableName {
        private final String varname;
        private final STObjects.PuffinBasicDataType dataType;

        public VariableName(
                @NotNull String varname,
                @NotNull STObjects.PuffinBasicDataType dataType)
        {
            this.varname = Preconditions.checkNotNull(varname);
            this.dataType = Preconditions.checkNotNull(dataType);
        }

        public String getVarname() {
            return varname;
        }

        public STObjects.PuffinBasicDataType getDataType() {
            return dataType;
        }

        @Override
        public String toString() {
            return varname + ":" + dataType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VariableName variable = (VariableName) o;
            return varname.equals(variable.varname) &&
                    dataType == variable.dataType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(varname, dataType);
        }
    }

    public static final String UDF_PREFIX ="FN";

    public enum Kind {
        SCALAR,
        ARRAY,
        UDF
    }

    public static Variable of(
            @NotNull VariableName variableName,
            boolean isArray,
            Supplier<String> lineSupplier)
    {
        if (isArray) {
            if (!variableName.varname.startsWith(UDF_PREFIX)) {
                return new Variable(variableName, Kind.ARRAY);
            } else {
                throw new PuffinBasicSemanticError(
                        PuffinBasicSemanticError.ErrorCode.ARRAY_VARIABLE_CANNOT_STARTWITH_FN,
                        lineSupplier.get(),
                        "Array variable cannot start with " + UDF_PREFIX + ": " + variableName.varname);
            }
        } else {
            if (variableName.varname.startsWith(UDF_PREFIX)) {
                return new Variable(variableName, Kind.UDF);
            } else {
                return new Variable(variableName, Kind.SCALAR);
            }
        }
    }

    private final VariableName variableName;
    private final Kind kind;

    public Variable(
            @NotNull VariableName variableName,
            @NotNull Kind kind)
    {
        this.variableName = Preconditions.checkNotNull(variableName);
        this.kind = Preconditions.checkNotNull(kind);
    }

    public VariableName getVariableName() {
        return variableName;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isScalar() {
        return kind == Kind.SCALAR;
    }

    public boolean isArray() {
        return kind == Kind.ARRAY;
    }

    public boolean isUDF() {
        return kind == Kind.UDF;
    }

    @Override
    public String toString() {
        return variableName + ":" + kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return variableName.equals(variable.variableName) &&
                kind == variable.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, kind);
    }
}
