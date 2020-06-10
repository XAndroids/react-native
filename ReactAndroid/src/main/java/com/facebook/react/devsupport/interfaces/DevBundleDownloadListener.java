/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport.interfaces;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.NativeDeltaClient;

/**
 * Dev Bundle下载监听器
 */
public interface DevBundleDownloadListener {
  //下载成功
  void onSuccess(@Nullable NativeDeltaClient nativeDeltaClient);

  //下载中
  void onProgress(@Nullable String status, @Nullable Integer done, @Nullable Integer total);

  //下载失败
  void onFailure(Exception cause);
}
