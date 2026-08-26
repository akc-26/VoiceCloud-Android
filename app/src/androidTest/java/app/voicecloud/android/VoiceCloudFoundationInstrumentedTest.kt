package app.voicecloud.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceCloudFoundationInstrumentedTest {
    @Test fun packageIdentityIsVoiceCloudAndroid() { assertEquals("app.voicecloud.android.debug", androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.packageName) }
}
