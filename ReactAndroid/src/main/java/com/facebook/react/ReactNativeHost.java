/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import android.app.Application;
import androidx.annotation.Nullable;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.JSIModulePackage;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.bridge.ReactMarkerConstants;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.devsupport.RedBoxHandler;
import com.facebook.react.uimanager.UIImplementationProvider;
import java.util.List;

/**
 * 一个简单的类，它持有 {@link ReactInstanceManager}的实例。可以在你的{@link Application class} (see
 * {@link ReactApplication})中使用，或者作为静态字段使用。
 */
public abstract class ReactNativeHost {

  private final Application mApplication;
  //单例
  private @Nullable ReactInstanceManager mReactInstanceManager;

  protected ReactNativeHost(Application application) {
    mApplication = application;
  }

  /**获取当前的 {@link ReactInstanceManager} 实例，或者创建一个。*/
  public ReactInstanceManager getReactInstanceManager() {
    if (mReactInstanceManager == null) {
      ReactMarker.logMarker(ReactMarkerConstants.GET_REACT_INSTANCE_MANAGER_START);
      mReactInstanceManager = createReactInstanceManager();
      ReactMarker.logMarker(ReactMarkerConstants.GET_REACT_INSTANCE_MANAGER_END);
    }
    return mReactInstanceManager;
  }

  /**
   * 获取该holder是否包含{@link ReactInstanceManager}实例。例如，如果{@link #getReactInstanceManager()}
   * 在创建对象后至少被调用一次，或者{@link #clear()}被调用。
   */
  public boolean hasInstance() {
    return mReactInstanceManager != null;
  }

  /**
   * Destroy the current instance and release the internal reference to it, allowing it to be GCed.
   */
  public void clear() {
    if (mReactInstanceManager != null) {
      mReactInstanceManager.destroy();
      mReactInstanceManager = null;
    }
  }

  protected ReactInstanceManager createReactInstanceManager() {
    ReactMarker.logMarker(ReactMarkerConstants.BUILD_REACT_INSTANCE_MANAGER_START);

    ReactInstanceManagerBuilder builder =
        ReactInstanceManager.builder()
            .setApplication(mApplication)
            .setJSMainModulePath(getJSMainModuleName())
            .setUseDeveloperSupport(getUseDeveloperSupport())
            .setRedBoxHandler(getRedBoxHandler())
            .setJavaScriptExecutorFactory(getJavaScriptExecutorFactory())
            .setUIImplementationProvider(getUIImplementationProvider())
            .setJSIModulesPackage(getJSIModulePackage())
            .setInitialLifecycleState(LifecycleState.BEFORE_CREATE);

    //添加Package
    for (ReactPackage reactPackage : getPackages()) {
      builder.addPackage(reactPackage);
    }
    //在assets目录下内置bundle包
    String jsBundleFile = getJSBundleFile();
    if (jsBundleFile != null) {
      builder.setJSBundleFile(jsBundleFile);
    } else {
      builder.setBundleAssetName(Assertions.assertNotNull(getBundleAssetName()));
    }
    ReactInstanceManager reactInstanceManager = builder.build();
    ReactMarker.logMarker(ReactMarkerConstants.BUILD_REACT_INSTANCE_MANAGER_END);
    return reactInstanceManager;
  }

  /** 获取{@link RedBoxHandler}来发送红RedBox-related相关的回调。*/
  protected @Nullable RedBoxHandler getRedBoxHandler() {
    return null;
  }

  /** 获取{@link JavaScriptExecutorFactory}。重写它使用自定义Executor。*/
  protected @Nullable JavaScriptExecutorFactory getJavaScriptExecutorFactory() {
    return null;
  }

  protected final Application getApplication() {
    return mApplication;
  }

  /**
   * 获取要使用的{@link UIImplementationProvider}。如果你想使用自定义的UI实现重写这个方法。
   *
   * <p>注意：这是非常高级的功能，在99%的情况下，你不需要重写它。
   */
  protected UIImplementationProvider getUIImplementationProvider() {
    return new UIImplementationProvider();
  }

  protected @Nullable JSIModulePackage getJSIModulePackage() {
    return null;
  }

  /**
   * 返回main module的名称。确定用于从packeger server获取JS bundle的URL。它只在启用dev support时使用。这是在创
   * 建{@link ReactInstanceManager}后第一个执行的文件。如"index.android"
   */
  protected String getJSMainModuleName() {
    return "index.android";
  }

  /**
   * 返回bundle文件自定义路径。这用于bundle应该从自定义路径加载的情况。默认情况下它从Android assets加载，由
   * {@link getBundleAssetName }指定的路径。如： "file://sdcard/myapp_cache/index.android.bundle"
   */
  protected @Nullable String getJSBundleFile() {
    return null;
  }

  /**
   * 返回bundle在assets中的名字。如果它为null，并且没有指定bundle包的文件路径，应用程序将只在
   * {@code getUseDeveloperSupport}启用的情况下工作，并且总是尝试从packager server加载JS Bundle。如："index
   * .android.bundle"
   */
  protected @Nullable String getBundleAssetName() {
    return "index.android.bundle";
  }

  /** 返回是否dev模式启动。这将启用如dev菜单。*/
  public abstract boolean getUseDeveloperSupport();

  /**
   *返回应用程序使用的{@link ReactPackage}的列表。您可能希望至少返回{@code MainReactPackage}。如果你的应用程
   * 序除了使用默认视图或模块之外还使用了其他视图或模块，那么你需要在这里包含更多的包。
   */
  protected abstract List<ReactPackage> getPackages();
}
