package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class PlayerControlButton {
    private final WeakReference<View> buttonRef = new WeakReference<>(null);

    public interface PlayerControlButtonStatus {
        boolean buttonEnabled();
    }

    public PlayerControlButton(
            View controlsViewGroup,
            String buttonId,
            @Nullable String textOverlayId,
            PlayerControlButtonStatus enabledStatus,
            View.OnClickListener onClickListener,
            @Nullable View.OnLongClickListener longClickListener) {
    }

    public void setVisibilityNegatedImmediate() {}
    public void setVisibilityImmediate(boolean visible) {}
    public void setVisibility(boolean visible, boolean animated) {}
}
