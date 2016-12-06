package com.quancheng.saluki.serializer;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.saluki.serializer.pojo.PhoneType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProtobufSerializerToProtobufTest {

    private com.quancheng.saluki.serializer.pojo.Address          address;
    private com.quancheng.saluki.serializer.pojo.Person           person;

    private com.quancheng.saluki.serializer.proto.Message.Address protobufAddress;
    private com.quancheng.saluki.serializer.proto.Message.Person  protobufPerson;

    private static final ProtobufSerializer                       SERIALIZER = new ProtobufSerializer();

    @Before
    public void setupObjects() {
        // Setup Pojo Address
        address = new com.quancheng.saluki.serializer.pojo.Address();
        address.setStreet("1 Main St");
        address.setCity("Foo Ville");
        address.setStateOrProvince("Bar");
        address.setPostalCode("J0J 1J1");
        address.setCountry("Canada");
        address.setIsCanada("true");
        Map<String, String> mapTest = new HashMap<String, String>();
        mapTest.put("123", "123");
        address.setMapTest(mapTest);
        address.setPhoneType(PhoneType.HOME);
        // Setup POJO Person
        person = new com.quancheng.saluki.serializer.pojo.Person();
        person.setName("Erick");
        person.setAge(22);
        person.setAddress(address);

        // Setup Address Protobuf
        protobufAddress = com.quancheng.saluki.serializer.proto.Message.Address.newBuilder().setStreet("1 Main St").setCity("Foo Ville").setStateOrProvince("Bar").setPostalCode("J0J 1J1").setCountry("Canada").setIsCanada(true).build();
        // Setup Person Protobuf
        protobufPerson = com.quancheng.saluki.serializer.proto.Message.Person.newBuilder().setName("Erick").setAge(22).setAddress(protobufAddress).build();
    }

    @Test
    public void test1Address() throws ProtobufException {
        final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress = (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
        Assert.assertEquals("Attribute street not equal", address.getStreet(), protobufAddress.getStreet());
        Map<String, String> test = protobufAddress.getMapTestMap();
        System.out.println(test);
    }

    // @Test
    // public void test2Address() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress =
    // (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
    // Assert.assertEquals("Attribute city not equal", address.getCity(), protobufAddress.getCity());
    // }
    //
    // @Test
    // public void test3Address() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress =
    // (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
    // Assert.assertEquals("Attribute stateOrProvince not equal", address.getStateOrProvince(),
    // protobufAddress.getStateOrProvince());
    // }
    //
    // @Test
    // public void test4Address() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress =
    // (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
    // Assert.assertEquals("Attribute postalCode not equal", address.getPostalCode(), protobufAddress.getPostalCode());
    // }
    //
    // @Test
    // public void test5Address() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress =
    // (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
    // Assert.assertEquals("Attribute country not equal", address.getCountry(), protobufAddress.getCountry());
    // }
    //
    // @Test
    // public void test6Address() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress =
    // (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
    // Assert.assertTrue("Attribute isCanada not true", protobufAddress.getIsCanada());
    // }
    //
    // @Test
    // public void test7Person() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Person protobufPerson =
    // (com.quancheng.saluki.serializer.proto.Message.Person) SERIALIZER.toProtobuf(person);
    // Assert.assertEquals("Attribute age not equal", (int) person.getAge(), protobufPerson.getAge());
    // }
    //
    // @Test
    // public void test8Person() throws ProtobufException {
    // final com.quancheng.saluki.serializer.proto.Message.Person protobufPerson =
    // (com.quancheng.saluki.serializer.proto.Message.Person) SERIALIZER.toProtobuf(person);
    // Assert.assertEquals("Attribute age not equal", person.getName(), protobufPerson.getName());
    // }
}
