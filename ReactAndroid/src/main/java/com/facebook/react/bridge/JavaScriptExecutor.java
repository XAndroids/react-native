/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;

@DoNotStrip
public abstract class JavaScriptExecutor {
  private final HybridData mHybridData;

  protected JavaScriptExecutor(HybridData hybridData) {
    mHybridData = hybridData;
  }

  /**
   * 关闭此executor并清除它所使用的任何资源。在此之后，预计不会再有进一步的调用。
   * TODO mhorowitz:这个可能不再使用;检查并删除，如果可能的话。
   */
  public void close() {
    mHybridData.resetNative();
  }

  /** 返回executor的名称，标识底层运行时。*/
  public abstract String getName();
}
