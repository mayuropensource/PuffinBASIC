package org.puffinbasic.domain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.puffinbasic.domain.STObjects.STEntry;
import org.puffinbasic.domain.Variable.VariableName;

import static org.puffinbasic.domain.PuffinBasicSymbolTable.NULL_ID;

public interface Scope {

    int getCallerInstrId();
    Scope createRuntimeScope(int callerInstrId);
    Scope createChild(int funcId);
    Scope getChild(int funcId);
    Scope getParent();
    int getIdForVariable(VariableName variableName);
    void putVariable(VariableName variableName, int id);
    boolean containsVariable(VariableName variableName);
    void putEntry(int id, STEntry entry);
    STEntry getEntry(int id);
    boolean containsId(int id);

    final class GlobalScope implements Scope {
        private final int callerInstrId;
        private final Int2ObjectMap<Scope> funcIdToScope;
        // This is an optimization to make entry access fast at runtime.
        private final ObjectList<STEntry> entryMap;
        private final Object2IntMap<VariableName> variableNameToEntry;

        GlobalScope() {
            this(
                NULL_ID,
                new Int2ObjectOpenHashMap<>(),
                new ObjectArrayList<>(),
                new Object2IntOpenHashMap<>());
        }

        private GlobalScope(
            int callerInstrId,
            Int2ObjectMap<Scope> funcIdToScope,
            ObjectList<STEntry> entryMap,
            Object2IntMap<VariableName> variableNameToEntry)
        {
            this.callerInstrId = callerInstrId;
            this.funcIdToScope = funcIdToScope;
            this.entryMap = entryMap;
            this.variableNameToEntry = variableNameToEntry;
        }

        @Override
        public Scope createRuntimeScope(int callerInstrId) {
            return new GlobalScope(
                callerInstrId,
                funcIdToScope,
                entryMap,
                variableNameToEntry);
        }

        @Override
        public int getCallerInstrId() {
      return callerInstrId;
    }

        @Override
        public Scope createChild(int funcId) {
            var child = funcIdToScope.get(funcId);
            if (child == null) {
                child = new ChildScope(this);
                funcIdToScope.put(funcId, child);
            }
            return child;
        }

        @Override
        public Scope getChild(int funcId) {
      return funcIdToScope.get(funcId);
    }

        @Override
        public Scope getParent() {
      return null;
    }

        @Override
        public int getIdForVariable(VariableName variableName) {
            return variableNameToEntry.getOrDefault(variableName, -1);
        }

        @Override
        public void putVariable(VariableName variableName, int id) {
      variableNameToEntry.put(variableName, id);
    }

        @Override
        public boolean containsVariable(VariableName variableName) {
            return variableNameToEntry.containsKey(variableName);
        }

        @Override
        public void putEntry(int id, STEntry entry) {
            int sz = entryMap.size();
            if (id == sz) {
                entryMap.add(entry);
            } else if (id < sz) {
                entryMap.set(id, entry);
            } else {
                for (int i = sz; i < id; i++) {
                    entryMap.add(null);
                }
                entryMap.add(entry);
            }
        }

        @Override
        public STEntry getEntry(int id) {
            return entryMap.get(id);
        }

        @Override
        public boolean containsId(int id) {
            if (id >= 0 && id < entryMap.size()) {
                return entryMap.get(id) != null;
            }
            return false;
        }
    }

    final class ChildScope implements Scope {
        private final Scope parent;
        private final int callerInstrId;
        private final Int2ObjectMap<Scope> funcIdToScope;
        private final Int2ObjectMap<STEntry> entryMap;
        private final Object2IntMap<VariableName> variableNameToEntry;

        ChildScope(Scope parent) {
            this(parent,
                    NULL_ID,
                    new Int2ObjectOpenHashMap<>(),
                    new Int2ObjectOpenHashMap<>(),
                    new Object2IntOpenHashMap<>());
        }

        private ChildScope(
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

        @Override
        public Scope createRuntimeScope(int callerInstrId) {
            return new ChildScope(
                    parent,
                    callerInstrId,
                    new Int2ObjectOpenHashMap<>(funcIdToScope),
                    new Int2ObjectOpenHashMap<>(entryMap),
                    new Object2IntOpenHashMap<>(variableNameToEntry)
            );
        }

        @Override
        public int getCallerInstrId() {
            return callerInstrId;
        }

        @Override
        public Scope createChild(int funcId) {
            var child = funcIdToScope.get(funcId);
            if (child == null) {
                child = new ChildScope(this);
                funcIdToScope.put(funcId, child);
            }
            return child;
        }

        @Override
        public Scope getChild(int funcId) {
            return funcIdToScope.get(funcId);
        }

        @Override
        public Scope getParent() {
            return parent;
        }

        @Override
        public int getIdForVariable(VariableName variableName) {
            return variableNameToEntry.getOrDefault(variableName, -1);
        }

        @Override
        public void putVariable(VariableName variableName, int id) {
            variableNameToEntry.put(variableName, id);
        }

        @Override
        public boolean containsVariable(VariableName variableName) {
            return variableNameToEntry.containsKey(variableName);
        }

        @Override
        public void putEntry(int id, STEntry entry) {
            entryMap.put(id, entry);
        }

        @Override
        public STEntry getEntry(int id) {
            return entryMap.get(id);
        }

        @Override
        public boolean containsId(int id) {
            return entryMap.containsKey(id);
        }
    }
}
