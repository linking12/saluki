package com.quancheng.saluki.serializer.proto.message;


import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;


@ProtobufEntity(com.quancheng.saluki.serializer.proto.Message.Person.class)
public class Person {


  @ProtobufAttribute
  private String name;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @ProtobufAttribute
  private Integer age;


  public Integer getAge() {
    return this.age;
  }


  public void setAge(Integer age) {
    this.age = age;
  }



  @ProtobufAttribute
  private com.quancheng.saluki.serializer.proto.message.Address address;


  public com.quancheng.saluki.serializer.proto.message.Address getAddress() {
    return this.address;
  }


  public void setAddress(com.quancheng.saluki.serializer.proto.message.Address address) {
    this.address = address;
  }



  @ProtobufAttribute
  private java.util.Map<String, com.quancheng.saluki.serializer.proto.message.Address> mapObject;


  public java.util.Map<String, com.quancheng.saluki.serializer.proto.message.Address> getMapObject() {
    return this.mapObject;
  }


  public void setMapObject(
      java.util.Map<String, com.quancheng.saluki.serializer.proto.message.Address> mapObject) {
    this.mapObject = mapObject;
  }


}
