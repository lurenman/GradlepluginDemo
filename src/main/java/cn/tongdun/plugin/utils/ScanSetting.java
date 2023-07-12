package cn.tongdun.plugin.utils;

public class ScanSetting {


    public static final String EXTENSIONS_NAME = "tongdun";

    /**
     * 需要插入的加密方法名
     */
    public static final String STRING_DECRYPT_METHOD_NAME = "¢¢¢₵₵¢₲₲₵₵£₤£₵";

    /**
     * clinit方法名
     */
    public static final String CLINIT_METHOD_NAME = "<clinit>";

    /**
     * 构造方法
     */
    public static final String INIT_METHOD_NAME = "<init>";

    /**
     * String 字符串签名
     */
    public static final String STRING_DESCRIPTOR = "Ljava/lang/String;";
    /**
     * 需要插入的加密方法签名
     */
    public static final String STRING_DECRYPT_METHOD_DESCRIPTOR = "(Ljava/lang/String;I)Ljava/lang/String;";

    /**
     * 同盾LogUtil类owner
     */
    public static final String TONGDUN_LOGUTIL_OWNER = "cn/tongdun/android/common/util/LogUtil";

    /**
     * DeviceInfo 类
     */
    public static final String DEVICE_INFO_CLASS_OWNER = "cn/tongdun/android/core/model/DeviceInfo";

    /**
     * DeviceInfo类成员变量get方法前缀
     */
    public static final String  DEVICE_INFO_GET_METHOD_PREFIX = "getInfoBy";
}
