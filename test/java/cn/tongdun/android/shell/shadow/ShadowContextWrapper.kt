package cn.tongdun.android.shell.shadow

import android.content.ContextWrapper
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * @Author yang.bai.
 * Date: 2023/1/6
 */
@Implements(ContextWrapper::class)
class ShadowContextWrapper {
    companion object {
        var checkPermissionValue: Int = -1

        @Implementation
        @JvmStatic
        open fun checkPermission(permission: String?, pid: Int, uid: Int): Int {
            return checkPermissionValue
        }
    }
}