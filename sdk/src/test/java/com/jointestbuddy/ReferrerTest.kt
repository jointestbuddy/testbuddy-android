package com.jointestbuddy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReferrerTest {
    @Test
    fun readsThePlaceCodeTestBuddyPutsInThePlayLink() {
        assertEquals("K7M2QX", codeFromReferrer("tb_K7M2QX"))
        assertEquals("K7M2QX", codeFromReferrer("utm_source=x&tb_K7M2QX&utm_medium=y"))
    }

    @Test
    fun anInstallFromElsewhereHasNoCode() {
        assertNull(codeFromReferrer("utm_source=google-play&utm_medium=organic"))
        assertNull(codeFromReferrer(null))
        assertNull(codeFromReferrer("tb_K7M2QXZ"))
    }

    @Test
    fun aWebLinkCarriesSixLettersOrDigits() {
        assertEquals("9tfje9", codeFromLink("9tfje9"))
        assertNull(codeFromLink("9TFJE"))
        assertNull(codeFromLink("../../x"))
    }
}
