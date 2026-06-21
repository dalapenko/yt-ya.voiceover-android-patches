/*
 * Copyright (C) 2026 anddea
 *
 * This file is part of the revanced-patches project:
 * https://github.com/anddea/revanced-patches
 *
 * Original author(s) (based on contributions):
 * - Jav1x (https://github.com/Jav1x)
 * - anddea (https://github.com/anddea)
 *
 * Licensed under the GNU General Public License v3.0.
 *
 * ------------------------------------------------------------------------
 * GPLv3 Section 7 – Attribution Notice
 * ------------------------------------------------------------------------
 *
 * This file contains substantial original work by the author(s) listed above.
 *
 * In accordance with Section 7 of the GNU General Public License v3.0,
 * the following additional terms apply to this file:
 *
 * 1. Attribution (Section 7(b)): This specific copyright notice and the
 *    list of original authors above must be preserved in any copy or
 *    derivative work. You may add your own copyright notice below it,
 *    but you may not remove the original one.
 *
 * 2. Origin (Section 7(c)): Modified versions must be clearly marked as
 *    such (e.g., by adding a "Modified by" line or a new copyright notice).
 *    They must not be misrepresented as the original work.
 *
 * ------------------------------------------------------------------------
 * Version Control Acknowledgement (Non-binding Request)
 * ------------------------------------------------------------------------
 *
 * While not a legal requirement of the GPLv3, the original author(s)
 * respectfully request that ports or substantial modifications retain
 * historical authorship credit in version control systems (e.g., Git),
 * listing original author(s) appropriately and modifiers as committers
 * or co-authors.
 */

package app.revanced.extension.youtube.patches.voiceovertranslation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.ui.Dim;
import app.revanced.extension.shared.ui.SheetBottomDialog;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.videoplayer.PlayerControlButton;

import static app.revanced.extension.shared.StringRef.str;

