package com.cyberity.cvsu
import org.junit.Assert.*
import org.junit.Test
class RegistrationPasswordPolicyTest {
    @Test fun requiresEverySelectedComponent() {
        for (value in listOf("Aa1!", "alllower1!", "ALLUPPER1!", "NoNumbers!", "NoSymbol123"))
            assertNotNull(RegistrationPasswordPolicy.validate(value))
        assertNull(RegistrationPasswordPolicy.validate("Valid123!"))
        assertNull(RegistrationPasswordPolicy.validate("Long valid Passphrase 42!"))
        assertNotNull(RegistrationPasswordPolicy.validate("Spaces123 "))
    }
}
