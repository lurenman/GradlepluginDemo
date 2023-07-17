package cn.tongdun.android.shell.shadow

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import androidx.test.core.app.ApplicationProvider
import cn.tongdun.android.common.setting.Constants
import cn.tongdun.android.core.utils.CacheDeviceInfoUtil
import cn.tongdun.android.shell.common.HelperJNI
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowWifiInfo

/**
 * @Author yang.bai.
 * Date: 2022/12/2
 * C 层mock
 */
@Implements(HelperJNI::class)
object ShadowHelperJNI {
    var exportValue: Any? = null

    @Implementation
    @JvmStatic
    open fun exprot(type: Int, args: Any?): Any? {
        val arrayOfAnys = args as Array<*>
        when (type) {
            Constants.JniCallCode.CODE_GENERATE_KEY -> {
                exportValue = if (arrayOfAnys[0] == 1) {
                    "bs3ggr0ismnzmdwxkacrq88xs9uj3l06"
                } else {
                    "ykj314o0nd8423k2cimo5fvx0k234sc5"
                }
            }
            Constants.JniCallCode.CODE_GET_MAPCACHE_DATA -> {
                if (arrayOfAnys[0] == CacheDeviceInfoUtil.WIFIMANAGER_KEY[0]) {
                    val context = ApplicationProvider.getApplicationContext() as Context
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.getConnectionInfo()
                    val shadowWifiInfo = Shadow.extract<ShadowWifiInfo>(wifiInfo)
                    shadowWifiInfo.setSSID("TONGDUN")
                    shadowWifiInfo.setBSSID("34:fc:b9:fc:2f:30")
                    exportValue = wifiInfo
                }
                if (arrayOfAnys[0] == CacheDeviceInfoUtil.WIFIMANAGER_KEY[1]) {
                    exportValue = DhcpInfo().apply {
                        gateway = 32651530
                        netmask = 0
                    }
                }
                if (arrayOfAnys[0] == CacheDeviceInfoUtil.WIFIMANAGER_KEY[2]) {
                    exportValue = true
                }
            }
        }
        return exportValue
    }

}