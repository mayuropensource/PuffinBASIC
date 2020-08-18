package org.puffinbasic.runtime;

import java.util.HashMap;
import java.util.Map;

public interface Environment {
    String get(String key);
    void set(String key, String value);

    class SystemEnv implements Environment {
        private final Map<String, String> overrides;

        public SystemEnv() {
            this.overrides = new HashMap<>();
        }

        @Override
        public String get(String key) {
            String result = overrides.get(key);
            return result != null ? result : System.getenv(key);
        }

        @Override
        public void set(String key, String value) {
            overrides.put(key, value);
        }
    }
}
