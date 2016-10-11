package com.quancheng.saluki.serializer.pojo;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;

@ProtobufEntity(com.quancheng.saluki.serializer.proto.Message.Person.class)
public class Person {

    @ProtobufAttribute
    private String  name;

    @ProtobufAttribute
    private Integer age;

    @ProtobufAttribute
    private Address address;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the age
     */
    public Integer getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + ", address=" + address + "]";
    }
}
