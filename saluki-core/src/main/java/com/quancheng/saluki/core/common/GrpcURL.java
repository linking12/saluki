package com.quancheng.saluki.core.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.quancheng.saluki.core.utils.CollectionUtils;
import com.quancheng.saluki.core.utils.NetUtils;

public class GrpcURL implements Serializable {

    private static final long                       serialVersionUID = -1985165475234910535L;

    private final String                            protocol;

    private final String                            username;

    private final String                            password;

    private final String                            host;

    private final int                               port;

    private final String                            path;

    private final Map<String, String>               parameters;

    // ==== cache ====

    private volatile transient Map<String, Number>  numbers;

    private volatile transient Map<String, GrpcURL> urls;

    private volatile transient String               ip;

    private volatile transient String               full;

    private volatile transient String               identity;

    private volatile transient String               parameter;

    private volatile transient String               string;

    protected GrpcURL(){
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public GrpcURL(String protocol, String host, int port){
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public GrpcURL(String protocol, String host, int port, String[] pairs){
        this(protocol, null, null, host, port, null, CollectionUtils.toStringMap(pairs));
    }

    public GrpcURL(String protocol, String host, int port, Map<String, String> parameters){
        this(protocol, null, null, host, port, null, parameters);
    }

    public GrpcURL(String protocol, String host, int port, String path){
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public GrpcURL(String protocol, String host, int port, String path, String... pairs){
        this(protocol, null, null, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public GrpcURL(String protocol, String host, int port, String path, Map<String, String> parameters){
        this(protocol, null, null, host, port, path, parameters);
    }

    public GrpcURL(String protocol, String username, String password, String host, int port, String path){
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public GrpcURL(String protocol, String username, String password, String host, int port, String path,
                   String... pairs){
        this(protocol, username, password, host, port, path, CollectionUtils.toStringMap(pairs));
    }

    public GrpcURL(String protocol, String username, String password, String host, int port, String path,
                   Map<String, String> parameters){
        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        if (protocol.equals(Constants.REMOTE_PROTOCOL)) {
            if (!parameters.containsKey(Constants.GROUP_KEY)) {
                parameters.put(Constants.GROUP_KEY, Constants.DEFAULT_GROUP);
            }
            if (!parameters.containsKey(Constants.VERSION_KEY)) {
                parameters.put(Constants.VERSION_KEY, Constants.DEFAULT_VERSION);
            }
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public static GrpcURL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?"); // seperator between body and parameters
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) host = url;
        return new GrpcURL(protocol, username, password, host, port, path, parameters);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthority() {
        if ((username == null || username.length() == 0) && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) + ":" + (password == null ? "" : password);
    }

    public String getHost() {
        return host;
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public String getPath() {
        return path;
    }

    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public GrpcURL setProtocol(String protocol) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setUsername(String username) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setPassword(String password) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setHost(String host) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setPort(int port) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public GrpcURL setPath(String path) {
        return new GrpcURL(protocol, username, password, host, port, path, getParameters());
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameterAndDecoded(String key) {
        return getParameterAndDecoded(key, null);
    }

    public String getParameterAndDecoded(String key, String defaultValue) {
        return decode(getParameter(key, defaultValue));
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        return value;
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Constants.COMMA_SPLIT_PATTERN.split(value);
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) { // 允许并发重复创建
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }

    private Map<String, GrpcURL> getUrls() {
        if (urls == null) { // 允许并发重复创建
            urls = new ConcurrentHashMap<String, GrpcURL>();
        }
        return urls;
    }

    public GrpcURL getUrlParameter(String key) {
        GrpcURL u = getUrls().get(key);
        if (u != null) {
            return u;
        }
        String value = getParameterAndDecoded(key);
        if (value == null || value.length() == 0) {
            return null;
        }
        u = GrpcURL.valueOf(value);
        getUrls().put(key, u);
        return u;
    }

    public double getParameter(String key, double defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.doubleValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        getNumbers().put(key, d);
        return d;
    }

    public float getParameter(String key, float defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(key, f);
        return f;
    }

    public long getParameter(String key, long defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    public short getParameter(String key, short defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.shortValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(key, s);
        return s;
    }

    public byte getParameter(String key, byte defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.byteValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(key, b);
        return b;
    }

    public char getParameter(String key, char defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    public boolean isLocalHost() {
        return NetUtils.isLocalHost(host) || getParameter(Constants.LOCALHOST_KEY, false);
    }

    public boolean isAnyHost() {
        return Constants.ANYHOST_VALUE.equals(host) || getParameter(Constants.ANYHOST_KEY, false);
    }

    public GrpcURL addParameterAndEncoded(String key, String value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, encode(value));
    }

    public GrpcURL addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, Enum<?> value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, Number value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) return this;
        return addParameter(key, String.valueOf(value));
    }

    public GrpcURL addParameter(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        // 如果没有修改，直接返回。
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new GrpcURL(protocol, username, password, host, port, path, map);
    }

    public GrpcURL addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new GrpcURL(protocol, username, password, host, port, path, map);
    }

    public GrpcURL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null && entry.getValue() != null || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual) return this;

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new GrpcURL(protocol, username, password, host, port, path, map);
    }

    public GrpcURL addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new GrpcURL(protocol, username, password, host, port, path, map);
    }

    public GrpcURL addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }

    public GrpcURL addParameterString(String query) {
        if (query == null || query.length() == 0) {
            return this;
        }
        return addParameters(GrpcURLUtils.parseQueryString(query));
    }

    public GrpcURL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    public GrpcURL removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    public GrpcURL removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new GrpcURL(protocol, username, password, host, port, path, map);
    }

    public GrpcURL clearParameters() {
        return new GrpcURL(protocol, username, password, host, port, path, new HashMap<String, String>());
    }

    public String getRawParameter(String key) {
        if ("protocol".equals(key)) return protocol;
        if ("username".equals(key)) return username;
        if ("password".equals(key)) return password;
        if ("host".equals(key)) return host;
        if ("port".equals(key)) return String.valueOf(port);
        if ("path".equals(key)) return path;
        return getParameter(key);
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>(parameters);
        if (protocol != null) map.put("protocol", protocol);
        if (username != null) map.put("username", username);
        if (password != null) map.put("password", password);
        if (host != null) map.put("host", host);
        if (port > 0) map.put("port", String.valueOf(port));
        if (path != null) map.put("path", path);
        return map;
    }

    public String toString() {
        if (string != null) {
            return string;
        }
        return string = buildString(false, true); // no show username and password
    }

    public String toString(String... parameters) {
        return buildString(false, true, parameters); // no show username and password
    }

    public String toIdentityString() {
        if (identity != null) {
            return identity;
        }
        return identity = buildString(true, false); // only return identity message, see the method "equals" and
                                                    // "hashCode"
    }

    public String toIdentityString(String... parameters) {
        return buildString(true, false, parameters); // only return identity message, see the method "equals" and
                                                     // "hashCode"
    }

    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    public String toFullString(String... parameters) {
        return buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                    && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService,
                               String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public URI toJavaURI() {
        try {
            return new URI(toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public String getServiceKey() {
        String inf = getServiceInterface();
        if (inf == null) return null;
        StringBuilder buf = new StringBuilder();
        String group = getGroup();
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = getVersion();
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    public String getGroup() {
        String group = getParameter(Constants.GROUP_KEY, Constants.DEFAULT_GROUP);
        return group;
    }

    public String getVersion() {
        String group = getParameter(Constants.VERSION_KEY, Constants.DEFAULT_VERSION);
        return group;
    }

    public String getServiceInterface() {
        return getParameter(Constants.INTERFACE_KEY, path);
    }

    public String toServiceString() {
        return buildString(true, false, true, true);
    }

    public GrpcURL setServiceInterface(String service) {
        return addParameter(Constants.INTERFACE_KEY, service);
    }

    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + ((this.getGroup() == null) ? 0 : this.getGroup().hashCode());
        result = prime * result + ((this.getVersion() == null) ? 0 : this.getVersion().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GrpcURL other = (GrpcURL) obj;
        if (host == null) {
            if (other.host != null) return false;
        } else if (!host.equals(other.host)) return false;
        if (password == null) {
            if (other.password != null) return false;
        } else if (!password.equals(other.password)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        if (port != other.port) return false;
        if (protocol == null) {
            if (other.protocol != null) return false;
        } else if (!protocol.equals(other.protocol)) return false;
        if (username == null) {
            if (other.username != null) return false;
        } else if (!username.equals(other.username)) return false;
        if (this.getGroup() == null) {
            if (other.getGroup() != null) return false;
        } else if (!this.getGroup().equals(other.getGroup())) return false;
        if (this.getVersion() == null) {
            if (other.getVersion() != null) return false;
        } else if (!this.getVersion().equals(other.getVersion())) return false;

        return true;
    }

}
