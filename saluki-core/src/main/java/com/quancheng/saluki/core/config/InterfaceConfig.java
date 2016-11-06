package com.quancheng.saluki.core.config;

import java.io.Serializable;

public class InterfaceConfig implements Serializable {

    private static final long serialVersionUID = 1574013084792698823L;

    // 分组
    private String            group;

    // 服务版本
    private String            version;

    // 接口名
    private String            serviceName;

    // 引用的实现
    private Object            ref;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        InterfaceConfig other = (InterfaceConfig) obj;
        if (group == null) {
            if (other.group != null) return false;
        } else if (!group.equals(other.group)) return false;
        if (ref == null) {
            if (other.ref != null) return false;
        } else if (!ref.equals(other.ref)) return false;
        if (serviceName == null) {
            if (other.serviceName != null) return false;
        } else if (!serviceName.equals(other.serviceName)) return false;
        if (version == null) {
            if (other.version != null) return false;
        } else if (!version.equals(other.version)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "InterfaceConfig [group=" + group + ", version=" + version + ", serviceName=" + serviceName + ", ref="
               + ref + "]";
    }

}
