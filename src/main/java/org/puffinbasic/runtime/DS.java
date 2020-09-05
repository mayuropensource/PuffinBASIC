package org.puffinbasic.runtime;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.puffinbasic.domain.STObjects.PuffinBasicDataType;
import org.puffinbasic.domain.STObjects.STValue;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicRuntimeError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.ILLEGAL_FUNCTION_PARAM;

final class DS {
    static final class DictAndTypes {
        final Map<?, ?> map;
        final PuffinBasicDataType keyType;
        final PuffinBasicDataType valueType;

        DictAndTypes(Map<?, ?> map, PuffinBasicDataType keyType, PuffinBasicDataType valueType) {
            this.map = map;
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @SuppressWarnings("unchecked")
        <K, V> Map<K, V> cast() {
            return (Map<K, V>) map;
        }

        void checkKeyType(PuffinBasicDataType actualType) {
            assertDataType(actualType, keyType, "key");
        }

        void checkValueType(PuffinBasicDataType actualType) {
            assertDataType(actualType, valueType, "value");
        }
    }

    public static final class DictState {
        private final AtomicInteger counter;
        private final Int2ObjectMap<DictAndTypes> state;

        DictState() {
            this.counter = new AtomicInteger();
            this.state = new Int2ObjectOpenHashMap<>();
        }

        int create(
                PuffinBasicDataType ketType,
                PuffinBasicDataType valueType,
                Consumer<DictAndTypes> consumer)
        {
            var id = counter.incrementAndGet();
            var mt = new DictAndTypes(new HashMap<>(), ketType, valueType);
            state.put(id, mt);
            consumer.accept(mt);
            return id;
        }

        public void get(int id, STValue key, STValue defaultValue,
                        PuffinBasicDataType keyType,
                        PuffinBasicDataType valueType,
                        STValue result) {
            var mt = getDict(id);
            mt.checkKeyType(keyType);
            mt.checkValueType(valueType);
            Map<?, Object> map = mt.cast();
            Object keyValue = getValue(key, mt.keyType);
            Object valueValue = getValue(defaultValue, mt.valueType);
            setValue(map.getOrDefault(keyValue, valueValue), result, mt.valueType);
        }

        public void put(int id, STValue key, STValue value,
                        PuffinBasicDataType keyType, PuffinBasicDataType valueType) {
            put(getDict(id), key, value, keyType, valueType);
        }

        void put(DictAndTypes mt, STValue key, STValue value,
                 PuffinBasicDataType keyType, PuffinBasicDataType valueType) {
            mt.checkKeyType(keyType);
            mt.checkValueType(valueType);
            Map<Object, Object> map = mt.cast();
            Object keyValue = getValue(key, mt.keyType);
            Object valueValue = getValue(value, mt.valueType);
            map.put(keyValue, valueValue);
        }

        public void containsKey(int id, STValue key, PuffinBasicDataType keyType, STValue result) {
            var mt = getDict(id);
            mt.checkKeyType(keyType);
            Map<Object, Object> map = mt.cast();
            Object keyValue = getValue(key, mt.keyType);
            result.setInt64(map.containsKey(keyValue) ? -1 : 0);
        }

        public void clear(int id) {
            var mt = getDict(id);
            Map<Object, Object> map = mt.cast();
            map.clear();
        }

        public void size(int id, STValue result) {
            var mt = getDict(id);
            Map<Object, Object> map = mt.cast();
            result.setInt32(map.size());
        }

        @NotNull
        private DictAndTypes getDict(int id) {
            var map = state.get(id);
            if (map == null) {
                throw new PuffinBasicRuntimeError(
                        ILLEGAL_FUNCTION_PARAM,
                        "Bad dict id: " + id
                );
            }
            return map;
        }
    }

    static final class SetAndType {
        final Set<?> set;
        final PuffinBasicDataType valueType;

        SetAndType(Set<?> set, PuffinBasicDataType valueType) {
            this.set = set;
            this.valueType = valueType;
        }

        @SuppressWarnings("unchecked")
        <V> Set<V> cast() {
            return (Set<V>) set;
        }

