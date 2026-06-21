package app.revanced.extension.shared;

import androidx.annotation.NonNull;

public class StringRef {
    @NonNull public static final StringRef empty = new StringRef("");

    public StringRef(@NonNull String resName) {}

    @NonNull public static StringRef sfc(@NonNull String id) { return empty; }
    @NonNull public static StringRef sf(@NonNull String id) { return empty; }
    @NonNull public static String str(@NonNull String id) { return ""; }
    @NonNull public static String str(@NonNull String id, Object... args) { return ""; }
    @NonNull public static StringRef constant(@NonNull String value) { return empty; }

    @Override @NonNull public String toString() { return ""; }
}
