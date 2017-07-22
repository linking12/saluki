package com.quancheng.saluki.core.grpc.client.validate;

import com.google.common.base.Optional;

import java.util.List;

/**
 * Created by guoyubo on 2017/7/22.
 */
public class GrpcRequestValidatorGroupHolden {


  private static final ThreadLocal<List<Class>> GROUP_HOLDEN = new ThreadLocal<>();


  public static void setHoldenGroups(final List<Class> groups) {
    GROUP_HOLDEN.set(groups);
  }

  public static Optional<List<Class>> getHoldenGroups() {
    return GROUP_HOLDEN.get() == null  ? Optional.absent() : Optional.fromNullable(GROUP_HOLDEN.get());
  }
}
