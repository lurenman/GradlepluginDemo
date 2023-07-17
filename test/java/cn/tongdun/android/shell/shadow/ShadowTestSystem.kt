package cn.tongdun.android.shell.shadow

import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * @Author yang.bai.
 * Date: 2022/12/6
 * todo：代理System类失败
 */
@Implements(System::class)
class ShadowTestSystem {
    companion object {
        const val key_host = "http.proxyHost"
        const val key_port = "http.proxyPort"
        var propertyValue: String? = null

        @Implementation
        @JvmStatic
        public open fun getProperty(key: String): String? {
            propertyValue = when (key) {
                key_host -> {
                    "127.0.0.1"
                }
                key_port -> {
                    "1087"
                }
                else -> null
            }
            return propertyValue
        }
    }
}