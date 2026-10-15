package com.jointestbuddy

/**
 * The installer package Android reports, as the hello sends it: com.android.vending for Google Play, null when Android
 * names none (Android Studio, adb). A blank or oversized value is dropped: the API takes 1 to 255 characters and would
 * refuse the whole hello.
 */
internal fun installerName(raw: String?): String? = raw?.takeIf { it.isNotBlank() && it.length <= 255 }
