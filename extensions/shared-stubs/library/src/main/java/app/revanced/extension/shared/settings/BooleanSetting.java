package app.revanced.extension.shared.settings;

import androidx.annotation.NonNull;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String key, Boolean defaultValue) {
        super(key, defaultValue);
    }
    public BooleanSetting(String key, Boolean defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    @NonNull @Override public Boolean get() { return defaultValue; }
}
