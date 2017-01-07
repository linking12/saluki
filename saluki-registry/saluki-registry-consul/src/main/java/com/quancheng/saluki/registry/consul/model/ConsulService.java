package com.quancheng.saluki.registry.consul.model;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import com.ecwid.consul.v1.agent.model.NewService;

public final class ConsulService {

    private final String      name;
    private final String      id;
    private final String      address;
    private final Integer     port;
    private final Set<String> tags;
    private final String      interval;

    private ConsulService(Builder builder){
        this.name = builder.name;
        this.id = builder.id != null ? builder.id : name + ":" + UUID.randomUUID().toString();
        this.address = builder.address;
        this.port = builder.port;
        this.tags = unmodifiableSet(new HashSet<>(builder.tags));
        this.interval = builder.interval;
    }

    public NewService getNewService() {
        NewService consulService = new NewService();
        consulService.setName(this.name);
        consulService.setId(this.id);
        consulService.setAddress(this.address);
        consulService.setPort(this.port);
        consulService.setTags(unmodifiableList(new ArrayList<>(this.tags)));
        NewService.Check check = new NewService.Check();
        check.setTtl(this.interval + "s");
        check.setDeregisterCriticalServiceAfter("3m");
        consulService.setCheck(check);
        return consulService;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getInterval() {
        return interval;
    }

    public static Builder newSalukiService() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Service{" + //
               "name=" + name + //
               ", id=" + id + //
               ", address=" + address + //
               ", port=" + port + //
               ", interval=" + interval + //
               ", tags=" + tags + '}';//
    }

    String toConsulRegistrationJson() {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        append(builder, "ID", id);
        builder.append(",");
        append(builder, "Name", name);
        builder.append(",");
        if (!tags.isEmpty()) {
            builder.append("\"Tags\":[");
            StringJoiner joiner = new StringJoiner(",");
            tags.stream().map(t -> "\"" + t + "\"").forEach(joiner::add);
            builder.append(joiner.toString());
            builder.append("],");
        }
        if (address != null) {
            append(builder, "Address", address);
            builder.append(",");
        }
        append(builder, "Port", port);
        builder.append(",");
        builder.append("\"Check\":{");
        append(builder, "Interval", interval);
        builder.append("}");
        builder.append("}");
        return builder.toString();
    }

    private void append(StringBuilder builder, String key, String value) {
        builder.append("\"");
        builder.append(key);
        builder.append("\":\"");
        builder.append(value);
        builder.append("\"");
    }

    private void append(StringBuilder builder, String key, Integer value) {
        builder.append("\"");
        builder.append(key);
        builder.append("\":");
        builder.append(value);
        builder.append("");
    }

    public static class Builder extends AbstractBuilder {

        private String      name;
        private String      id;
        private String      address;
        private Integer     port;
        private Set<String> tags = new HashSet<String>();
        private String      interval;

        public Builder withName(String name) {
            this.name = substituteEnvironmentVariables(name);
            return this;
        }

        public Builder withId(String id) {
            this.id = substituteEnvironmentVariables(id);
            return this;
        }

        public Builder withAddress(String address) {
            this.address = substituteEnvironmentVariables(address);
            return this;
        }

        public Builder withPort(String port) {
            this.port = Integer.parseInt(substituteEnvironmentVariables(port));
            return this;
        }

        public Builder withCheckInterval(String interval) {
            final String value = substituteEnvironmentVariables(interval);
            this.interval = value;
            return this;
        }

        public Builder withTag(String tag) {
            tags.add(substituteEnvironmentVariables(tag));
            return this;
        }

        public Builder withTags(List<String> tags) {
            for (String tag : tags) {
                this.tags.add(substituteEnvironmentVariables(tag));
            }
            return this;
        }

        public ConsulService build() {
            if (name == null) {
                throw new java.lang.IllegalArgumentException("Required service name is missing");
            }
            if (port == null) {
                throw new java.lang.IllegalArgumentException("Required port is missing for service " + name);
            }
            return new ConsulService(this);
        }

    }
}
