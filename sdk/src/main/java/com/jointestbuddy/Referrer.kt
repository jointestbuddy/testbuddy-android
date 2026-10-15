package com.jointestbuddy

// The place code TestBuddy puts in the Play install link (referrer=tb_XXXXXX). An install from anywhere else reads
// Play's default "utm_source=google-play&utm_medium=organic", which carries no code.
private val PLACE_CODE = Regex("""(?:^|[&?])tb_([A-Za-z0-9]{6})(?:$|&)""")

/** The place code in an install referrer, or null. */
internal fun codeFromReferrer(referrer: String?): String? = referrer?.let { PLACE_CODE.find(it)?.groupValues?.get(1) }

/** The place code in a link opened from the web (testbuddy://<package>/link?code=XXXXXX), or null. */
internal fun codeFromLink(code: String?): String? = code?.takeIf { it.matches(Regex("[A-Za-z0-9]{6}")) }
