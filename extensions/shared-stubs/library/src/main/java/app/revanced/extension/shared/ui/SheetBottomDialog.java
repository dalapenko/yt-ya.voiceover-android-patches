package app.revanced.extension.shared.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SheetBottomDialog {

    public static SlideDialog createSlideDialog(@NonNull Context context, @NonNull View contentView, int animationDuration) {
        return new SlideDialog(context);
    }

    public static DraggableLinearLayout createMainLayout(@NonNull Context context, @Nullable Integer backgroundColor) {
        return new DraggableLinearLayout(context);
    }

    public static class DraggableLinearLayout extends LinearLayout {
        public DraggableLinearLayout(@NonNull Context context) {
            super(context);
        }
        public DraggableLinearLayout(@NonNull Context context, int animDuration) {
            super(context);
        }
        public void setDialog(@NonNull SlideDialog dialog) {}
    }

    public static class SlideDialog extends Dialog {
        public SlideDialog(@NonNull Context context) {
            super(context);
        }
        public void setAnimView(@NonNull View view) {}
        public void setAnimationDuration(int duration) {}
        @Override public void show() {}
        @Override public void cancel() {}
        @Override public void dismiss() {}
    }
}
