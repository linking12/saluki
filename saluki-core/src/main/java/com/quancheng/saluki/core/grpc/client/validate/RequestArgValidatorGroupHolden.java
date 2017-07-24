package com.quancheng.saluki.core.grpc.client.validate;

import com.google.common.base.Optional;

import java.util.Set;

/**
 * Created by guoyubo on 2017/7/22.
 */
public class RequestArgValidatorGroupHolden {


  private static final ThreadLocal<Set<Class>> GROUP_HOLDEN = new ThreadLocal<>();


  public static void setHoldenGroups(final Set<Class> groups) {
    GROUP_HOLDEN.set(groups);
  }

  public static Optional<Set<Class>> getHoldenGroups() {
    return GROUP_HOLDEN.get() == null  ? Optional.absent() : Optional.fromNullable(GROUP_HOLDEN.get());
  }
}
