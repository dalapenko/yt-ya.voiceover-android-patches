package app.revanced.extension.youtube.patches;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public final class VideoInformation {
    private static WeakReference<Object> playerControllerRef = new WeakReference<>(null);

    @NonNull
    public static String getVideoId() { return ""; }
    public static long getVideoTime() { return 0L; }
    public static long getVideoLength() { return 0L; }
    public static float getPlaybackSpeed() { return 1.0f; }

    public static void setVideoId(String videoId) {}
    public static void setVideoLength(long length) {}
    public static void setPlayerResponseVideoId(String videoId, boolean isShortAndOpeningOrPlaying) {}
    public static void videoSpeedChanged(float speed) {}
    public static void userSelectedPlaybackSpeed(float speed) {}
}
