package cn.tongdun.android.shell.shadow

import android.content.Context
import cn.tongdun.android.core.utils.PmsAndUtil
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * @Author yang.bai.
 * Date: 2022/11/28
 */
@Implements(PmsAndUtil::class)
class ShadowPmsAndUtil {
    companion object {
        var hasReadPhoneStatePms = true
        var hasPms = true
        var hasPmsAnd = true

        /**
         * 是否有读取手机信息权限，如果配置了OPTION_READ_PHONE_STATE_ENABLE，不管有没有申请权限，都返回false，不能读取
         * @param context context
         * @return true：可读取；false：不可读取
         */
        @Implementation
        @JvmStatic
        fun hasReadPhoneStatePms(context: Context?): Boolean {
            return hasReadPhoneStatePms
        }

        /**
         * 检查单个权限是否授权
         * checkCallingOrSelfPermission函数有漏洞，所以用checkPermission替代
         */
        @Implementation
        @JvmStatic
        fun hasPms(context: Context, permission: String?): Boolean {
            return hasPms
        }

        @Implementation
        @JvmStatic
        fun hasPmsAnd(context: Context?, vararg permissions: String?): Boolean {
            return hasPmsAnd
        }
    }
}