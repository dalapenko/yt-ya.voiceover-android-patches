/*
 * Copyright (C) 2026 anddea
 *
 * This file is part of the revanced-patches project:
 * https://github.com/anddea/revanced-patches
 *
 * Original author(s):
 * - Jav1x (https://github.com/Jav1x)
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

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

/**
 * Matches the method that wraps AudioTrack.setVolume(float) so we can
 * intercept volume changes and dim original audio during VOT playback.
 */
internal val BytecodePatchContext.audioTrackSetVolumeMethodMatch by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes()
    instructions(
        method {
            definingClass == "Landroid/media/AudioTrack;" &&
            name == "setVolume" &&
            parameterTypes.size == 1 &&
            parameterTypes.first() == "F" &&
            returnType == "I"
        }
    )
}
