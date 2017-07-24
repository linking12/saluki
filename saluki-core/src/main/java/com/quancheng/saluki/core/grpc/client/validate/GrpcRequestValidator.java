package com.quancheng.saluki.core.grpc.client.validate;

import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.exception.RpcValidatorException;
import com.quancheng.saluki.serializer.ProtobufValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by guoyubo on 2017/7/21.
 */
public class GrpcRequestValidator {

  private Validator validator;

  private GrpcRequestValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private static class GrpcRequestValidatorHolder {

    private static final GrpcRequestValidator INSTANCE = new GrpcRequestValidator();
  }

 public static final GrpcRequestValidator getInstance() {
    return GrpcRequestValidatorHolder.INSTANCE;
 }

  /**
   * use java validation to validate request arg
   * @param request
   */
  public void doValidate(final GrpcRequest request) {
    if (!request.getMethodRequest().getArg().getClass().isAnnotationPresent(ProtobufValidator.class)) {
      return;
    }
    List<Class> validatorGroups = new ArrayList<>();
    if (GrpcRequestValidatorGroupHolden.getHoldenGroups().isPresent()) {
      validatorGroups = GrpcRequestValidatorGroupHolden.getHoldenGroups().get();
    }
    Set<ConstraintViolation<Object>> validate = validator.validate(request.getMethodRequest().getArg(),
        (Class[]) validatorGroups.toArray(new Class[0]));
    StringBuffer validateMsg = new StringBuffer();
    for (ConstraintViolation<Object> constraintViolation : validate) {
      validateMsg.append(String.format("parameter[%s] message[%s] ", constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
    }

    if (validateMsg.length() > 0) {
      throw new RpcValidatorException(validateMsg.toString());
    }
  }

}
