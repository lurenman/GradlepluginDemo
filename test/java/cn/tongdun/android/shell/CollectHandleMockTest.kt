package cn.tongdun.android.shell

import cn.tongdun.android.common.setting.Constants
import cn.tongdun.android.shell.life.Observer
import cn.tongdun.android.shell.model.CollectHandleModel
import cn.tongdun.android.shell.model.ICollectHandleModel
import cn.tongdun.android.shell.shadow.*
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowBuild

/**
 * @Author yang.bai.
 * Date: 2022/11/28
 *  如何运行:  './gradlew sdk-shell:testReleaseUnitTest --parallel' 或者 AS右键 Run .第一次运行会特别慢(因为要下载依赖库)
 *  参考资料：https://blog.csdn.net/shensky711/article/details/53561172
 * https://github.com/robolectric/robolectric
 * https://blog.csdn.net/Clever99/article/details/106672707/
 */
@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(
    manifest = org.robolectric.annotation.Config.NONE,
    shadows = arrayOf(
        ShadowTestSystem::class,
        ShadowPmsAndUtil::class,
        ShadowFileUtil::class,
        ShadowSharedPreUtil::class,
        ShadowHelperJNI::class,
        ShadowBuild::class,
        ShadowContextWrapper::class
    )
)
class CollectHandleMockTest : BaseMockTest(), ICollectHandleModel by mCollectHandleModel {
    companion object {
        val TAG = "CollectHandleMockTest->"
        val mCollectHandleModel: CollectHandleModel = CollectHandleModel()
    }

    /**
     * 获取fmVersion/os
     */
    @Test
    fun getfmVersionAndos() {
        val expected = "${Constants.VERSION}^^${Constants.OS}"
        mCollectHandleModel.fmVersion.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getfmVersionAndos:${value}")
                assertEquals(expected, value)
            }
        })
        getModelfmVersionAndos()
    }

    /**
     * 获取build字段ShadowBuild
     */
    @Test
    fun getBuildField() {
        val expected =
            "29^^10^^Infinix X683^^robolectric^^Infinix^^^^X683-H694EFGHIJUW-Q-OP-210305V332^^buildsrv-186^^Infinix-X683^^mt6769^^release-keys^^Infinix/X683-OP/Infinix-X683:10/QP1A.190711.020/210305V332:user/release-keys"
        mCollectHandleModel.buildField.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getBuildField:${value}")
                assertEquals(expected, value)
            }
        })
        getModelBuildField(mContext)
    }

    /**
     * 测试phoneStatus
     */
    @Index(20 - 27)
    @Test
    fun getPhoneStatus() {
        val expected = "^^^^^^^^^^^^^^^^^^^^^^^^"
        mCollectHandleModel.phoneStatus.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getPhoneStatus:${value}")
                assertEquals(expected, value)
            }
        })
        getModelPhoneStatus(mContext, mTelephonyManager)
    }

    @Test
    fun getWifiInfo() {
        val expected = "0.0.0.0^^^^TONGDUN^^34:fc:b9:fc:2f:30^^0^^0"
        mCollectHandleModel.wifiInfo.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getWifiInfo:${value}")
                assertEquals(expected, value)
            }
        })
        getModelWifiInfo(mContext, mWifiManager)
    }

    @Test
    fun getProxyInfo() {
        mCollectHandleModel.proxyInfo.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getProxyInfo:${value}")
            }
        })
        getModelProxyInfo(mContext)
    }

    @Index(36)
    @Test
    fun getWifiList() {
        val expected =
            "[[\"70:3a:0e:92:3b:e3\",\"ZBX\",-74],[\"70:3a:0e:92:3b:f0\",\"TONGDUN\",-68]]"
        mCollectHandleModel.wifiList.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${CollectHandleMockTest.TAG} getWifiList:${value}")
                assertEquals(expected, value)
            }
        })
        getModelWifiList(mContext, mWifiManager)
    }

    /**
     * 获取clientid3
     */
    @Test
    fun getClientId_3() {
        val expected =
            "012badc8-cb3a-41c0-8775e-ef655a4a149^^0^^012badc8-cb3a-41c0-8775e-ef655a4a149#012badc8-cb3a-41c0-8775e-ef655a4a149"
        mCollectHandleModel.clientId_3.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getClientId_3:${value}")
                assertEquals(expected, value)
            }
        })
        getModelClientId_3(mContext)
    }

    /**
     *获取安装包
     */
    @Test
    fun getInstalledPackages() {
        mCollectHandleModel.installedPackages.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getInstalledPackages:${value}")
                assertNotEquals("", value)
            }
        })
        getModelInstalledPackages(mContext)
    }

    /**
     * 获取版本
     */
    @Test
    fun getVersion() {
        val version = FMAgent.getVersion()
        println("${TAG} getVersion:${version}")
        assertThat(version, notNullValue())
    }

    /**
     * 获取gpsLocation
     * "gpsLocation": "{\"type\":\"network\",\"lat\":30.282356,\"long\":120.011815,\"acc\":29}"
     */
    @Test
    fun getGPSLocation() {
        val expected = "{\"acc\":29,\"type\":\"gps\",\"lat\":30.282356,\"long\":120.011815}"
        mCollectHandleModel.gpsLocation.observe(this, object : Observer<String> {
            override fun onChanged(value: String?) {
                println("${TAG} getGPSLocation:${value}")
                assertEquals(expected, value)
            }
        })
        getModelGPSLocation(mContext, mLocationManager)
    }
}