        void checkValueType(PuffinBasicDataType actualType) {
            assertDataType(actualType, valueType, "value");
        }
    }

    public static final class SetState {
        private final AtomicInteger counter;
        private final Int2ObjectMap<SetAndType> state;

        public SetState() {
            this.counter = new AtomicInteger();
            this.state = new Int2ObjectOpenHashMap<>();
        }

        public int create(PuffinBasicDataType valueType, Consumer<SetAndType> consumer) {
            var id = counter.incrementAndGet();
            var st = new SetAndType(new HashSet<>(), valueType);
            state.put(id, st);
            consumer.accept(st);
            return id;
        }

        public void add(int id, STValue value, PuffinBasicDataType valueType) {
            add(getSet(id), value, valueType);
        }

        public void add(SetAndType st, STValue value, PuffinBasicDataType valueType) {
            st.checkValueType(valueType);
            var set = st.cast();
            switch (valueType) {
                case INT32:
                    set.add(value.getInt32());
                    break;
                case INT64:
                    set.add(value.getInt64());
                    break;
                case FLOAT:
                    set.add(value.getFloat32());
                    break;
                case DOUBLE:
                    set.add(value.getFloat64());
                    break;
                case STRING:
                    set.add(value.getString());
                    break;
                default:
                    throwBadDataTypeError(valueType);
                    break;
            }
        }

        public void contains(int id, STValue value, PuffinBasicDataType valueType, STValue result) {
            var st = getSet(id);
            st.checkValueType(valueType);
            var set = st.cast();
            switch (valueType) {
                case INT32:
                    result.setInt64(set.contains(value.getInt32()) ? -1 : 0);
                    break;
                case INT64:
                    result.setInt64(set.contains(value.getInt64()) ? -1 : 0);
                    break;
                case FLOAT:
                    result.setInt64(set.contains(value.getFloat32()) ? -1 : 0);
                    break;
                case DOUBLE:
                    result.setInt64(set.contains(value.getFloat64()) ? -1 : 0);
                    break;
                case STRING:
                    result.setInt64(set.contains(value.getString()) ? -1 : 0);
                    break;
                default:
                    throwBadDataTypeError(valueType);
                    break;
            }
        }

        public void clear(int id) {
            var st = getSet(id);
            var set = st.cast();
            set.clear();
        }

        public void size(int id, STValue result) {
            var st = getSet(id);
            var set = st.cast();
            result.setInt32(set.size());
        }

        private SetAndType getSet(int id) {
            var set = state.get(id);
            if (set == null) {
                throw new PuffinBasicRuntimeError(
                        ILLEGAL_FUNCTION_PARAM,
                        "Bad Set id: " + id
                );
            }
            return set;
        }
    }

    private static void throwBadDataTypeError(PuffinBasicDataType valueType) {
        throw new PuffinBasicInternalError("Bad data type: " + valueType);
    }

    private static void assertDataType(PuffinBasicDataType actualType, PuffinBasicDataType expectedType, String tag) {
        if (actualType != expectedType) {
            throw new PuffinBasicRuntimeError(
                    PuffinBasicRuntimeError.ErrorCode.DATA_TYPE_MISMATCH,
                    "Type mismatch for " + tag
                            + ", given " + actualType
                            + ", expected " + expectedType
            );
        }
    }

    private static Object getValue(STValue v, PuffinBasicDataType dt) {
        switch (dt) {
            case INT32: return v.getInt32();
            case INT64: return v.getInt64();
            case FLOAT: return v.getFloat32();
            case DOUBLE: return v.getFloat64();
            case STRING: return v.getString();
            default: throwBadDataTypeError(dt);
        }
        return 0;
    }

    private static void setValue(Object v, STValue result, PuffinBasicDataType dt) {
        switch (dt) {
            case INT32:
                result.setInt32((int) v);
                break;
            case INT64:
                result.setInt64((long) v);
                break;
            case FLOAT:
                result.setFloat32((float) v);
                break;
            case DOUBLE:
                result.setFloat64((double) v);
                break;
            case STRING:
                result.setString((String) v);
                break;
            default:
                throwBadDataTypeError(dt);
        }
    }


}
