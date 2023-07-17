package cn.tongdun.android.shell.shadow

import cn.tongdun.android.core.utils.FileUtil
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.File

/**
 * @Author yang.bai.
 * Date: 2022/12/2
 */
@Implements(FileUtil::class)
class ShadowFileUtil {
    companion object {
        var readIdValue: String? = null

        @Implementation
        @JvmStatic
        open fun readId(file: File?): String? {
            return readIdValue
        }
    }
}