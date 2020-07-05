// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react;

import com.facebook.react.bridge.ModuleHolder;
import com.facebook.react.bridge.NativeModuleRegistry;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.config.ReactFeatureFlags;
import java.util.HashMap;
import java.util.Map;

/** 构建NativeModuleRegistry帮助类。*/
public class NativeModuleRegistryBuilder {

  private final ReactApplicationContext mReactApplicationContext;
  private final ReactInstanceManager mReactInstanceManager;

  private final Map<String, ModuleHolder> mModules = new HashMap<>();

  public NativeModuleRegistryBuilder(
      ReactApplicationContext reactApplicationContext, ReactInstanceManager reactInstanceManager) {
    mReactApplicationContext = reactApplicationContext;
    mReactInstanceManager = reactInstanceManager;
  }

  public void processPackage(ReactPackage reactPackage) {
    //我们在使用一个iterable替代一个iterator来确保线程安全，并且这个类表不能修改
    Iterable<ModuleHolder> moduleHolders;
    if (reactPackage instanceof LazyReactPackage) {
      moduleHolders =
          ((LazyReactPackage) reactPackage).getNativeModuleIterator(mReactApplicationContext);
    } else if (reactPackage instanceof TurboReactPackage) {
      moduleHolders =
          ((TurboReactPackage) reactPackage).getNativeModuleIterator(mReactApplicationContext);
    } else {
      moduleHolders =
          ReactPackageHelper.getNativeModuleIterator(
              reactPackage, mReactApplicationContext, mReactInstanceManager);
    }

    for (ModuleHolder moduleHolder : moduleHolders) {
      String name = moduleHolder.getName();
      if (mModules.containsKey(name)) {
        ModuleHolder existingNativeModule = mModules.get(name);
        if (!moduleHolder.getCanOverrideExistingModule()) {
          throw new IllegalStateException(
              "Native module "
                  + name
                  + " tried to override "
                  + existingNativeModule.getClassName()
                  + ". Check the getPackages() method in MainApplication.java, it might be that module is being created twice. If this was your intention, set canOverrideExistingModule=true. "
                  + "This error may also be present if the package is present only once in getPackages() but is also automatically added later during build time by autolinking. Try removing the existing entry and rebuild.");
        }
        mModules.remove(existingNativeModule);
      }
      if (ReactFeatureFlags.useTurboModules && moduleHolder.isTurboModule()) {
        // If this module is a TurboModule, and if TurboModules are enabled, don't add this module

        // This condition is after checking for overrides, since if there is already a module,
        // and we want to override it with a turbo module, we would need to remove the modules thats
        // already in the list, and then NOT add the new module, since that will be directly exposed

        // Note that is someone uses {@link NativeModuleRegistry#registerModules}, we will NOT check
        // for TurboModules - assuming that people wanted to explicitly register native modules
        // there
        continue;
      }
      mModules.put(name, moduleHolder);
    }
  }

  public NativeModuleRegistry build() {
    return new NativeModuleRegistry(mReactApplicationContext, mModules);
  }
}
