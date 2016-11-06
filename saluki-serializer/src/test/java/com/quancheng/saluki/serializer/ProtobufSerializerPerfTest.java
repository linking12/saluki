package com.quancheng.saluki.serializer;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.quancheng.saluki.serializer.pojo.Address;

public class ProtobufSerializerPerfTest {

    private static final long               TOTAL_ITERATIONS          = 1_000_000;
    private static final long               MAX_EXPECTED_ELAPSED_TIME = 10000;

    private static final ProtobufSerializer SERIALIZER                = new ProtobufSerializer();

    /**
     * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#toProtobuf(java.lang.Object)}.
     */
    @Test
    public void perfTestToProtobuf() {
        try {
            final long startTime = System.nanoTime();
            for (int i = 0; i < TOTAL_ITERATIONS; i++) {
                // 1. Setup the a Pojo Address
                final Address address = new Address();
                address.setStreet("1 Main St");
                address.setCity("Foo Ville");
                address.setStateOrProvince("Bar");
                address.setPostalCode("J0J 1J1");
                address.setCountry("Canada");

                final com.quancheng.saluki.serializer.proto.Message.Address protoBufAddress = (com.quancheng.saluki.serializer.proto.Message.Address) SERIALIZER.toProtobuf(address);
            }
            final long endTime = System.nanoTime();
            final long elapsedTime = endTime - startTime;
            final long elapsedTimeMS = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);

            System.out.println("Performance Test of toProtobuf took " + elapsedTimeMS + "ms to serialize "
                               + TOTAL_ITERATIONS + " simple objects");

            Assert.assertTrue("The performance test too much too long", elapsedTimeMS < MAX_EXPECTED_ELAPSED_TIME * 2);
        } catch (Exception e) {
            fail("Can not serialize Address to protoBuf: " + e);
        }
    }

    /**
     * Test method for {@link ca.jeb.protobuf.ProtobufSerializer#fromProtobuf(com.google.protobuf.GeneratedMessage)}.
     */
    @Test
    public void testFromProtobuf() {
        try {
            final long startTime = System.nanoTime();
            for (int i = 0; i < TOTAL_ITERATIONS; i++) {
                final com.quancheng.saluki.serializer.proto.Message.Address protobufAddress = com.quancheng.saluki.serializer.proto.Message.Address.newBuilder().setStreet("1 Main St").setCity("Foo Ville").setStateOrProvince("Bar").setPostalCode("J0J 1J1").setCountry("Canada").build();

                final com.quancheng.saluki.serializer.pojo.Address address = (com.quancheng.saluki.serializer.pojo.Address) SERIALIZER.fromProtobuf(protobufAddress,
                                                                                                                                                    com.quancheng.saluki.serializer.pojo.Address.class);
            }
            final long endTime = System.nanoTime();
            final long elapsedTime = endTime - startTime;
            final long elapsedTimeMS = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);

            System.out.println("Performance Test of fromProtobuf took " + elapsedTimeMS + "ms to deserialize "
                               + TOTAL_ITERATIONS + " to simple objects");

            Assert.assertTrue("The performance test too much too long", elapsedTimeMS < MAX_EXPECTED_ELAPSED_TIME * 2);
        } catch (Exception e) {
            fail("Can not serialize Address to protoBuf: " + e);
        }
    }

}
