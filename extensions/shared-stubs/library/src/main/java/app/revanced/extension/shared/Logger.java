package app.revanced.extension.shared;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Logger {
    @FunctionalInterface
    public interface LogMessage {
        @NonNull String buildMessageString();
    }

    public static void printDebug(LogMessage message) {}
    public static void printDebug(LogMessage message, @Nullable Exception ex) {}
    public static void printInfo(LogMessage message) {}
    public static void printInfo(LogMessage message, @Nullable Exception ex) {}
    public static void printException(LogMessage message) {}
    public static void printException(LogMessage message, @Nullable Throwable ex) {}
}
