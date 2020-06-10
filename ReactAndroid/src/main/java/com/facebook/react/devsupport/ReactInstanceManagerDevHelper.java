/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport;

import android.app.Activity;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.JavaJSExecutor;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.NativeDeltaClient;

/**
 * 由{@link DevSupportManager}用于访问 {@link ReactInstanceManager}的一些字段和方法的接口。用于显示和处理开
 * 发人员菜单选项。
 */
public interface ReactInstanceManagerDevHelper {

  /** 在启用JS调试的情况下，请求react instance重新创建。*/
  void onReloadWithJSDebugger(JavaJSExecutor.Factory proxyExecutorFactory);

  /** 通知react instance manager 从服务器下载的新JS bundle版本。*/
  void onJSBundleLoadedFromServer(@Nullable NativeDeltaClient nativeDeltaClient);

  /** Request to toggle the react element inspector. */
  void toggleElementInspector();

  /** Get reference to top level #{link Activity} attached to react context */
  @Nullable
  Activity getCurrentActivity();

  JavaScriptExecutorFactory getJavaScriptExecutorFactory();
}
