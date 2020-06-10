/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport.interfaces;

/**
 * Package Server状态回调：是否正在打包
 */
public interface PackagerStatusCallback {
  void onPackagerStatusFetched(boolean packagerIsRunning);
}
