package com.quancheng.saluki.core.common;

import java.util.HashMap;
import java.util.Map;

public class RpcContext {

    private static final ThreadLocal<RpcContext> LOCAL       = new InheritableThreadLocal<RpcContext>() {

                                                                 @Override
                                                                 protected RpcContext initialValue() {
                                                                     return new RpcContext();
                                                                 }
                                                             };
    private final Map<String, String>            attachments = new HashMap<String, String>();
    private final Map<String, Object>            values      = new HashMap<String, Object>();

    public static RpcContext getContext() {
        return LOCAL.get();
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    private RpcContext(){
    }

    public String getAttachment(String key) {
        return attachments.get(key);
    }

    public boolean containAttachment(String key) {
        return attachments.containsKey(key);
    }

    public RpcContext setAttachment(String key, String value) {
        if (value == null) {
            attachments.remove(key);
        } else {
            attachments.put(key, value);
        }
        return this;
    }

    public RpcContext removeAttachment(String key) {
        attachments.remove(key);
        return this;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public RpcContext setAttachments(Map<String, String> attachment) {
        this.attachments.clear();
        if (attachment != null && attachment.size() > 0) {
            this.attachments.putAll(attachment);
        }
        return this;
    }

    public void clear() {
        this.attachments.clear();
        this.values.clear();
    }

    public Map<String, Object> get() {
        return values;
    }

    public RpcContext set(String key, Object value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
        return this;
    }

    public RpcContext remove(String key) {
        values.remove(key);
        return this;
    }

    public Object get(String key) {
        return values.get(key);
    }

    public boolean contain(String key) {
        return values.containsKey(key);
    }
}
