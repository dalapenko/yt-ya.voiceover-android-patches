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

package app.revanced.patches.youtube.video.voiceovertranslation

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.misc.playercontrols.initializeBottomControl
import app.revanced.patches.youtube.misc.playercontrols.injectVisibilityCheckCall
import app.revanced.patches.youtube.misc.playercontrols.addBottomControl
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.patches.youtube.video.information.videoTimeHook
import app.revanced.patches.youtube.video.information.videoSpeedChangedHook
import app.revanced.patches.youtube.video.information.userSelectedPlaybackSpeedHook
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_VOT_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/voiceovertranslation/VoiceOverTranslationPatch;"

private const val EXTENSION_VOT_VOLUME_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/voiceovertranslation/VotOriginalVolumePatch;"

val voiceOverTranslationBytecodePatch = bytecodePatch(
    name = "Voice Over Translation Bytecode Hooks",
    description = "Hooks YouTube video time, speed, status, and player controls overlay buttons."
) {
    dependsOn(
        videoInformationPatch,
        videoIdPatch,
        playerControlsPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.40.45"
        )
    )

    extendWith("extensions/voiceovertranslation.rve")

    apply {
        // 1. Hook video time updates — called once per second during playback
        videoTimeHook(
            EXTENSION_VOT_CLASS_DESCRIPTOR,
            "setVideoTime"
        )

        // 2. Hook playback speed changes (automatic and user-selected)
        videoSpeedChangedHook(
            EXTENSION_VOT_CLASS_DESCRIPTOR,
            "onPlaybackSpeedChanged"
        )
        userSelectedPlaybackSpeedHook(
            EXTENSION_VOT_CLASS_DESCRIPTOR,
            "onPlaybackSpeedChanged"
        )

        // 3. Hook new video started event via player response
        hookPlayerResponseVideoId(
            "$EXTENSION_VOT_CLASS_DESCRIPTOR->newVideoStarted(Ljava/lang/String;Z)V"
        )

        // 4. Hook controls state/player overlays to inject overlay buttons
        val buttonDescriptor = "Lapp/revanced/extension/youtube/patches/voiceovertranslation/VoiceOverTranslationButton;"
        initializeBottomControl(buttonDescriptor)
        injectVisibilityCheckCall(buttonDescriptor)
    }
}

val votOriginalVolumeBytecodePatch = bytecodePatch(
    name = "VOT Original Audio Volume Hook",
    description = "Hooks AudioTrack.setVolume to dim original audio track when voice-over translation is playing."
) {
    dependsOn(voiceOverTranslationBytecodePatch)

    compatibleWith(
        "com.google.android.youtube"(
            "20.40.45"
        )
    )

    apply {
        val mutableMethod = audioTrackSetVolumeMethodMatch
        val index = mutableMethod.indexOfFirstInstructionOrThrow {
            (opcode == Opcode.INVOKE_VIRTUAL || opcode == Opcode.INVOKE_VIRTUAL_RANGE) &&
                (getReference<MethodReference>()?.let { ref ->
                    ref.definingClass == "Landroid/media/AudioTrack;" &&
                    ref.name == "setVolume" &&
                    ref.parameterTypes.size == 1 &&
                    ref.parameterTypes.first() == "F" &&
                    ref.returnType == "I"
                } == true)
        }

        val instruction = mutableMethod.implementation!!.instructions.elementAt(index)

        val audioTrackReg = getAudioTrackRegister(instruction)
            ?: throw PatchException("VotOriginalVolume: cannot get AudioTrack register")
        val volReg = getVolumeRegister(instruction)
            ?: throw PatchException("VotOriginalVolume: cannot get volume register")

        mutableMethod.addInstructions(
            index,
            """
            invoke-static { v$audioTrackReg, v$volReg }, $EXTENSION_VOT_VOLUME_CLASS_DESCRIPTOR->applyVolumeMultiplier(Landroid/media/AudioTrack;F)F
            move-result v$volReg
            """.trimIndent()
        )
    }
}

