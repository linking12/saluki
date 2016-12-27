package com.quancheng.saluki.example.server;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.test.model.user.UserCreateRequest;
import com.quancheng.test.model.user.UserCreateResponse;
import com.quancheng.test.model.user.UserDeleteRequest;
import com.quancheng.test.model.user.UserGetRequest;
import com.quancheng.test.model.user.UserGetResponse;
import com.quancheng.test.model.user.UserSetRequest;
import com.quancheng.test.service.UserService;
import com.quancheng.zeus.model.basemodel.BaseResponse;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public UserCreateResponse create(UserCreateRequest arg0) {
        UserCreateResponse response = new UserCreateResponse();
        response.setId(12L);
        return response;
    }

    @Override
    public BaseResponse delete(UserDeleteRequest arg0) {
        BaseResponse response = new BaseResponse();
        response.setMessage("123123");
        return response;
    }

    @Override
    public UserGetResponse get(UserGetRequest arg0) {
        UserGetResponse response = new UserGetResponse();
        response.setName("asdf");
        return response;
    }

    @Override
    public BaseResponse set(UserSetRequest arg0) {
        BaseResponse response = new BaseResponse();
        response.setMessage("123123");
        return response;
    }

}
