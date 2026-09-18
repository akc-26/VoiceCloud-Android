package app.voicecloud.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RegisterViewModelValidationTest {
    @Test
    fun registerUiState_defaultsAreEmpty() {
        val state = RegisterUiState()
        assertEquals("", state.email)
        assertEquals(false, state.isSubmitting)
    }

    @Test
    fun phoneAuthUiState_tracksCodeSent() {
        val state = PhoneAuthUiState(codeSent = true)
        assertNotNull(state)
        assertEquals(true, state.codeSent)
    }
}
