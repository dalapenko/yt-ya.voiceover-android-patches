package app.revanced.extension.shared.ui;

public final class Dim {
    public static final int dp1 = 1, dp2 = 2, dp4 = 4, dp6 = 6, dp7 = 7, dp8 = 8;
    public static final int dp10 = 10, dp12 = 12, dp16 = 16, dp20 = 20;
    public static final int dp24 = 24, dp28 = 28, dp32 = 32, dp36 = 36, dp40 = 40, dp48 = 48;
    public static final int SCREEN_WIDTH = 1080;
    public static final int SCREEN_HEIGHT = 1920;

    public static int dp(float dp) { return (int) dp; }
    public static int pctHeight(int percent) { return 0; }
    public static int pctWidth(int percent) { return 0; }
    public static int pctPortraitWidth(int percent) { return 0; }
    public static float[] roundedCorners(float dp) { return new float[]{dp,dp,dp,dp,dp,dp,dp,dp}; }
}
