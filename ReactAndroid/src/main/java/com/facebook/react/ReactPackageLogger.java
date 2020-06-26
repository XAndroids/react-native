// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react;

/** 接口，用于桥接器调用TTI开始和结束标记。*/
public interface ReactPackageLogger {

  void startProcessPackage();

  void endProcessPackage();
}
