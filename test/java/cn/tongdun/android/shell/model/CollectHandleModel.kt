package cn.tongdun.android.shell.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageStats
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import cn.tongdun.android.common.setting.Config
import cn.tongdun.android.core.common.CollectHandle
import cn.tongdun.android.shell.Index
import cn.tongdun.android.shell.life.LiveTestData
import cn.tongdun.android.shell.shadow.ShadowContextWrapper
import cn.tongdun.android.shell.shadow.ShadowFileUtil
import cn.tongdun.android.shell.shadow.ShadowPmsAndUtil
import cn.tongdun.android.shell.shadow.ShadowSharedPreUtil
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowBuild
import org.robolectric.util.ReflectionHelpers

/**
 * @Author yang.bai.
 * Date: 2022/12/7
 */
class CollectHandleModel : ICollectHandleModel {
    @Index(4 - 1)
    val fmVersion: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(0 - 14 * 110)
    val buildField: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(20 - 27)
    val phoneStatus: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(28 - 33)
    val wifiInfo: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(34)
    val proxyInfo: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(36)
    val wifiList: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(3 * 83 * 128)
    val clientId_3: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(49)
    val installedPackages: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    @Index(82)
    val gpsLocation: LiveTestData<String> by lazy {
        LiveTestData<String>()
    }

    override fun getModelfmVersionAndos() {
        val fmSets = CollectHandle::class.java.getDeclaredMethod(
            "getFmSets"
        ).run {
            isAccessible = true
            invoke(CollectHandle::class.java)
        } as String
        fmVersion.setValue(fmSets)
    }

    override fun getModelBuildField(context: Context) {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            29
        )
        ShadowBuild.setVersionRelease("10")
        ShadowBuild.setModel("Infinix X683")
        ShadowBuild.setBrand("Infinix")
        ShadowBuild.setSerial("")
        ReflectionHelpers.setStaticField(
            Build::class.java,
            "DISPLAY",
            "X683-H694EFGHIJUW-Q-OP-210305V332"
        )
        ReflectionHelpers.setStaticField(Build::class.java, "HOST", "buildsrv-186")
        ShadowBuild.setDevice("Infinix-X683")
        ShadowBuild.setHardware("mt6769")
        ShadowBuild.setTags("release-keys")
        ShadowBuild.setFingerprint("Infinix/X683-OP/Infinix-X683:10/QP1A.190711.020/210305V332:user/release-keys")

        val buildfield = CollectHandle::class.java.getDeclaredMethod(
            "getBuildField", Context::class.java
        ).run {
            isAccessible = true
            invoke(CollectHandle::class.java, context)
        }
        buildField.setValue(buildfield as String)
    }

    override fun getModelPhoneStatus(context: Context, telephonyManager: TelephonyManager) {
        Config.READ_PHONE_STATE_ENABLE = false
        ShadowPmsAndUtil.hasReadPhoneStatePms = false
        val value =
            CollectHandle::class.java.getDeclaredMethod(
                "getPhoneStatus", Context::class.java,
                TelephonyManager::class.java
            ).run {
                isAccessible = true
                invoke(CollectHandle::class.java, context, telephonyManager)
            } as String
        phoneStatus.setValue(value)
    }

    override fun getModelWifiInfo(context: Context, wifiManager: WifiManager) {
        ShadowPmsAndUtil.hasPmsAnd = true
        val value =
            CollectHandle::class.java.getDeclaredMethod(
                "getWifiInfo", Context::class.java,
                WifiManager::class.java
            ).run {
                isAccessible = true
                invoke(CollectHandle::class.java, context, wifiManager)
            } as String
        wifiInfo.setValue(value)
    }

    override fun getModelProxyInfo(context: Context) {
        val value =
            CollectHandle::class.java.getDeclaredMethod(
                "getProxyInfo", Context::class.java
            ).run {
                isAccessible = true
                invoke(CollectHandle::class.java, context)
            } as String
        proxyInfo.setValue(value)
    }

    override fun getModelWifiList(context: Context, wifiManager: WifiManager) {
        ShadowPmsAndUtil.hasPmsAnd = true
        val shadowWifiManager = Shadows.shadowOf(wifiManager)
        val scanResults = mutableListOf<ScanResult>().apply {
            add(ScanResult().apply {
                BSSID = "70:3a:0e:92:3b:e3"
                SSID = "ZBX"
                level = -74
            })
            add(ScanResult().apply {
                BSSID = "70:3a:0e:92:3b:f0"
                SSID = "TONGDUN"
                level = -68
            })
        }
        shadowWifiManager.setScanResults(scanResults)
        val value =
            CollectHandle::class.java.getDeclaredMethod(
                "getWifiList",
                Context::class.java,
                WifiManager::class.java
            ).run {
                isAccessible = true
                invoke(CollectHandle::class.java, context, wifiManager)
            } as String
        wifiList.setValue(value)
    }

    override fun getModelClientId_3(context: Context) {
        ShadowSharedPreUtil.readValue =
            "BA5E78EEF52D14A589EA59CFFA4D7CAB40273AF92881D5CB4C32F7681616B79D1E0548AC79CCB309A03467C8DBF4101D"
        ShadowFileUtil.readIdValue =
            "F3A8B0EA46F63D51D44EA78EE2F5536497425C81FD9380748F9DCA7F52E6E725F0486B112753B7E7AFAD07A0ACD9EF45"
        clientId_3.setValue(CollectHandle::class.java.getDeclaredMethod(
            "getClient_3", Context::class.java
        ).run {
            isAccessible = true
            invoke(CollectHandle::class.java, context)
        } as String)
    }

    override fun getModelInstalledPackages(context: Context) {
        val codePackageList = arrayOf(
            "com.ClientUpCode",
            "me.ym51.app",
            "com.example.qwk"
        )
        val index = (Math.random() * 3).toInt()
        //模拟生成
        val packageInfo = PackageInfo()
        packageInfo.packageName = codePackageList[index]
        packageInfo.applicationInfo = ApplicationInfo().apply {
            flags = 8388608
            packageName = codePackageList[index]
        }
        val packageStats = PackageStats(packageInfo.packageName)
        Shadows.shadowOf(context.getPackageManager()).apply {
            addPackage(packageInfo, packageStats)
        }
        installedPackages.setValue(CollectHandle::class.java.getDeclaredMethod(
            "getInstalledPackages", Context::class.java
        ).run {
            isAccessible = true
            invoke(CollectHandle::class.java, context)
        } as String)
    }

    override fun getModelGPSLocation(context: Context, locationManager: LocationManager) {
        //设置权限通过
        ShadowContextWrapper.checkPermissionValue = 0
        Shadows.shadowOf(locationManager).apply {
            setProviderEnabled(LocationManager.GPS_PROVIDER, true)
            val lastLocation = Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 30.282356
                longitude = 120.011815
                accuracy = 29f
                provider = LocationManager.GPS_PROVIDER
            }
            setLastKnownLocation(LocationManager.GPS_PROVIDER, lastLocation)
        }
        gpsLocation.setValue(CollectHandle::class.java.getDeclaredMethod(
            "getGPSLocation", Context::class.java,
            LocationManager::class.java
        ).run {
            isAccessible = true
            invoke(CollectHandle::class.java, context, locationManager)
        } as String)
    }
}