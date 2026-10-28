package com.example.connecto

import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.ConnectoNetworkConfig
import com.example.connecto.security.ScreenSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenSecurityPolicyTest {

    @Before
    fun setUp() {
        // Reset state before each test
        ConnectoApiClient.currentUsername = null
        ConnectoApiClient.updateAdminState(false)
        ConnectoNetworkConfig.isUserAdmin = false
    }

    @Test
    fun testAdminUserChinnu14754x_IsAlwaysRecognizedAsAdmin() {
        ConnectoApiClient.currentUsername = "chinnu14754x"
        assertTrue("chinnu14754x must be recognized as admin", ScreenSecurityManager.isAdminUser())
        
        ConnectoApiClient.currentUsername = "@chinnu14754x"
        assertTrue("@chinnu14754x with at-sign must be recognized as admin", ScreenSecurityManager.isAdminUser())

        ConnectoApiClient.currentUsername = "CHINNU14754X"
        assertTrue("CHINNU14754X in uppercase must be recognized as admin", ScreenSecurityManager.isAdminUser())
    }

    @Test
    fun testAdminUserChinnu14754x_ScreenSecurityIsNotEnforced_ScreenshotsAllowed() {
        ConnectoApiClient.currentUsername = "chinnu14754x"
        val isEnforced = ScreenSecurityManager.isScreenSecurityEnforced()
        assertFalse("Screen security must NOT be enforced for admin chinnu14754x (screenshots & screen recording ALLOWED)", isEnforced)

        val securePolicy = ScreenSecurityManager.getSecureFlagPolicy()
        assertEquals("SecureFlagPolicy must be SecureOff for admin", androidx.compose.ui.window.SecureFlagPolicy.SecureOff, securePolicy)
    }

    @Test
    fun testRegularMemberSrinu_ScreenSecurityIsEnforced_ScreenshotsBlocked() {
        ConnectoApiClient.currentUsername = "srinu"
        ConnectoApiClient.updateAdminState(false)
        
        assertFalse("srinu must not be recognized as admin", ScreenSecurityManager.isAdminUser())
        val isEnforced = ScreenSecurityManager.isScreenSecurityEnforced()
        assertTrue("Screen security MUST be enforced for regular members (screenshots BLOCKED)", isEnforced)

        val securePolicy = ScreenSecurityManager.getSecureFlagPolicy()
        assertEquals("SecureFlagPolicy must be SecureOn for members", androidx.compose.ui.window.SecureFlagPolicy.SecureOn, securePolicy)
    }

    @Test
    fun testServerAdminFlag_OverridesUsername() {
        ConnectoApiClient.currentUsername = "test_user_777"
        ConnectoApiClient.updateAdminState(true)

        assertTrue("When server returns is_admin=true, user must be admin", ScreenSecurityManager.isAdminUser())
        assertFalse("When user is admin, screen security must be false (allowed to screenshot)", ScreenSecurityManager.isScreenSecurityEnforced())
    }
}
