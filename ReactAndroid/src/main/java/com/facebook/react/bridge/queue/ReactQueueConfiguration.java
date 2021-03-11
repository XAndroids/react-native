/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge.queue;

/**
 * 指定了必须使用哪个{@link messagequeuthread}来运行catalyst内部的各种执行context(主UI线程，Native Module和
 * JS)。其中一些队列*可能*相同，但应该按照它们不同的方式进行编码。
 *
 * UI Queue 线程：标准的Android主UI线程和Looper，不可以配置；
 * Native Module线程：本机模块被调用的线程和循环器；
 * JS Queue 线程：JS在这个线程上执行；
 */
public interface ReactQueueConfiguration {
  MessageQueueThread getUIQueueThread();

  MessageQueueThread getNativeModulesQueueThread();

  MessageQueueThread getJSQueueThread();

  void destroy();
}
