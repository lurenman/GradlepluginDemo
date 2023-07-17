package cn.tongdun.android.shell

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider

import cn.tongdun.android.common.util.LogUtil
import cn.tongdun.android.shell.life.LifeTestcycle
import cn.tongdun.android.shell.life.LifeTestcycleOwner
import cn.tongdun.android.shell.life.LifeTestcycleRegistry
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass

/**
 * @Author yang.bai.
 * Date: 2022/11/28
 * 参考jetpack实现简单的livedata
 */
open class BaseMockTest : LifeTestcycleOwner {
    companion object {
        /**
         * 在类中所有方法前运行。此注解修饰的方法必须是static void
         */
        @BeforeClass
        @JvmStatic
        fun BeforeClassTest() {
            println("BeforeClassTest执行")

        }

        /**
         * 在类中最后运行。此注解修饰的方法必须是static void
         */
        @AfterClass
        @JvmStatic
        fun AfterClassTest() {
            println("AfterClassTest执行")

        }
    }

    private val mLifecycleRegistry: LifeTestcycleRegistry = LifeTestcycleRegistry(this)

    protected var mContext: Context
    protected var mTelephonyManager: TelephonyManager
    protected var mWifiManager: WifiManager
    protected var mConnectivityManager: ConnectivityManager
    protected var mLocationManager: LocationManager

    init {
        mContext = ApplicationProvider.getApplicationContext<Context>()
        mContext.run {
            mWifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
            mTelephonyManager =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mConnectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            mLocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }


    @Before
    open fun setup() {
        LogUtil.openLog()
        mLifecycleRegistry.handleLifecycleEvent(LifeTestcycle.Event.ON_RUN)
    }

    @After
    open fun after() {
        mLifecycleRegistry.handleLifecycleEvent(LifeTestcycle.Event.ON_STOP)
    }

    override fun getLifecycle(): LifeTestcycle {
        return mLifecycleRegistry
    }
}