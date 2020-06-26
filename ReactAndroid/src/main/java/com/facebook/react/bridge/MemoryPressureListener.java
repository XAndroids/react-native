// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react.bridge;

/** 用于内存压力事件的侦听器接口。*/
public interface MemoryPressureListener {

  /** 当系统生成内存警告时调用。*/
  void handleMemoryPressure(int level);
}
