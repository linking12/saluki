package com.quancheng.saluki.core.grpc.server;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.BindableService;

public class ProtocolExporterFactory {

    private static class ProtocolExporterFactoryHolder {

        private static final ProtocolExporterFactory INSTANCE = new ProtocolExporterFactory();
    }

    private ProtocolExporterFactory(){
    }

    public static final ProtocolExporterFactory getInstance() {
        return ProtocolExporterFactoryHolder.INSTANCE;
    }

    public ProtocolExporter getProtocolExporter(SalukiURL providerUrl, Object protocolinstance) {
        ProtocolExporter protocolExporter;
        if (protocolinstance instanceof BindableService) {
            protocolExporter = new StubProtocolExporter(protocolinstance.getClass(), protocolinstance);
        } else {
            // 如果是泛化导出，直接导出类本身
            boolean isGeneric = providerUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
            Class<?> protocolClass;
            if (isGeneric) {
                protocolClass = protocolinstance.getClass();
            } else {
                try {
                    protocolClass = ReflectUtil.name2class(providerUrl.getServiceInterface());
                    if (!protocolClass.isAssignableFrom(protocolinstance.getClass())) {
                        throw new IllegalStateException("protocolClass " + providerUrl.getServiceInterface()
                                                        + " is not implemented by protocolImpl which is of class "
                                                        + protocolinstance.getClass());
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e.getMessage(), e);

                }
            }
            protocolExporter = new NormalProtocolExporter(protocolClass, protocolinstance);
        }
        return protocolExporter;
    }
}