@SuppressWarnings({"unused"})
public class VoiceOverTranslationButton {
    private static PlayerControlButton instance = null;

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            VoiceOverTranslationPatch.setOnTranslationStateChangeCallback(
                    VoiceOverTranslationButton::refreshActivatedState
            );
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_vot_button",
                    null, // no text overlay
                    VoiceOverTranslationButton::isButtonEnabled,
                    VoiceOverTranslationButton::onClick,
                    VoiceOverTranslationButton::onLongClick
            );
        } catch (Exception ex) {
            Logger.printException(() -> "VoiceOverTranslationButton initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibilityNegatedImmediate() {
        if (instance != null) {
            instance.setVisibilityNegatedImmediate();
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            refreshActivatedState();
            instance.setVisibilityImmediate(visible);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            refreshActivatedState();
            instance.setVisibility(visible, animated);
        }
    }

    private static boolean isButtonEnabled() {
        return Settings.VOT_ENABLED.get()
                && !SegmentPlaybackController.isAdProgressTextVisible();
    }

    private static void onClick(View view) {
        VoiceOverTranslationPatch.toggleTranslation();
        refreshActivatedState();
    }

    private static boolean onLongClick(View view) {
        Context context = Utils.getContext();
        if (context == null) return false;
        showVotBottomSheetDialog(context);
        return true;
    }

    private static void refreshActivatedState() {
        if (instance == null) return;
        try {
            java.lang.reflect.Field f = PlayerControlButton.class.getDeclaredField("buttonRef");
            f.setAccessible(true);
            WeakReference<?> ref = (WeakReference<?>) f.get(instance);
            View btnView = ref != null ? (View) ref.get() : null;
            if (btnView != null) {
                btnView.setActivated(VoiceOverTranslationPatch.isTranslationActive());
            }
        } catch (Exception e) {
            Logger.printException(() -> "refreshActivatedState reflection error", e);
        }
    }

    // ==========================================
    // VOT Settings Bottom Sheet Construction
    // ==========================================

    private static void showVotBottomSheetDialog(@NonNull Context context) {
        try {
            SheetBottomDialog.DraggableLinearLayout mainLayout =
                    SheetBottomDialog.createMainLayout(context, null);

            final int dip8 = Dim.dp8;
            final int dip12 = Dim.dp12;
            final int dip20 = Dim.dp20;

            // Title: Translation volume
            TextView titleText = new TextView(context);
            titleText.setText(str("revanced_vot_translation_volume_title"));
            titleText.setTextColor(Utils.getAppForegroundColor());
            titleText.setTextSize(16);
            titleText.setTypeface(Typeface.DEFAULT_BOLD);
            titleText.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(0, dip20, 0, dip12);
            titleText.setLayoutParams(titleParams);
            mainLayout.addView(titleText);

            // Slider row for translation volume
            LinearLayout sliderLayout = new LinearLayout(context);
            sliderLayout.setOrientation(LinearLayout.HORIZONTAL);
            sliderLayout.setGravity(Gravity.CENTER_VERTICAL);

            Button minusButton = createStyledButton(context, false, dip8, dip8);
            Button plusButton = createStyledButton(context, true, dip8, dip8);

            SeekBar volumeSlider = new SeekBar(context);
            volumeSlider.setFocusable(true);
            volumeSlider.setFocusableInTouchMode(true);
            volumeSlider.setMax(100);
            volumeSlider.setProgress(Settings.VOT_TRANSLATION_VOLUME.get());
            volumeSlider.getProgressDrawable().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN);
            volumeSlider.getThumb().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams sliderParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            volumeSlider.setLayoutParams(sliderParams);

            TextView volumeValueText = new TextView(context);
            volumeValueText.setText(str("revanced_vot_percent_value", Settings.VOT_TRANSLATION_VOLUME.get()));
            volumeValueText.setTextColor(Utils.getAppForegroundColor());
            volumeValueText.setTextSize(14);
            volumeValueText.setMinWidth(Dim.dp(40));

            sliderLayout.addView(minusButton);
            sliderLayout.addView(volumeSlider);
            sliderLayout.addView(plusButton);
            sliderLayout.addView(volumeValueText);

            mainLayout.addView(sliderLayout);

            java.util.function.Consumer<Integer> applyVolume = vol -> {
                int clamped = Math.min(Math.max(vol, 0), 100);
                Settings.VOT_TRANSLATION_VOLUME.save(clamped);
                volumeSlider.setProgress(clamped);
                volumeValueText.setText(str("revanced_vot_percent_value", clamped));
                VoiceOverTranslationPatch.applyVolumeToCurrentPlayer();
            };

            volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) applyVolume.accept(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });

            minusButton.setOnClickListener(v -> applyVolume.accept(Settings.VOT_TRANSLATION_VOLUME.get() - 5));
            plusButton.setOnClickListener(v -> applyVolume.accept(Settings.VOT_TRANSLATION_VOLUME.get() + 5));

            // Original audio volume title
            TextView origTitleText = new TextView(context);
            origTitleText.setText(str("revanced_vot_original_audio_volume_title"));
            origTitleText.setTextColor(Utils.getAppForegroundColor());
            origTitleText.setTextSize(16);
            origTitleText.setTypeface(Typeface.DEFAULT_BOLD);
            origTitleText.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams origTitleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            origTitleParams.setMargins(0, dip20, 0, dip12);
            origTitleText.setLayoutParams(origTitleParams);
            mainLayout.addView(origTitleText);

            // Slider row for original audio volume
            LinearLayout origSliderLayout = new LinearLayout(context);
            origSliderLayout.setOrientation(LinearLayout.HORIZONTAL);
            origSliderLayout.setGravity(Gravity.CENTER_VERTICAL);

            Button origMinusButton = createStyledButton(context, false, dip8, dip8);
            Button origPlusButton = createStyledButton(context, true, dip8, dip8);

            SeekBar origVolumeSlider = new SeekBar(context);
            origVolumeSlider.setFocusable(true);
            origVolumeSlider.setFocusableInTouchMode(true);
            origVolumeSlider.setMax(100);
            origVolumeSlider.setProgress(Settings.VOT_ORIGINAL_AUDIO_VOLUME.get());
            origVolumeSlider.getProgressDrawable().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN);
            origVolumeSlider.getThumb().setColorFilter(
                    Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams origSliderParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            origVolumeSlider.setLayoutParams(origSliderParams);

            TextView origVolumeValueText = new TextView(context);
            origVolumeValueText.setText(str("revanced_vot_percent_value", Settings.VOT_ORIGINAL_AUDIO_VOLUME.get()));
            origVolumeValueText.setTextColor(Utils.getAppForegroundColor());
            origVolumeValueText.setTextSize(14);
            origVolumeValueText.setMinWidth(Dim.dp(40));

            origSliderLayout.addView(origMinusButton);
            origSliderLayout.addView(origVolumeSlider);
            origSliderLayout.addView(origPlusButton);
            origSliderLayout.addView(origVolumeValueText);

            mainLayout.addView(origSliderLayout);

            java.util.function.Consumer<Integer> applyOrigVolume = vol -> {
                int clamped = Math.min(Math.max(vol, 0), 100);
                Settings.VOT_ORIGINAL_AUDIO_VOLUME.save(clamped);
                origVolumeSlider.setProgress(clamped);
                origVolumeValueText.setText(str("revanced_vot_percent_value", clamped));
                if (VoiceOverTranslationPatch.isTranslationActive()) {
                    VoiceOverTranslationPatch.refreshOriginalAudioVolumeIfActive();
                }
            };

            origVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) applyOrigVolume.accept(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });

            origMinusButton.setOnClickListener(v -> applyOrigVolume.accept(Settings.VOT_ORIGINAL_AUDIO_VOLUME.get() - 5));
            origPlusButton.setOnClickListener(v -> applyOrigVolume.accept(Settings.VOT_ORIGINAL_AUDIO_VOLUME.get() + 5));

            // Segmented buttons for voice style (Standard | Live)
            LinearLayout voiceStyleRow = createVotVoiceStyleButtons(context, VoiceOverTranslationPatch::restartTranslationIfActive);
            mainLayout.addView(voiceStyleRow);

            // Audio proxy toggle
            LinearLayout proxyItem = createVotSwitchItem(context, str("revanced_vot_audio_proxy_title"),
                    Settings.VOT_AUDIO_PROXY_ENABLED, VoiceOverTranslationPatch::restartTranslationIfActive);
            mainLayout.addView(proxyItem);

            SheetBottomDialog.createSlideDialog(context, mainLayout, 200).show();
        } catch (Exception ex) {
            Logger.printException(() -> "showVotBottomSheetDialog failure", ex);
        }
    }

    private static LinearLayout createVotVoiceStyleButtons(Context context, Runnable onChanged) {
        final int dip8 = Dim.dp8;
        final int dip12 = Dim.dp12;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(Dim.dp(16), dip12, Dim.dp(16), dip12);

        TextView labelText = new TextView(context);
        labelText.setText(str("revanced_vot_voice_style_title"));
        labelText.setTextColor(Utils.getAppForegroundColor());
        labelText.setTextSize(14);
        labelText.setTypeface(Typeface.DEFAULT_BOLD);
        labelText.setGravity(Gravity.START);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, 0, 0, dip8);
        labelText.setLayoutParams(labelParams);
        container.addView(labelText);

        LinearLayout buttonsRow = new LinearLayout(context);
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonsRow.setGravity(Gravity.CENTER);

        Button standardButton = createVotSegmentedButton(context, str("revanced_vot_voice_style_standard"));
        Button liveButton = createVotSegmentedButton(context, str("revanced_vot_voice_style_live"));

        Runnable updateSelection = () -> {
            boolean useLive = Settings.VOT_USE_LIVE_VOICES.get();
            int selectedColor = getAdjustedBackgroundColor(true);
            int unselectedColor = getAdjustedBackgroundColor(false);
            ShapeDrawable standardBg = (ShapeDrawable) standardButton.getBackground();
            ShapeDrawable liveBg = (ShapeDrawable) liveButton.getBackground();
            standardBg.getPaint().setColor(useLive ? unselectedColor : selectedColor);
            liveBg.getPaint().setColor(useLive ? selectedColor : unselectedColor);
            standardBg.invalidateSelf();
            liveBg.invalidateSelf();
            standardButton.invalidate();
            liveButton.invalidate();
        };

        standardButton.setOnClickListener(v -> {
            Settings.VOT_USE_LIVE_VOICES.save(false);
            updateSelection.run();
            if (onChanged != null) onChanged.run();
        });
        liveButton.setOnClickListener(v -> {
            Settings.VOT_USE_LIVE_VOICES.save(true);
            updateSelection.run();
            if (onChanged != null) onChanged.run();
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, Dim.dp(40), 1f);
        btnParams.setMargins(0, 0, dip8 / 2, 0);
        standardButton.setLayoutParams(btnParams);
        LinearLayout.LayoutParams btnParams2 = new LinearLayout.LayoutParams(0, Dim.dp(40), 1f);
        btnParams2.setMargins(dip8 / 2, 0, 0, 0);
        liveButton.setLayoutParams(btnParams2);

        buttonsRow.addView(standardButton);
        buttonsRow.addView(liveButton);
        container.addView(buttonsRow);

        updateSelection.run();
        return container;
    }

    private static Button createVotSegmentedButton(Context context, String text) {
        Button button = new Button(context, null, 0);
        button.setText(text);
        button.setTextColor(Utils.getAppForegroundColor());
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Dim.roundedCorners(12), null, null));
        background.getPaint().setColor(getAdjustedBackgroundColor(false));
        button.setBackground(background);
        button.setPadding(Dim.dp(12), Dim.dp(8), Dim.dp(12), Dim.dp(8));
        return button;
    }

    private static LinearLayout createVotSwitchItem(Context context, String title,
                                                    BooleanSetting setting, Runnable onChanged) {
        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(Dim.dp(16), Dim.dp(12), Dim.dp(16), Dim.dp(12));
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setClickable(true);
        itemLayout.setFocusable(true);

        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(getAdjustedBackgroundColor(true)));
        background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        itemLayout.setBackground(background);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTextColor(Utils.getAppForegroundColor());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        itemLayout.addView(titleView, titleParams);

        Switch switchView = new Switch(context);
        switchView.setChecked(setting.get());
        switchView.getThumbDrawable().setColorFilter(Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_ATOP);
        switchView.getTrackDrawable().setColorFilter(Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_ATOP);
        switchView.setOnCheckedChangeListener((v, isChecked) -> {
            setting.save(isChecked);
            if (onChanged != null) onChanged.run();
        });
        itemLayout.addView(switchView);

        itemLayout.setOnClickListener(v -> switchView.setChecked(!setting.get()));

        return itemLayout;
    }

    private static Button createStyledButton(Context context, boolean isPlus, int marginStart, int marginEnd) {
        Button button = new Button(context, null, 0);
        button.setText("");
        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(
                Dim.roundedCorners(20), null, null));
        background.getPaint().setColor(getAdjustedBackgroundColor(false));
        button.setBackground(background);
        button.setForeground(new OutlineSymbolDrawable(isPlus));
        final int dip36 = Dim.dp(36);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dip36, dip36);
        params.setMargins(marginStart, 0, marginEnd, 0);
        button.setLayoutParams(params);
        return button;
    }

    public static int getAdjustedBackgroundColor(boolean isHandleBar) {
        int baseColor = Utils.getDialogBackgroundColor();
        float factor = Utils.isDarkModeEnabled()
                ? (isHandleBar ? 1.25f : 1.115f)
                : (isHandleBar ? 0.9f : 0.95f);
        return adjustColorBrightness(baseColor, factor);
    }

    private static int adjustColorBrightness(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (factor > 1.0f) {
            float t = 1.0f - (1.0f / factor);
            red = Math.round(red + (255 - red) * t);
            green = Math.round(green + (255 - green) * t);
            blue = Math.round(blue + (255 - blue) * t);
        } else {
            red = Math.round(red * factor);
            green = Math.round(green * factor);
            blue = Math.round(blue * factor);
        }

        red = Math.min(Math.max(red, 0), 255);
        green = Math.min(Math.max(green, 0), 255);
        blue = Math.min(Math.max(blue, 0), 255);

        return Color.argb(alpha, red, green, blue);
    }
}

class OutlineSymbolDrawable extends Drawable {
    private final boolean isPlus;
    private final Paint paint;

    OutlineSymbolDrawable(boolean isPlus) {
        this.isPlus = isPlus;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Utils.getAppForegroundColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Dim.dp(1));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        final int width = bounds.width();
        final int height = bounds.height();
        final float centerX = width / 2f;
        final float centerY = height / 2f;
        final float size = Math.min(width, height) * 0.25f;

        canvas.drawLine(centerX - size, centerY, centerX + size, centerY, paint);
        if (isPlus) {
            canvas.drawLine(centerX, centerY - size, centerX, centerY + size, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
