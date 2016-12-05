package com.quancheng.saluki.example;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestMain {

    public static void main(String[] args) {
        InputStream in = TestMain.class.getClassLoader().getResourceAsStream("SalukiServiceDefinition.json");
        List<Map<String, String>> obj = new Gson().fromJson(new InputStreamReader(in),
                                                            new TypeToken<List<Map<String, String>>>() {
                                                            }.getType());
        System.out.println(obj);
    }

}
