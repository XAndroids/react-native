/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import androidx.annotation.NonNull;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewManager;
import java.util.List;

/**
 * 通过两种不同的方式，用于向catalyst fraework提供额外能力的主要接口：
 * <ol>
 *  <li>注册新的native modules
 *  <li>注册可以从native module或native code的其它部分访问新JS模块（从保重require JS module并不意味着它会自
 *      动包括为JS bundle的一部分，因此在JS端应该有一段相应的代码，需要实现那个JS模块，这样它才能被绑定）
 *  <li>注册自定义native view（view manager）和自定义事件类型
 *  <li>注册暴露给JS的native打包的seets/resources（如：images）
 */
public interface ReactPackage {

  /**
   * @param reactContext react application context that can be used to create modules
   * @return list of native modules to register with the newly created catalyst instance
   */
  @NonNull
  List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext);

  /** @return a list of view managers that should be registered with {@link UIManagerModule} */
  @NonNull
  List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext);
}
