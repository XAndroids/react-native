/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react;

import com.facebook.react.bridge.ModuleSpec;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.devsupport.JSCHeapCapture;
import com.facebook.react.devsupport.JSDevSupport;
import com.facebook.react.module.annotations.ReactModuleList;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;

/**
 * Package定义核心框架模块(如UIManager)。它应该用于需要与其他框架部件进行特殊集成的模块(例如，用于加载视图管理器的
 * 包列表)。
 */
@ReactModuleList(
    nativeModules = {
      JSCHeapCapture.class,
      JSDevSupport.class,
    })
/* package */ class DebugCorePackage extends LazyReactPackage {

  DebugCorePackage() {}

  @Override
  public List<ModuleSpec> getNativeModules(final ReactApplicationContext reactContext) {
    List<ModuleSpec> moduleSpecList = new ArrayList<>();
    moduleSpecList.add(
        ModuleSpec.nativeModuleSpec(
            JSCHeapCapture.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new JSCHeapCapture(reactContext);
              }
            }));
    return moduleSpecList;
  }

  @Override
  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    return LazyReactPackage.getReactModuleInfoProviderViaReflection(this);
  }
}
