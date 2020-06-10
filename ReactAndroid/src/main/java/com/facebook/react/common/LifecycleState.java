/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.common;

/**
 * Activity的生命周期状态。pause之后和resume之前的状态基本是一样的，所以这个枚举是指向生命周期进程(onResume, etc)。
 * 最终，如果有必要，它会包含一下内容：
 * <p>BEFORE_CREATE, CREATED, VIEW_CREATED, STARTED, RESUMED
 */
public enum LifecycleState {
  BEFORE_CREATE,
  BEFORE_RESUME,
  RESUMED,
}
