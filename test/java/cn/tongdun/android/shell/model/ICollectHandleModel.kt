package cn.tongdun.android.shell.model

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager

/**
 * @Author yang.bai.
 * Date: 2022/12/7
 */
interface ICollectHandleModel {
    fun getModelfmVersionAndos()
    fun getModelBuildField(context: Context)
    fun getModelPhoneStatus(context: Context, telephonyManager: TelephonyManager)
    fun getModelWifiInfo(context: Context, wifiManager: WifiManager)
    fun getModelProxyInfo(context: Context)
    fun getModelWifiList(context: Context, wifiManager: WifiManager)
    fun getModelClientId_3(context: Context)
    fun getModelInstalledPackages(context: Context)
    fun getModelGPSLocation(context: Context, locationManager: LocationManager)

}