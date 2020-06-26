/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import static com.facebook.systrace.Systrace.TRACE_TAG_REACT_JAVA_BRIDGE;

import android.os.SystemClock;
import com.facebook.soloader.SoLoader;
import com.facebook.systrace.Systrace;

public class ReactBridge {
  //Load开始时间
  private static volatile long sLoadStartTime = 0;
  //Load结束事件
  private static volatile long sLoadEndTime = 0;

  //是否加载过
  private static boolean sDidInit = false;

  public static synchronized void staticInit() {
    if (sDidInit) {
      return;
    }
    sDidInit = true;

    sLoadStartTime = SystemClock.uptimeMillis();
    Systrace.beginSection(
        TRACE_TAG_REACT_JAVA_BRIDGE, "ReactBridge.staticInit::load:reactnativejni");
    ReactMarker.logMarker(ReactMarkerConstants.LOAD_REACT_NATIVE_SO_FILE_START);

    //加载reactnativejni.so库
    SoLoader.loadLibrary("reactnativejni");

    ReactMarker.logMarker(ReactMarkerConstants.LOAD_REACT_NATIVE_SO_FILE_END);
    Systrace.endSection(TRACE_TAG_REACT_JAVA_BRIDGE);
    sLoadEndTime = SystemClock.uptimeMillis();
  }

  public static long getLoadStartTime() {
    return sLoadStartTime;
  }

  public static long getLoadEndTime() {
    return sLoadEndTime;
  }
}