@Suppress("unused")
val voiceOverTranslationPatch = resourcePatch(
    name = "Yandex Voice Over Translation",
    description = "Adds Yandex Voice Over Translation to YouTube."
) {
    dependsOn(
        settingsPatch,
        voiceOverTranslationBytecodePatch,
        votOriginalVolumeBytecodePatch,
        playerControlsPatch,
        addResourcesPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.40.45"
        )
    )

    apply {
        // Register English Strings (Default)
        val enStrings = mapOf(
            "revanced_vot_settings_title" to "Voice Over Translation",
            "revanced_vot_settings_summary" to "Configure settings for Yandex voice-over translation",
            "revanced_language_EN" to "English",
            "revanced_language_RU" to "Russian",
            "revanced_language_DE" to "German",
            "revanced_language_FR" to "French",
            "revanced_language_ES" to "Spanish",
            "revanced_language_IT" to "Italian",
            "revanced_language_JA" to "Japanese",
            "revanced_language_KO" to "Korean",
            "revanced_language_ZH" to "Chinese",
            "revanced_language_KK" to "Kazakh",
            
            "revanced_vot_enabled_title" to "Enable Voice Over Translation",
            "revanced_vot_enabled_summary_on" to "Yandex voice-over translation is enabled",
            "revanced_vot_enabled_summary_off" to "Yandex voice-over translation is disabled",
            
            "revanced_vot_source_language_title" to "Source language",
            "revanced_vot_source_language_summary" to "Select the language of the video audio",
            "revanced_vot_lang_auto" to "Auto-detect",
            
            "revanced_vot_target_language_title" to "Target language",
            "revanced_vot_target_language_summary" to "Select the language to translate to",
            
            "revanced_vot_translation_volume_title" to "Translation volume",
            "revanced_vot_translation_volume_summary" to "Volume of the translated audio track (0-100). Default: 100",
            
            "revanced_vot_original_audio_volume_title" to "Original audio volume",
            "revanced_vot_original_audio_volume_summary" to "Volume of the original video track when translation is playing (0-100). Default: 50",
            
            "revanced_vot_proxy_url_title" to "Proxy server",
            "revanced_vot_proxy_url_summary" to "VOT worker proxy host. Default: vot-new.toil-dump.workers.dev",
            
            "revanced_vot_voice_style_title" to "Voice-over style",
            "revanced_vot_voice_style_standard" to "Standard voices",
            "revanced_vot_voice_style_live" to "Live voices",
            "revanced_vot_voice_style_live_title" to "Live voices",
            "revanced_vot_voice_style_live_summary_on" to "Natural-sounding voices from Yandex",
            "revanced_vot_voice_style_live_summary_off" to "Standard TTS voices",
            
            "revanced_vot_audio_proxy_title" to "Proxy audio stream",
            "revanced_vot_audio_proxy_summary_on" to "Audio stream is proxied through the worker",
            "revanced_vot_audio_proxy_summary_off" to "Audio stream uses direct URL",
            
            "revanced_vot_started" to "Translation started",
            "revanced_vot_stopped" to "Translation stopped",
            "revanced_vot_playback_error" to "Failed to play translation audio",
            "revanced_vot_live_voices_unavailable" to "Live voices unavailable, using standard",
            "revanced_vot_unavailable_live" to "Translation is not available for live streams",
            "revanced_vot_unavailable_too_long" to "Video is too long for translation (max 4 hours)",
            "revanced_vot_unavailable_same_language" to "Video audio is already in the target language",
            "revanced_vot_stream_waiting" to "Waiting… ~%s",
            "revanced_vot_stream_not_ready" to "Translation not ready. Use the button to enable when ready.",
            "revanced_vot_time_sec" to "%d sec",
            "revanced_vot_time_min" to "%d min",
            "revanced_vot_percent_value" to "%d%%"
        )
        enStrings.forEach { (name, value) ->
            addResources(name, value, formatted = true, resourceValue = "values")
        }

        // Register Russian Strings
        val ruStrings = mapOf(
            "revanced_vot_settings_title" to "Закадровый перевод",
            "revanced_vot_settings_summary" to "Настройки закадрового перевода Яндекса",
            "revanced_language_EN" to "Английский",
            "revanced_language_RU" to "Русский",
            "revanced_language_DE" to "Немецкий",
            "revanced_language_FR" to "Французский",
            "revanced_language_ES" to "Испанский",
            "revanced_language_IT" to "Итальянский",
            "revanced_language_JA" to "Японский",
            "revanced_language_KO" to "Корейский",
            "revanced_language_ZH" to "Китайский",
            "revanced_language_KK" to "Казахский",
            
            "revanced_vot_enabled_title" to "Включить закадровый перевод",
            "revanced_vot_enabled_summary_on" to "Закадровый перевод Яндекс включён",
            "revanced_vot_enabled_summary_off" to "Закадровый перевод Яндекс выключен",
            
            "revanced_vot_source_language_title" to "Язык оригинала",
            "revanced_vot_source_language_summary" to "Выберите язык аудио видео",
            "revanced_vot_lang_auto" to "Автоопределение",
            
            "revanced_vot_target_language_title" to "Язык перевода",
            "revanced_vot_target_language_summary" to "Выберите язык перевода",
            
            "revanced_vot_translation_volume_title" to "Громкость перевода",
            "revanced_vot_translation_volume_summary" to "Громкость переведённой аудиодорожки (0-100). По умолчанию: 100",
            
            "revanced_vot_original_audio_volume_title" to "Громкость оригинала",
            "revanced_vot_original_audio_volume_summary" to "Громкость оригинальной дорожки при воспроизведении перевода (0-100). По умолчанию: 50",
            
            "revanced_vot_proxy_url_title" to "Прокси-сервер",
            "revanced_vot_proxy_url_summary" to "Хост прокси-сервера VOT worker. По умолчанию: vot-new.toil-dump.workers.dev",
            
            "revanced_vot_voice_style_title" to "Вид озвучки",
            "revanced_vot_voice_style_standard" to "Обычные голоса",
            "revanced_vot_voice_style_live" to "Живые голоса",
            "revanced_vot_voice_style_live_title" to "Живые голоса",
            "revanced_vot_voice_style_live_summary_on" to "Естественная озвучка от Яндекса",
            "revanced_vot_voice_style_live_summary_off" to "Стандартная TTS озвучка",
            
            "revanced_vot_audio_proxy_title" to "Проксировать аудио",
            "revanced_vot_audio_proxy_summary_on" to "Аудио поток идёт через прокси worker",
            "revanced_vot_audio_proxy_summary_off" to "Аудио поток загружается напрямую",
            
            "revanced_vot_started" to "Перевод запущен",
            "revanced_vot_stopped" to "Перевод остановлен",
            "revanced_vot_playback_error" to "Не удалось воспроизвести аудио перевода",
            "revanced_vot_live_voices_unavailable" to "Живые голоса недоступны, используем обычные",
            "revanced_vot_unavailable_live" to "Перевод недоступен для стримов",
            "revanced_vot_unavailable_too_long" to "Видео слишком длинное для перевода (макс. 4 часа)",
            "revanced_vot_unavailable_same_language" to "Аудио видео уже на выбранном языке",
            "revanced_vot_stream_waiting" to "Ожидание… ~%s",
            "revanced_vot_stream_not_ready" to "Перевод ещё не готов. Включите кнопкой, когда будет готов.",
            "revanced_vot_time_sec" to "%d сек",
            "revanced_vot_time_min" to "%d мин",
            "revanced_vot_percent_value" to "%d%%"
        )
        ruStrings.forEach { (name, value) ->
            addResources(name, value, formatted = true, resourceValue = "values-ru")
        }

        // Register String Arrays (under values)
        addResources(
            "revanced_vot_source_language_entries",
            listOf(
                "@string/revanced_vot_lang_auto",
                "@string/revanced_language_EN",
                "@string/revanced_language_RU",
                "@string/revanced_language_DE",
                "@string/revanced_language_FR",
                "@string/revanced_language_ES",
                "@string/revanced_language_IT",
                "@string/revanced_language_JA",
                "@string/revanced_language_KO",
                "@string/revanced_language_ZH"
            )
        )
        addResources(
            "revanced_vot_source_language_entry_values",
            listOf("auto", "en", "ru", "de", "fr", "es", "it", "ja", "ko", "zh")
        )
        addResources(
            "revanced_vot_target_language_entries",
            listOf(
                "@string/revanced_language_RU",
                "@string/revanced_language_EN",
                "@string/revanced_language_KK"
            )
        )
        addResources(
            "revanced_vot_target_language_entry_values",
            listOf("ru", "en", "kk")
        )

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                "revanced_vot_settings",
                preferences = setOf(
                    SwitchPreference("revanced_vot_enabled"),
                    ListPreference(
                        key = "revanced_vot_source_language",
                        entriesKey = "revanced_vot_source_language_entries",
                        entryValuesKey = "revanced_vot_source_language_entry_values"
                    ),
                    ListPreference(
                        key = "revanced_vot_target_language",
                        entriesKey = "revanced_vot_target_language_entries",
                        entryValuesKey = "revanced_vot_target_language_entry_values"
                    ),
                    TextPreference(
                        key = "revanced_vot_translation_volume",
                        inputType = InputType.NUMBER
                    ),
                    TextPreference(
                        key = "revanced_vot_original_volume",
                        titleKey = "revanced_vot_original_audio_volume_title",
                        summaryKey = "revanced_vot_original_audio_volume_summary",
                        inputType = InputType.NUMBER
                    ),
                    TextPreference(
                        key = "revanced_vot_proxy_url",
                        inputType = InputType.TEXT
                    ),
                    SwitchPreference(
                        key = "revanced_vot_use_live_voices",
                        titleKey = "revanced_vot_voice_style_live_title",
                        summaryOnKey = "revanced_vot_voice_style_live_summary_on",
                        summaryOffKey = "revanced_vot_voice_style_live_summary_off"
                    ),
                    SwitchPreference(
                        key = "revanced_vot_audio_proxy_enabled",
                        titleKey = "revanced_vot_audio_proxy_title",
                        summaryOnKey = "revanced_vot_audio_proxy_summary_on",
                        summaryOffKey = "revanced_vot_audio_proxy_summary_off"
                    )
                ),
                sorting = PreferenceScreenPreference.Sorting.UNSORTED
            )
        )

        copyResources(
            "voiceovertranslation",
            ResourceGroup(
                resourceDirectoryName = "drawable",
                "revanced_vot_button.xml",
                "revanced_vot_button_icon.xml",
                "revanced_vot_button_activated_icon.xml",
            ),
        )

        addBottomControl("voiceovertranslation")
    }
}

private fun getVolumeRegister(instruction: Instruction): Int? {
    return when (instruction) {
        is FiveRegisterInstruction -> {
            if (instruction.registerCount >= 2) instruction.registerD
            else null
        }
        is TwoRegisterInstruction -> instruction.registerB
        is RegisterRangeInstruction -> {
            if (instruction.registerCount >= 2)
                instruction.startRegister + instruction.registerCount - 1
            else null
        }
        else -> null
    }
}

private fun getAudioTrackRegister(instruction: Instruction): Int? {
    return when (instruction) {
        is FiveRegisterInstruction -> if (instruction.registerCount >= 1) instruction.registerC else null
        is TwoRegisterInstruction -> instruction.registerA
        is RegisterRangeInstruction -> if (instruction.registerCount >= 1) instruction.startRegister else null
        else -> null
    }
}
