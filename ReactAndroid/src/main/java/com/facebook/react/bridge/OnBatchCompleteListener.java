/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

/** 当一批JS-Java调用结束的时候，警备通知的一个module接口。 */
public interface OnBatchCompleteListener {

  void onBatchComplete();
}
