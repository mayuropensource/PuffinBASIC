package org.beaglebasic.domain;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.beaglebasic.domain.STObjects.BeagleBasicDataType;
import org.beaglebasic.domain.STObjects.STEntry;
import org.beaglebasic.domain.STObjects.STVariable;
import org.beaglebasic.domain.Variable.VariableName;
import org.beaglebasic.error.BeagleBasicInternalError;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.DOUBLE;

public class BeagleBasicSymbolTable {

    public static final int NULL_ID = -1;

    private final Char2ObjectMap<BeagleBasicDataType> defaultDataTypes;
    private final AtomicInteger idmaker;
    private Scope currentScope;

    public BeagleBasicSymbolTable() {
        this.idmaker = new AtomicInteger();
        this.defaultDataTypes = new Char2ObjectOpenHashMap<>();
        this.currentScope = new Scope(null);
    }

    private int generateNextId() {
        return idmaker.incrementAndGet();
    }

    public Scope getCurrentScope() {
        return currentScope;
    }

    private Optional<Scope> findScope(Predicate<Scope> predicate) {
        var scope = getCurrentScope();
        while (scope != null) {
            if (predicate.test(scope)) {
                return Optional.of(scope);
            } else {
                scope = scope.getParent();
            }
        }
        return Optional.empty();
    }

    public STEntry get(int id) {
        return findScope(s -> s.containsId(id)).orElseThrow(
                () -> new BeagleBasicInternalError("Failed to find entry for id: " + id)
        ).getEntry(id);
    }

    public int addVariableOrUDF(
            VariableName variableName,
            Function<VariableName, Variable> variableCreator,
            BiConsumer<Integer, STVariable> consumer)
    {
        var scope = findScope(s -> s.containsVariable(variableName)).orElse(getCurrentScope());
        int id = scope.getIdForVariable(variableName);
        final STVariable entry;
        if (id == -1) {
            id = generateNextId();
            scope.putVariable(variableName, id);
            var variable = variableCreator.apply(variableName);
            entry = variableName.getDataType().createVariableEntry(variable);
            scope.putEntry(id, entry);
        } else {
            entry = (STVariable) get(id);
        }
        consumer.accept(id, entry);
        return id;
    }

    public int addLabel() {
        var scope = getCurrentScope();
        var id = generateNextId();
        var entry = new STObjects.STLabel();
        scope.putEntry(id, entry);
        return id;
    }

    public int addGotoTarget() {
        var scope = getCurrentScope();
        int id = generateNextId();
        var entry = BeagleBasicDataType.INT32.createTmpEntry();
        scope.putEntry(id, entry);
        return id;
    }

    public int addTmp(BeagleBasicDataType dataType, Consumer<STEntry> consumer) {
        var scope = getCurrentScope();
        int id = generateNextId();
        var entry = dataType.createTmpEntry();
        scope.putEntry(id, entry);
        consumer.accept(entry);
        return id;
    }

    public int addTmpCompatibleWith(int srcId) {
        var scope = getCurrentScope();
        var dataType = scope.getEntry(srcId).getValue().getDataType();
        int id = generateNextId();
        scope.putEntry(id, dataType.createTmpEntry());
        return id;
    }

    public BeagleBasicDataType getDataTypeFor(String varname, String suffix) {
        if (varname.length() == 0) {
            throw new BeagleBasicInternalError("Empty variable name: " + varname);
        }
        if (suffix == null) {
            var firstChar = varname.charAt(0);
            return defaultDataTypes.getOrDefault(firstChar, DOUBLE);
        } else {
            return BeagleBasicDataType.lookup(suffix);
        }
    }

    public void setDefaultDataType(char c, BeagleBasicDataType dataType) {
        defaultDataTypes.put(c, dataType);
    }

    public void pushDeclarationScope(int funcId) {
        currentScope = getCurrentScope().createChild(funcId);
    }

    public void pushRuntimeScope(int funcId, int callerInstrId) {
        var funcDeclScope = getCurrentScope().getChild(funcId);
        if (funcDeclScope == null) {
            throw new BeagleBasicInternalError("Failed to find scope for id: " + funcId);
        }
        currentScope = funcDeclScope.createRuntimeScope(callerInstrId);;
    }

    public void popScope() {
        var parent = getCurrentScope().getParent();
        if (parent == null) {
            throw new BeagleBasicInternalError("Scope underflow!");
        }
        currentScope = parent;
    }
}
