package org.beaglebasic.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.beaglebasic.domain.STObjects.STEntry;
import org.beaglebasic.domain.Variable.VariableName;

public final class Scope {
    private final Scope parent;
    private final int callerInstrId;
    private final Int2ObjectMap<Scope> funcIdToScope;
    private final Int2ObjectMap<STEntry> entryMap;
    private final Object2IntMap<VariableName> variableNameToEntry;

    public Scope(Scope parent) {
        this(parent,
                BeagleBasicSymbolTable.NULL_ID,
                new Int2ObjectOpenHashMap<>(),
                new Int2ObjectOpenHashMap<>(),
                new Object2IntOpenHashMap<>());
    }

    private Scope(
            Scope parent,
            int callerInstrId,
            Int2ObjectMap<Scope> funcIdToScope,
            Int2ObjectMap<STEntry> entryMap,
            Object2IntMap<VariableName> variableNameToEntry)
    {
        this.parent = parent;
        this.callerInstrId = callerInstrId;
        this.funcIdToScope = funcIdToScope;
        this.entryMap = entryMap;
        this.variableNameToEntry = variableNameToEntry;
    }

    public Scope createRuntimeScope(int callerInstrId) {
        return new Scope(
                parent,
                callerInstrId,
                new Int2ObjectOpenHashMap<>(funcIdToScope),
                new Int2ObjectOpenHashMap<>(entryMap),
                new Object2IntOpenHashMap<>(variableNameToEntry)
        );
    }

    public int getCallerInstrId() {
        return callerInstrId;
    }

    public Scope createChild(int funcId) {
        var child = funcIdToScope.get(funcId);
        if (child == null) {
            child = new Scope(this);
            funcIdToScope.put(funcId, child);
        }
        return child;
    }

    public boolean containsScope(int funcId) {
        return funcIdToScope.containsKey(funcId);
    }

    public Scope getChild(int funcId) {
        return funcIdToScope.get(funcId);
    }

    public Scope getParent() {
        return parent;
    }

    public int getIdForVariable(VariableName variableName) {
        return variableNameToEntry.getOrDefault(variableName, -1);
    }

    public void putVariable(VariableName variableName, int id) {
        variableNameToEntry.put(variableName, id);
    }

    public boolean containsVariable(VariableName variableName) {
        return variableNameToEntry.containsKey(variableName);
    }

    public void putEntry(int id, STEntry entry) {
        entryMap.put(id, entry);
    }

    public STEntry getEntry(int id) {
        return entryMap.get(id);
    }

    public boolean containsId(int id) {
        return entryMap.containsKey(id);
    }
}
