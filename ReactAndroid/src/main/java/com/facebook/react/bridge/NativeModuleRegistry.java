/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.systrace.Systrace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 一组公开的Java api，给特定JavaScript实例的。*/
public class NativeModuleRegistry {

  private final ReactApplicationContext mReactApplicationContext;
  private final Map<String, ModuleHolder> mModules;

  public NativeModuleRegistry(
      ReactApplicationContext reactApplicationContext, Map<String, ModuleHolder> modules) {
    mReactApplicationContext = reactApplicationContext;
    mModules = modules;
  }

  /** Private getters for combining NativeModuleRegistrys */
  private Map<String, ModuleHolder> getModuleMap() {
    return mModules;
  }

  private ReactApplicationContext getReactApplicationContext() {
    return mReactApplicationContext;
  }

  /* package */ Collection<JavaModuleWrapper> getJavaModules(JSInstance jsInstance) {
    ArrayList<JavaModuleWrapper> javaModules = new ArrayList<>();
    for (Map.Entry<String, ModuleHolder> entry : mModules.entrySet()) {
      if (!entry.getValue().isCxxModule()) {
        javaModules.add(new JavaModuleWrapper(jsInstance, entry.getValue()));
      }
    }
    return javaModules;
  }

  /* package */ Collection<ModuleHolder> getCxxModules() {
    ArrayList<ModuleHolder> cxxModules = new ArrayList<>();
    for (Map.Entry<String, ModuleHolder> entry : mModules.entrySet()) {
      if (entry.getValue().isCxxModule()) {
        cxxModules.add(entry.getValue());
      }
    }
    return cxxModules;
  }

  /*
   * Adds any new modules to the current module registry
   */
  /* package */ void registerModules(NativeModuleRegistry newRegister) {

    Assertions.assertCondition(
        mReactApplicationContext.equals(newRegister.getReactApplicationContext()),
        "Extending native modules with non-matching application contexts.");

    Map<String, ModuleHolder> newModules = newRegister.getModuleMap();

    for (Map.Entry<String, ModuleHolder> entry : newModules.entrySet()) {
      String key = entry.getKey();
      if (!mModules.containsKey(key)) {
        ModuleHolder value = entry.getValue();
        mModules.put(key, value);
      }
    }
  }

  /* package */ void notifyJSInstanceDestroy() {
    mReactApplicationContext.assertOnNativeModulesQueueThread();
    Systrace.beginSection(
        Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "NativeModuleRegistry_notifyJSInstanceDestroy");
    try {
      for (ModuleHolder module : mModules.values()) {
        module.destroy();
      }
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
    }
  }

  /* package */ void notifyJSInstanceInitialized() {
    mReactApplicationContext.assertOnNativeModulesQueueThread(
        "From version React Native v0.44, "
            + "native modules are explicitly not initialized on the UI thread. See "
            + "https://github.com/facebook/react-native/wiki/Breaking-Changes#d4611211-reactnativeandroidbreaking-move-nativemodule-initialization-off-ui-thread---aaachiuuu "
            + " for more details.");
    ReactMarker.logMarker(ReactMarkerConstants.NATIVE_MODULE_INITIALIZE_START);
    Systrace.beginSection(
        Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "NativeModuleRegistry_notifyJSInstanceInitialized");

    try {
      for (ModuleHolder module : mModules.values()) {
        module.markInitializable();
      }
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
      ReactMarker.logMarker(ReactMarkerConstants.NATIVE_MODULE_INITIALIZE_END);
    }
  }

  public void onBatchComplete() {
    // 唯一使用onBatchComplete的native module是UI Manager。因此，我们没有遍历所有模块来查找这个实例，然后调用
    // 它，而是缩短了搜索，只在UI Manager上调用OnBatchComplete。有了Fabric, UIManager将不再是一个NativeModu
    // le，所以这个调用将简单地消失
    ModuleHolder moduleHolder = mModules.get("UIManager");
    if (moduleHolder != null && moduleHolder.hasInstance()) {
      ((OnBatchCompleteListener) moduleHolder.getModule()).onBatchComplete();
    }
  }

  public <T extends NativeModule> boolean hasModule(Class<T> moduleInterface) {
    String name = moduleInterface.getAnnotation(ReactModule.class).name();
    return mModules.containsKey(name);
  }

  public <T extends NativeModule> T getModule(Class<T> moduleInterface) {
    ReactModule annotation = moduleInterface.getAnnotation(ReactModule.class);
    if (annotation == null) {
      throw new IllegalArgumentException(
          "Could not find @ReactModule annotation in class " + moduleInterface.getName());
    }
    return (T)
        Assertions.assertNotNull(
                mModules.get(annotation.name()),
                annotation.name()
                    + " could not be found. Is it defined in "
                    + moduleInterface.getName())
            .getModule();
  }

  public boolean hasModule(String name) {
    return mModules.containsKey(name);
  }

  public NativeModule getModule(String name) {
    return Assertions.assertNotNull(mModules.get(name), "Could not find module with name " + name)
        .getModule();
  }

  public List<NativeModule> getAllModules() {
    List<NativeModule> modules = new ArrayList<>();
    for (ModuleHolder module : mModules.values()) {
      modules.add(module.getModule());
    }
    return modules;
  }
}
