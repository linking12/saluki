/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.core.grpc.client.validate;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.annotation.ArgValidator;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.exception.RpcValidatorException;
import com.quancheng.saluki.core.utils.CollectionUtils;

/**
 * @author liushiming
 * @version RequestValidator.java, v 0.0.1 2017年7月24日 下午6:45:05 liushiming
 * @since JDK 1.8
 */
public class RequestValidator {
  private Validator validator;

  private static final Object LOCK = new Object();

  private static RequestValidator requestValidator;

  private RequestValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  public static RequestValidator newRequestValidator() {
    synchronized (LOCK) {
      if (requestValidator != null) {
        return requestValidator;
      } else {
        return new RequestValidator();
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public void doValidate(final GrpcRequest request) throws ClassNotFoundException {
    if (!request.getRequestParam().getClass().isAnnotationPresent(ArgValidator.class)) {
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
    Optional<Set<Class>> optional = RpcContext.getContext().getHoldenGroups();
    if (optional.isPresent()) {
      validatorGroups = optional.get();
    }
    Set<ConstraintViolation<Object>> violations = validator.validate(request.getRequestParam(),
        (Class[]) validatorGroups.toArray(new Class[0]));
    if (CollectionUtils.isNotEmpty(violations)) {
      StringBuffer validateMsg = new StringBuffer();
      for (ConstraintViolation<Object> constraintViolation : violations) {
        validateMsg.append(String.format("parameter[%s] message[%s] ",
            constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
      }

      if (validateMsg.length() > 0) {
        throw new RpcValidatorException(validateMsg.toString());
      }
    }
  }
}

