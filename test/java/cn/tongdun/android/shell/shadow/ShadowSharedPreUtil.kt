package cn.tongdun.android.shell.shadow

import android.content.Context
import cn.tongdun.android.common.util.SharedPreUtil
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * @Author yang.bai.
 * Date: 2022/12/2
 */
@Implements(SharedPreUtil::class)
class ShadowSharedPreUtil {
    companion object {
        var readValue: String? = null

        @Implementation
        @JvmStatic
        fun read(context: Context?, key: String?, deValue: String?): String? {
            return readValue
        }
    }
}