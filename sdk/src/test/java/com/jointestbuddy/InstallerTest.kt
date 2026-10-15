package com.jointestbuddy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InstallerTest {
    @Test
    fun aGooglePlayInstallNamesThePlayStore() {
        assertEquals("com.android.vending", installerName("com.android.vending"))
    }

    @Test
    fun anInstallFromAndroidStudioOrAdbNamesNoInstaller() {
        assertNull(installerName(null))
    }

    @Test
    fun aValueTheApiWouldRefuseIsLeftOut() {
        assertNull(installerName(""))
        assertNull(installerName(" "))
        assertNull(installerName("a".repeat(256)))
    }
}
