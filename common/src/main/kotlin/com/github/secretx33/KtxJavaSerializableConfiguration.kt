package com.github.secretx33

class KtxJavaSerializableConfiguration {
    var enabled: Boolean = true
    var defaultSerialVersionUID: Long = 1L

    companion object {
        const val ENABLED_OPTION = "enabled"
        const val DEFAULT_SERIAL_VERSION_UID_OPTION = "defaultSerialVersionUID"
    }
}
