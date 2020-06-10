// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react.bridge;

import androidx.annotation.Nullable;
import com.facebook.proguard.annotations.DoNotStrip;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 静态类，允许将标识放置在Reat代码中，并可以以配置的方式对其进行响应
 */
@DoNotStrip
public class ReactMarker {

  public interface MarkerListener {
    void logMarker(ReactMarkerConstants name, @Nullable String tag, int instanceKey);
  };

  //这是为了冗长， 未来我们可以启用旧的logMarker API
  public interface FabricMarkerListener {
    void logFabricMarker(
        ReactMarkerConstants name, @Nullable String tag, int instanceKey, long timestamp);
  };

  //这里使用list替代set，是因为我们希望listener的数量非常小，并且我们希望listner按照确定顺序调用。
  private static final List<MarkerListener> sListeners = new CopyOnWriteArrayList<>();

  // Use a list instead of a set here because we expect the number of listeners
  // to be very small, and we want listeners to be called in a deterministic
  // order. For Fabric-specific events.
  private static final List<FabricMarkerListener> sFabricMarkerListeners =
      new CopyOnWriteArrayList<>();

  @DoNotStrip
  public static void addListener(MarkerListener listener) {
    if (!sListeners.contains(listener)) {
      sListeners.add(listener);
    }
  }

  @DoNotStrip
  public static void removeListener(MarkerListener listener) {
    sListeners.remove(listener);
  }

  @DoNotStrip
  public static void clearMarkerListeners() {
    sListeners.clear();
  }

  // Specific to Fabric marker listeners
  @DoNotStrip
  public static void addFabricListener(FabricMarkerListener listener) {
    if (!sFabricMarkerListeners.contains(listener)) {
      sFabricMarkerListeners.add(listener);
    }
  }

  // Specific to Fabric marker listeners
  @DoNotStrip
  public static void removeFabricListener(FabricMarkerListener listener) {
    sFabricMarkerListeners.remove(listener);
  }

  // Specific to Fabric marker listeners
  @DoNotStrip
  public static void clearFabricMarkerListeners() {
    sFabricMarkerListeners.clear();
  }

  // Specific to Fabric marker listeners
  @DoNotStrip
  public static void logFabricMarker(
      ReactMarkerConstants name, @Nullable String tag, int instanceKey, long timestamp) {
    for (FabricMarkerListener listener : sFabricMarkerListeners) {
      listener.logFabricMarker(name, tag, instanceKey, timestamp);
    }
  }

  // Specific to Fabric marker listeners
  @DoNotStrip
  public static void logFabricMarker(
      ReactMarkerConstants name, @Nullable String tag, int instanceKey) {
    logFabricMarker(name, tag, instanceKey, -1);
  }

  @DoNotStrip
  public static void logMarker(String name) {
    logMarker(name, null);
  }

  @DoNotStrip
  public static void logMarker(String name, int instanceKey) {
    logMarker(name, null, instanceKey);
  }

  @DoNotStrip
  public static void logMarker(String name, @Nullable String tag) {
    logMarker(name, tag, 0);
  }

  @DoNotStrip
  public static void logMarker(String name, @Nullable String tag, int instanceKey) {
    ReactMarkerConstants marker = ReactMarkerConstants.valueOf(name);
    logMarker(marker, tag, instanceKey);
  }

  @DoNotStrip
  public static void logMarker(ReactMarkerConstants name) {
    logMarker(name, null, 0);
  }

  @DoNotStrip
  public static void logMarker(ReactMarkerConstants name, int instanceKey) {
    logMarker(name, null, instanceKey);
  }

  @DoNotStrip
  public static void logMarker(ReactMarkerConstants name, @Nullable String tag) {
    logMarker(name, tag, 0);
  }

  @DoNotStrip
  public static void logMarker(ReactMarkerConstants name, @Nullable String tag, int instanceKey) {
    logFabricMarker(name, tag, instanceKey);
    for (MarkerListener listener : sListeners) {
      listener.logMarker(name, tag, instanceKey);
    }
  }
}
