package app.revanced.extension.shared.settings;

import androidx.annotation.NonNull;

public abstract class Setting<T> {
    public final String key;
    public final T defaultValue;
    public final boolean rebootApp;
    public final boolean includeWithImportExport;
    protected volatile T value;

    public Setting(String key, T defaultValue) {
        this(key, defaultValue, false);
    }
    public Setting(String key, T defaultValue, boolean rebootApp) {
        this(key, defaultValue, rebootApp, true);
    }
    public Setting(String key, T defaultValue, boolean rebootApp, boolean includeWithImportExport) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.rebootApp = rebootApp;
        this.includeWithImportExport = includeWithImportExport;
        this.value = defaultValue;
    }

    @NonNull public abstract T get();
    public final void save(@NonNull T newValue) {}
    public T resetToDefault() { return defaultValue; }
}
