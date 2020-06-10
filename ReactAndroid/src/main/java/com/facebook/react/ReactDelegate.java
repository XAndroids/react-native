/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.annotation.Nullable;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

/**
 *处理React应用支持的代理。这个代理不知道它是在一个Activity或者Fragment中使用
 */
public class ReactDelegate {

  private final Activity mActivity;
  private ReactRootView mReactRootView;

  @Nullable private final String mMainComponentName;

  @Nullable private Bundle mLaunchOptions;

  @Nullable private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer;

  private ReactNativeHost mReactNativeHost;

  public ReactDelegate(
      Activity activity,
      ReactNativeHost reactNativeHost,
      @Nullable String appKey,
      @Nullable Bundle launchOptions) {
    mActivity = activity;
    mMainComponentName = appKey;
    mLaunchOptions = launchOptions;
    mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();
    mReactNativeHost = reactNativeHost;
  }

  public void onHostResume() {
    if (getReactNativeHost().hasInstance()) {
      if (mActivity instanceof DefaultHardwareBackBtnHandler) {
        getReactNativeHost()
            .getReactInstanceManager()
            .onHostResume(mActivity, (DefaultHardwareBackBtnHandler) mActivity);
      } else {
        throw new ClassCastException(
            "Host Activity does not implement DefaultHardwareBackBtnHandler");
      }
    }
  }

  public void onHostPause() {
    if (getReactNativeHost().hasInstance()) {
      getReactNativeHost().getReactInstanceManager().onHostPause(mActivity);
    }
  }

  public void onHostDestroy() {
    if (mReactRootView != null) {
      mReactRootView.unmountReactApplication();
      mReactRootView = null;
    }
    if (getReactNativeHost().hasInstance()) {
      getReactNativeHost().getReactInstanceManager().onHostDestroy(mActivity);
    }
  }

  public boolean onBackPressed() {
    if (getReactNativeHost().hasInstance()) {
      getReactNativeHost().getReactInstanceManager().onBackPressed();
      return true;
    }
    return false;
  }

  public void onActivityResult(
      int requestCode, int resultCode, Intent data, boolean shouldForwardToReactInstance) {
    if (getReactNativeHost().hasInstance() && shouldForwardToReactInstance) {
      getReactNativeHost()
          .getReactInstanceManager()
          .onActivityResult(mActivity, requestCode, resultCode, data);
    }
  }

  public void loadApp() {
    loadApp(mMainComponentName);
  }

  public void loadApp(String appKey) {
    if (mReactRootView != null) {
      throw new IllegalStateException("Cannot loadApp while app is already running.");
    }
    mReactRootView = createRootView();
    mReactRootView.startReactApplication(
        getReactNativeHost().getReactInstanceManager(), appKey, mLaunchOptions);
  }

  public ReactRootView getReactRootView() {
    return mReactRootView;
  }

  protected ReactRootView createRootView() {
    return new ReactRootView(mActivity);
  }

  /**
   * 处理委派{@link Activity#onKeyUp(int, KeyEvent)}方法类决定应用程序是否应该显示developer菜单，还是应该重新
   * 加载React应用程序。
   *
   * @return 如果我们使用该事件并加载开发菜单或重新加载应用程序，则为true。
   */
  public boolean shouldShowDevMenuOrReload(int keyCode, KeyEvent event) {
    if (getReactNativeHost().hasInstance() && getReactNativeHost().getUseDeveloperSupport()) {
      //是开发者模式
      if (keyCode == KeyEvent.KEYCODE_MENU) {
        //打开开发选项对话框
        getReactNativeHost().getReactInstanceManager().showDevOptionsDialog();
        return true;
      }

      //如果双击"R"，重新加载JS
      boolean didDoubleTapR =
          Assertions.assertNotNull(mDoubleTapReloadRecognizer)
              .didDoubleTapR(keyCode, mActivity.getCurrentFocus());
      if (didDoubleTapR) {
        getReactNativeHost().getReactInstanceManager().getDevSupportManager().handleReloadJS();
        return true;
      }
    }
    return false;
  }

  /** Get the {@link ReactNativeHost} used by this app. */
  private ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  public ReactInstanceManager getReactInstanceManager() {
    return getReactNativeHost().getReactInstanceManager();
  }
}
