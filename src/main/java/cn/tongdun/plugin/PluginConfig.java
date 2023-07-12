package cn.tongdun.plugin;

/**
 * @description: 插件配置选项
 * @author: wuzuchang
 * @date: 2022/3/22
 */
public class PluginConfig {
    private boolean enterprise;
    private boolean release;
    private boolean openLog;
    private boolean AppKeyVerify = true;

    public boolean isAppKeyVerify() {
        return AppKeyVerify;
    }

    public void setAppKeyVerify(boolean appKeyVerify) {
        AppKeyVerify = appKeyVerify;
    }

    public boolean isEnterprise() {
        return enterprise;
    }

    public void setEnterprise(boolean enterprise) {
        this.enterprise = enterprise;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }

    public boolean isOpenLog() {
        return openLog;
    }

    public void setOpenLog(boolean openLog) {
        this.openLog = openLog;
    }
}
