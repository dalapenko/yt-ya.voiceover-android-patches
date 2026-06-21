package app.revanced.extension.shared.settings;

import androidx.annotation.NonNull;

public class StringSetting extends Setting<String> {
    public StringSetting(String key, String defaultValue) {
        super(key, defaultValue);
    }
    public StringSetting(String key, String defaultValue, boolean rebootApp) {
        super(key, defaultValue, rebootApp);
    }
    @NonNull @Override public String get() { return defaultValue; }
}
