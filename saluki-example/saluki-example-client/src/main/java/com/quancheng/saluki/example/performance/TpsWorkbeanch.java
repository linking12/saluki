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
package com.quancheng.saluki.example.performance;

import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.math.LongMath;

/**
 * @author liushiming
 * @version TpsWorkbeanch.java, v 0.0.1 2017年10月20日 下午9:17:25 liushiming
 */
public class TpsWorkbeanch {
  /** 线程数量 */
  public static final int N_THRESHOLDS = 100;

  /** 30 秒总时间 */
  public static final int TIME_THRESHOLDS = 30 * 60;

  /** 用原子变量来统计执行时间，便于作原子递减 */
  private static AtomicInteger totalTime = new AtomicInteger(TIME_THRESHOLDS);

  /** 用于统计执行的事物总数，用原子方式累加记录 */
  private static AtomicLong totalExecCount = new AtomicLong(0L);

  /** 需要到等到所有线程都在同一起跑线，才开始统计计数，类似于发令枪 */
  private static CyclicBarrier barrier = new CyclicBarrier(N_THRESHOLDS);

  /** 执行时间到达时，所有的线程需要依次退出，主线程才开始统计执行事物总数 */
  private static CountDownLatch countDownLatch = new CountDownLatch(N_THRESHOLDS);

  /** 线程执行标记 , 用volatile修饰，使变量修改具有线程可见性 */
  private static volatile boolean runnning = true;

  /** 用线程池来执行统计 */
  private static ExecutorService executorService;

  /**
   * 用接口来作模拟统计
   */
  interface Job {
    void execute() throws Exception;
  }

  /**
   * 具体Job，模拟完成一次Http请求 BTW:内部类用static修饰
   */
  static class JobDetail implements Job {

    public void execute() throws Exception {
      String run = OkHttpUtil.run("http://localhost:8081/proxy/hello?name=liushiming");
    }
  }

  /**
   * Worker执行Job
   */
  static class Worker implements Runnable {

    private Job job;

    Worker(Job job) {
      this.job = job;
    }

    // 每个线程执行的事物统计量
    int innerCount = 0;

    public void run() {
      try {
        barrier.await(); // 等到所有线程都在起跑线
        while (runnning) {
          this.job.execute();
          innerCount++;
        }
      } catch (Exception e) {
        System.out.println("线程Id：" + Thread.currentThread().getId() + " " + e.getMessage());
      } finally {
        // 累加到总记录统计量
        System.out.println("线程Id：" + Thread.currentThread().getId() + " 执行事物次数为：" + innerCount);
        totalExecCount.getAndAdd(innerCount);
        // 线程结束后，依次计数, 便于主线程继续执行
        countDownLatch.countDown();
      }
    }
  }

  public static void run() throws Exception {
    Job job = new JobDetail(); // 创建Job
    executorService = Executors.newFixedThreadPool(N_THRESHOLDS); // 新建固定大小线程的池子
    for (int i = 0; i < N_THRESHOLDS; i++) {
      executorService.submit(new Worker(job)); // 提交线程到池子中
    }
    // 还需要一个线程，用于周期检查执行时间是否到达
    final ScheduledExecutorService scheduledExcutor = Executors.newSingleThreadScheduledExecutor();
    scheduledExcutor.scheduleAtFixedRate(new Runnable() {
      public void run() {
        if (totalTime.decrementAndGet() == 0) { // 执行时间递减到0
          runnning = false; // 告诉线程，时间到了，所有线程不再执行
          scheduledExcutor.shutdownNow();
        }
      }
    }, 1L, 1L, TimeUnit.SECONDS);

    // 主线程等到所有的线程都退出，则开始统计
    countDownLatch.await();

    long totalExeCount = totalExecCount.get();
    System.out.println(N_THRESHOLDS + " 个线程，" + TIME_THRESHOLDS + " 秒内总共执行的事物数量：" + totalExeCount);

    long tps = LongMath.divide(totalExeCount, TIME_THRESHOLDS, RoundingMode.HALF_EVEN);

    System.out.println("OKHttp执行的TPS: " + tps);

    executorService.shutdownNow(); // 关闭线程池


  }

  public static void main(String[] args) throws Exception {
    run();
  }

}
