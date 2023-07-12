package cn.tongdun.plugin.model;

/**
 * @description: DeviceInfo 成员变量
 * @author: wuzuchang
 * @date: 2023/3/20
 */
public class DeviceInfoField {
    private String name;
    private String descriptor;
    private String signature;

    public DeviceInfoField(String name, String descriptor, String signature) {
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
