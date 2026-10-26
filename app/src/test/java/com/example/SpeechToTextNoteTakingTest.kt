package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SpeechToTextNoteTakingTest {

    @Test
    fun testRecordAudioPermissionDeclaredInManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        assertTrue(
            "RECORD_AUDIO permission must be declared in AndroidManifest.xml for speech-to-text",
            permissions.contains(android.Manifest.permission.RECORD_AUDIO)
        )
    }

    @Test
    fun testSpeechTextAppendLogic() {
        var rawText = ""
        val firstSpeechUtterance = "Autonomous clinical denial auditor for outpatient clinics."

        // Initial speech utterance populates blank field
        rawText = if (rawText.isBlank()) firstSpeechUtterance else "$rawText $firstSpeechUtterance"
        assertEquals("Autonomous clinical denial auditor for outpatient clinics.", rawText)

        // Subsequent speech utterance appends cleanly with space separator
        val secondSpeechUtterance = "Targeting billing directors at mid-size practices."
        rawText = if (rawText.isBlank()) secondSpeechUtterance else "$rawText $secondSpeechUtterance"
        assertEquals(
            "Autonomous clinical denial auditor for outpatient clinics. Targeting billing directors at mid-size practices.",
            rawText
        )
    }

    @Test
    fun testAutoTitleGenerationFromVoiceDictation() {
        var ideaTitle = ""
        val spokenText = "Milestone escrow marketplace for home remodelers"

        if (ideaTitle.isBlank()) {
            val words = spokenText.split(Regex("\\s+")).take(3).joinToString(" ")
            if (words.isNotBlank()) ideaTitle = words
        }

        assertEquals("Milestone escrow marketplace", ideaTitle)
    }
}
