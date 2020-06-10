/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport.interfaces;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.modules.debug.interfaces.DeveloperSettings;
import java.io.File;

/**
 * 用于访问和开发功能交互的接口。在dev模式下，使用实现 {@link DevSupportManagerImpl}。在生产模式下，使用虚拟实现
 * {@link DisabledDevSupportManager}。
 */
public interface DevSupportManager extends NativeModuleCallExceptionHandler {

  void showNewJavaError(String message, Throwable e);

  void addCustomDevOption(String optionName, DevOptionHandler optionHandler);

  void showNewJSError(String message, ReadableArray details, int errorCookie);

  void updateJSError(final String message, final ReadableArray details, final int errorCookie);

  void hideRedboxDialog();

  //显示开发选项对话框
  void showDevOptionsDialog();

  void setDevSupportEnabled(boolean isDevSupportEnabled);

  void startInspector();

  void stopInspector();

  boolean getDevSupportEnabled();

  DeveloperSettings getDevSettings();

  void onNewReactContextCreated(ReactContext reactContext);

  void onReactInstanceDestroyed(ReactContext reactContext);

  String getSourceMapUrl();

  String getSourceUrl();

  String getJSBundleURLForRemoteDebugging();

  String getDownloadedJSBundleFile();

  boolean hasUpToDateJSBundleInCache();

  void reloadSettings();

  //重新加载JS
  void handleReloadJS();

  void reloadJSFromServer(final String bundleURL);

  //Packager Server是否正在运行
  void isPackagerRunning(PackagerStatusCallback callback);

  void setHotModuleReplacementEnabled(final boolean isHotModuleReplacementEnabled);

  void setRemoteJSDebugEnabled(final boolean isRemoteJSDebugEnabled);

  void setReloadOnJSChangeEnabled(final boolean isReloadOnJSChangeEnabled);

  void setFpsDebugEnabled(final boolean isFpsDebugEnabled);

  void toggleElementInspector();

  @Nullable
  File downloadBundleResourceFromUrlSync(final String resourceURL, final File outputFile);

  @Nullable
  String getLastErrorTitle();

  @Nullable
  StackFrame[] getLastErrorStack();

  void registerErrorCustomizer(ErrorCustomizer errorCustomizer);
}
