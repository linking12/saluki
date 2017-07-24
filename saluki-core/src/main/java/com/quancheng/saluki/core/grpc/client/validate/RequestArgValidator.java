package com.quancheng.saluki.core.grpc.client.validate;

import org.apache.commons.lang.StringUtils;

import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.exception.RpcValidatorException;
import com.quancheng.saluki.core.utils.CollectionUtils;
import com.quancheng.saluki.serializer.ProtobufValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by guoyubo on 2017/7/21.
 */
public class RequestArgValidator {

  private Validator validator;

  private RequestArgValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private static class RequestArgsValidatorHolder {

    private static final RequestArgValidator INSTANCE = new RequestArgValidator();
  }

 public static final RequestArgValidator getInstance() {
    return RequestArgsValidatorHolder.INSTANCE;
 }

  /**
   * use java validation to validate request arg
   * @param request
   */
  public void doValidate(final GrpcRequest request) throws ClassNotFoundException {
    if (!request.getMethodRequest().getArg().getClass().isAnnotationPresent(ProtobufValidator.class)) {
      return;
    }
    Set<Class> validatorGroups = new HashSet<>();
    String validatorGroupStr = request.getRefUrl().getParameter(Constants.VALIDATOR_GROUPS);
    if (StringUtils.isNotEmpty(validatorGroupStr)) {
      String[] splitGroups = validatorGroupStr.split(";");
      for (String splitGroup : splitGroups) {
        validatorGroups.add(Class.forName(splitGroup));
      }
    }

    if (RequestArgValidatorGroupHolden.getHoldenGroups().isPresent()) {
      validatorGroups = RequestArgValidatorGroupHolden.getHoldenGroups().get();
    }
    Set<ConstraintViolation<Object>> violations = validator.validate(request.getMethodRequest().getArg(),
        (Class[]) validatorGroups.toArray(new Class[0]));
    if (CollectionUtils.isNotEmpty(violations)) {
      StringBuffer validateMsg = new StringBuffer();
      for (ConstraintViolation<Object> constraintViolation : violations) {
        validateMsg.append(String.format("parameter[%s] message[%s] ", constraintViolation.getPropertyPath(),
            constraintViolation.getMessage()));
      }

      if (validateMsg.length() > 0) {
        throw new RpcValidatorException(validateMsg.toString());
      }
    }
  }

}
