package com.quancheng.saluki.serializer.pojo;

public enum PhoneType {

                       MOBILE(0),
                       /**
                        * <code>HOME = 1;</code>
                        */
                       HOME(1),
                       /**
                        * <code>WORK = 2;</code>
                        */
                       WORK(2);

    private final int value;

    private PhoneType(int value){
        this.value = value;
    }

    public final int getNumber() {
        return value;
    }

    public static PhoneType forNumber(Integer value) {
        switch (value) {
            case 0:
                return MOBILE;
            case 1:
                return HOME;
            case 2:
                return WORK;
            default:
                return null;
        }
    }

}
