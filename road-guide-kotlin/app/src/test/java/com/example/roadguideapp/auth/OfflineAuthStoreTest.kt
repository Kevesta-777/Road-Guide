package com.example.roadguideapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineAuthStoreTest {

    

    @Test
    fun validateIdentifier_rejectsTooShort() {
        assertEquals(AuthError.IdentifierTooShort, OfflineAuthStore.validateIdentifier("a"))
    }

    @Test
    fun validateIdentifier_acceptsValid() {
        assertNull(OfflineAuthStore.validateIdentifier("roadguide"))
    }

    @Test
    fun validatePassword_rejectsTooShort() {
        assertEquals(AuthError.PasswordTooShort, OfflineAuthStore.validatePassword("12345"))
    }

    @Test
    fun validatePassword_acceptsValid() {
        assertNull(OfflineAuthStore.validatePassword("123456"))
    }

    @Test
    fun identifierAbbreviation_twoWords() {
        assertEquals("JD", identifierAbbreviation("John Doe"))
    }

    @Test
    fun identifierAbbreviation_singleWord() {
        assertEquals("RO", identifierAbbreviation("roadguide"))
    }
}
