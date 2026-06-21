package app.revanced.extension.shared;

import android.content.Context;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class Utils {
    public static Context getContext() { return null; }
    public static void showToastShort(String messageToToast) {}
    public static void runOnMainThread(Runnable runnable) {}
    public static void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {}
    public static void runOnMainThreadNowOrLater(Runnable runnable) {}
    public static void runOnBackgroundThread(Runnable task) {}
    public static boolean isDarkModeEnabled() { return false; }
    @ColorInt public static int getAppForegroundColor() { return 0; }
    @ColorInt public static int getDialogBackgroundColor() { return 0; }
    @ColorInt public static int adjustColorBrightness(@ColorInt int color, float factor) { return 0; }
    @ColorInt public static int adjustColorBrightness(@ColorInt int color, float lightThemeFactor, float darkThemeFactor) { return 0; }
    public static void setDialogWindowParameters(Window window, int gravity, int yOffsetDip, int widthPercentage, boolean dimAmount) {}
    @SuppressWarnings("unchecked")
    public static <R extends View> R getChildViewByResourceName(View view, String str) { return null; }
    public static int getResourceInteger(String resourceIdentifierName) { return 0; }
    public static int getResourceIdentifierOrThrow(@Nullable Object type, String resourceIdentifierName) { return 0; }
    @ColorInt public static int getResourceColor(String resourceIdentifierName) { return 0; }
    public static void verifyOnMainThread() {}
}
