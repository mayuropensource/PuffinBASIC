package org.puffinbasic.domain;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.puffinbasic.domain.STObjects.ArrayType;
import org.puffinbasic.domain.STObjects.PuffinBasicCompositeTypeBase;
import org.puffinbasic.domain.STObjects.PuffinBasicDataType;
import org.puffinbasic.domain.STObjects.ScalarType;
import org.puffinbasic.domain.STObjects.UDFType;
import org.puffinbasic.error.PuffinBasicSemanticError;

import java.util.Objects;
import java.util.function.Supplier;

import static org.puffinbasic.domain.STObjects.PuffinBasicCompositeType.ARRAY;
import static org.puffinbasic.domain.STObjects.PuffinBasicCompositeType.SCALAR;
import static org.puffinbasic.domain.STObjects.PuffinBasicCompositeType.UDF;

public class Variable {

    public static final class VariableName {
        private final String varname;
        private final PuffinBasicDataType dataType;

        public VariableName(
                @NotNull String varname,
                @NotNull PuffinBasicDataType dataType)
        {
            this.varname = Preconditions.checkNotNull(varname);
            this.dataType = Preconditions.checkNotNull(dataType);
        }

        public String getVarname() {
            return varname;
        }

        public PuffinBasicDataType getDataType() {
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

    public static Variable of(
            @NotNull VariableName variableName,
            boolean isArray,
            Supplier<String> lineSupplier)
    {
        if (isArray) {
            if (!variableName.varname.startsWith(UDF_PREFIX)) {
                return new Variable(variableName, new ArrayType(variableName.getDataType()));
            } else {
                throw new PuffinBasicSemanticError(
                        PuffinBasicSemanticError.ErrorCode.ARRAY_VARIABLE_CANNOT_STARTWITH_FN,
                        lineSupplier.get(),
                        "Array variable cannot start with " + UDF_PREFIX + ": " + variableName.varname);
            }
        } else {
            if (variableName.varname.startsWith(UDF_PREFIX)) {
                return new Variable(variableName, new UDFType(variableName.getDataType()));
            } else {
                return new Variable(variableName, new ScalarType(variableName.getDataType()));
            }
        }
    }

    private final VariableName variableName;
    private final PuffinBasicCompositeTypeBase type;

    public Variable(
            @NotNull VariableName variableName,
            @NotNull PuffinBasicCompositeTypeBase type)
    {
        this.variableName = Preconditions.checkNotNull(variableName);
        this.type = Preconditions.checkNotNull(type);
    }

    public VariableName getVariableName() {
        return variableName;
    }

    public PuffinBasicCompositeTypeBase getType() {
        return type;
    }

    public boolean isScalar() {
        return type.getCompositeType() == SCALAR;
    }

    public boolean isArray() {
        return type.getCompositeType() == ARRAY;
    }

    public boolean isUDF() {
        return type.getCompositeType() == UDF;
    }

    @Override
    public String toString() {
        return variableName + ":" + type.getCompositeType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return variableName.equals(variable.variableName) &&
                type.equals(variable.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName, type);
    }
}
