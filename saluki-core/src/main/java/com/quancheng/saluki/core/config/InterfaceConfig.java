package com.quancheng.saluki.core.config;

import java.io.Serializable;

public class InterfaceConfig implements Serializable {

    private static final long serialVersionUID = 1574013084792698823L;

    // 分组
    private String            group;

    // 服务版本
    private String            version;

    // 接口名
    private String            interfaceName;

    // 是否使用泛接口
    private Boolean           generic;

    // 是否是原生Grpc服务
    private Boolean           grpcStub;

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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Boolean getGeneric() {
        return generic;
    }

    public void setGeneric(Boolean generic) {
        this.generic = generic;
    }

    public Boolean getGrpcStub() {
        return grpcStub;
    }

    public void setGrpcStub(Boolean grpcStub) {
        this.grpcStub = grpcStub;
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
        result = prime * result + ((generic == null) ? 0 : generic.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((grpcStub == null) ? 0 : grpcStub.hashCode());
        result = prime * result + ((interfaceName == null) ? 0 : interfaceName.hashCode());
        result = prime * result + ((ref == null) ? 0 : ref.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        InterfaceConfig other = (InterfaceConfig) obj;
        if (generic == null) {
            if (other.generic != null) return false;
        } else if (!generic.equals(other.generic)) return false;
        if (group == null) {
            if (other.group != null) return false;
        } else if (!group.equals(other.group)) return false;
        if (grpcStub == null) {
            if (other.grpcStub != null) return false;
        } else if (!grpcStub.equals(other.grpcStub)) return false;
        if (interfaceName == null) {
            if (other.interfaceName != null) return false;
        } else if (!interfaceName.equals(other.interfaceName)) return false;
        if (ref == null) {
            if (other.ref != null) return false;
        } else if (!ref.equals(other.ref)) return false;
        if (version == null) {
            if (other.version != null) return false;
        } else if (!version.equals(other.version)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "InterfaceConfig [group=" + group + ", version=" + version + ", interfaceName=" + interfaceName
               + ", generic=" + generic + ", grpcStub=" + grpcStub + ", ref=" + ref + "]";
    }

}
