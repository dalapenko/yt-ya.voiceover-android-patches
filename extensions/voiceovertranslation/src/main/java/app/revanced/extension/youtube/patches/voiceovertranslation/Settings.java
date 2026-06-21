package app.revanced.extension.youtube.patches.voiceovertranslation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.shared.settings.StringSetting;

public class Settings {
    public static final BooleanSetting VOT_ENABLED = new BooleanSetting("revanced_vot_enabled", TRUE);
    public static final StringSetting VOT_SOURCE_LANGUAGE = new StringSetting("revanced_vot_source_language", "auto");
    public static final StringSetting VOT_TARGET_LANGUAGE = new StringSetting("revanced_vot_target_language", "ru");
    public static final BooleanSetting VOT_USE_LIVE_VOICES = new BooleanSetting("revanced_vot_use_live_voices", TRUE);
    public static final BooleanSetting VOT_AUDIO_PROXY_ENABLED = new BooleanSetting("revanced_vot_audio_proxy_enabled", FALSE);
    public static final IntegerSetting VOT_TRANSLATION_VOLUME = new IntegerSetting("revanced_vot_translation_volume", 100);
    public static final IntegerSetting VOT_ORIGINAL_AUDIO_VOLUME = new IntegerSetting("revanced_vot_original_volume", 20);
    public static final StringSetting VOT_PROXY_URL = new StringSetting("revanced_vot_proxy_url", "vot-new.toil-dump.workers.dev");
}
