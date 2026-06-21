package app.revanced.extension.youtube.shared;

import androidx.annotation.Nullable;

public enum VideoState {
    NEW, PLAYING, PAUSED, RECOVERABLE_ERROR, UNRECOVERABLE_ERROR, ENDED;

    @Nullable
    public static VideoState getCurrent() { return null; }

    public static void setFromString(String enumName) {}
}
