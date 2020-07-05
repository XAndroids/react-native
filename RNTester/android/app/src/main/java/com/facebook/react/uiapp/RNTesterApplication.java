/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.uiapp;

import android.app.Application;
import com.facebook.react.BuildConfig;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.react.views.text.ReactFontManager;
import com.facebook.soloader.SoLoader;
import java.util.Arrays;
import java.util.List;

public class RNTesterApplication extends Application implements ReactApplication {
  private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public String getJSMainModuleName() {
          //用于请求Packager Server加载的Main Module名称
          return "RNTester/js/RNTesterApp.android";
        }

        @Override
        public String getBundleAssetName() {
          //内置bundle的加载名称
          return "RNTesterApp.android.bundle";
        }

        @Override
        public boolean getUseDeveloperSupport() {
          //允许打开Dev菜单，和Reload JS等
          return BuildConfig.DEBUG;
        }

        @Override
        public List<ReactPackage> getPackages() {
          //提供自定义和Main Packager，用于后续注册
          return Arrays.<ReactPackage>asList(new MainReactPackage());
        }
      };

  @Override
  public void onCreate() {
    //添加自定义的iconfont字体
    ReactFontManager.getInstance().addCustomFont(this, "Rubik", R.font.rubik);
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
  }

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }
};
