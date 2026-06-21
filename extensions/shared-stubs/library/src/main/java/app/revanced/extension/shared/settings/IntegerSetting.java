package app.revanced.extension.shared.settings;

import androidx.annotation.NonNull;

public class IntegerSetting extends Setting<Integer> {
    public IntegerSetting(String key, Integer defaultValue) {
        super(key, defaultValue);
    }
    public IntegerSetting(String key, Integer defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    @NonNull @Override public Integer get() { return defaultValue; }
}
