package com.quancheng.saluki.serializer;

import java.util.Arrays;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.serializer.IProtobufSerializer;
import com.quancheng.saluki.serializer.ProtobufSerializer;
import com.quancheng.saluki.serializer.exception.ProtobufException;
import com.quancheng.zeus.model.account.AccountInfo;
import com.quancheng.zeus.model.account.GetAccountsListResponse;

public class TestMain {

    public static void main(String[] args) throws ProtobufException {
        GetAccountsListResponse response = new GetAccountsListResponse();
        AccountInfo ai = new AccountInfo();
        ai.setAccountId(283208L);
        response.setAccounts(Arrays.asList(ai, ai));
        ProtobufSerializer s = new ProtobufSerializer();
        Message json = s.toProtobuf(response);
        com.quancheng.zeus.model.basemodel.PageResponse base = new com.quancheng.zeus.model.basemodel.PageResponse();
        GetAccountsListResponse res_ = (GetAccountsListResponse) s.fromProtobuf(json, GetAccountsListResponse.class);

        System.out.println(res_);
    }

}
