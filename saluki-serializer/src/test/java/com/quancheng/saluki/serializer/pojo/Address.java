
package com.quancheng.saluki.serializer.pojo;

import java.util.Map;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;
import com.quancheng.saluki.serializer.converter.StringBooleanConverter;

@ProtobufEntity(com.quancheng.saluki.serializer.proto.Message.Address.class)
public class Address {

    @ProtobufAttribute
    private String              street;

    @ProtobufAttribute
    private String              city;

    @ProtobufAttribute
    private String              stateOrProvince;

    @ProtobufAttribute
    private String              country;

    @ProtobufAttribute
    private String              postalCode;

    @ProtobufAttribute
    private Map<String, String> mapTest;

    @ProtobufAttribute(pojoGetter = "getIsCanada", pojoSetter = "setIsCanada", protobufSetter = "setIsCanada", protobufGetter = "getIsCanada", converter = StringBooleanConverter.class)
    private String              isCanadaBooleanAsStr;

    @ProtobufAttribute
    private PhoneType           phoneType;

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @param stateOrProvince the stateOrProvince to set
     */
    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @param postalCode the postalCode to set
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @param isCanadaBooleanAsStr the isCanadaBooleanAsStr to set
     */
    public void setIsCanada(String isCanadaStr) {
        this.isCanadaBooleanAsStr = isCanadaStr;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return the stateOrProvince
     */
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return the postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @return the isCanadaBooleanAsStr
     */
    public String getIsCanada() {
        return isCanadaBooleanAsStr;
    }

    public Map<String, String> getMapTest() {
        return mapTest;
    }

    public void setMapTest(Map<String, String> mapTest) {
        this.mapTest = mapTest;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Address [street=" + street + ", city=" + city + ", stateOrProvince=" + stateOrProvince + ", country="
               + country + ", postalCode=" + postalCode + ", isCanadaBooleanAsStr=" + isCanadaBooleanAsStr + "]";
    }
}
