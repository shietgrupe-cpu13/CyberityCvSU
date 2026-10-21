package com.cyberity.cvsu
import org.junit.Assert.*
import org.junit.Test
class AuthFailureMessagesTest {
    @Test fun wrongPasswordAndUnknownEmailUseTheSameGenericMessage() {
        for(code in listOf("ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_LOGIN_CREDENTIALS", "ERROR_INVALID_CREDENTIAL"))
            assertEquals("Invalid email or password. Please try again.", AuthCredentialErrors.messageFor(code))
        assertNull(AuthCredentialErrors.messageFor("ERROR_NETWORK_REQUEST_FAILED"))
        assertNull(AuthCredentialErrors.messageFor("ERROR_TOO_MANY_REQUESTS"))
    }
}
