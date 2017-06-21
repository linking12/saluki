package com.quancheng.saluki.serializer.proto.message;


import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;


@ProtobufEntity(com.quancheng.saluki.serializer.proto.Message.Address.class)
public class Address{


    @ProtobufAttribute
    private String street;


    public String getStreet(){
        return this.street;
}


    public void setStreet(String street){
        this.street=street;
}




    @ProtobufAttribute
    private String city;


    public String getCity(){
        return this.city;
}


    public void setCity(String city){
        this.city=city;
}




    @ProtobufAttribute
    private String stateOrProvince;


    public String getStateOrProvince(){
        return this.stateOrProvince;
}


    public void setStateOrProvince(String stateOrProvince){
        this.stateOrProvince=stateOrProvince;
}




    @ProtobufAttribute
    private String country;


    public String getCountry(){
        return this.country;
}


    public void setCountry(String country){
        this.country=country;
}




    @ProtobufAttribute
    private String postalCode;


    public String getPostalCode(){
        return this.postalCode;
}


    public void setPostalCode(String postalCode){
        this.postalCode=postalCode;
}




    @ProtobufAttribute
    private Boolean isCanada;


    public Boolean getIsCanada(){
        return this.isCanada;
}


    public void setIsCanada(Boolean isCanada){
        this.isCanada=isCanada;
}




    @ProtobufAttribute
    private java.util.Map<String,String> mapTest;


    public java.util.Map<String,String> getMapTest(){
        return this.mapTest;
}


    public void setMapTest(java.util.Map<String,String> mapTest){
        this.mapTest=mapTest;
}




    @ProtobufAttribute
    private com.quancheng.saluki.serializer.proto.message.PhoneType phoneType;


    public com.quancheng.saluki.serializer.proto.message.PhoneType getPhoneType(){
        return this.phoneType;
}


    public void setPhoneType(com.quancheng.saluki.serializer.proto.message.PhoneType phoneType){
        this.phoneType=phoneType;
}


}
