/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

/** React Native应用基础Activity */
public abstract class ReactActivity extends AppCompatActivity
    implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {

  private final ReactActivityDelegate mDelegate;

  protected ReactActivity() {
    mDelegate = createReactActivityDelegate();
  }

  /**
   * 返回JavaScript注册的主组件的名称。用于安排组件的渲染。如"MoviesApp"
   */
  protected @Nullable String getMainComponentName() {
    return null;
  }

  /**在构造时调用，如果有自定义代理实现则重写*/
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegate(this, getMainComponentName());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDelegate.onCreate(savedInstanceState);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mDelegate.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mDelegate.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDelegate.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mDelegate.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return mDelegate.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return mDelegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    return mDelegate.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event);
  }

  @Override
  public void onBackPressed() {
    //监听Andriod Back按键的点击
    if (!mDelegate.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  public void invokeDefaultOnBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (!mDelegate.onNewIntent(intent)) {
      super.onNewIntent(intent);
    }
  }

  @Override
  public void requestPermissions(
      String[] permissions, int requestCode, PermissionListener listener) {
    mDelegate.requestPermissions(permissions, requestCode, listener);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    mDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    mDelegate.onWindowFocusChanged(hasFocus);
  }

  protected final ReactNativeHost getReactNativeHost() {
    return mDelegate.getReactNativeHost();
  }

  protected final ReactInstanceManager getReactInstanceManager() {
    return mDelegate.getReactInstanceManager();
  }

  protected final void loadApp(String appKey) {
    mDelegate.loadApp(appKey);
  }
}
