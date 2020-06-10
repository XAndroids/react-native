/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport;

import android.content.Context;
import androidx.annotation.Nullable;
import com.facebook.react.devsupport.interfaces.DevBundleDownloadListener;
import com.facebook.react.devsupport.interfaces.DevSupportManager;
import com.facebook.react.packagerconnection.RequestHandler;
import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * 一个创建 {@link DevSupportManager}实现实例的简单工厂。如果DevSupportManagerImpl存在，则使用反射创建它。这
 * 允许Proguard在发布版本中剥离该类及其依赖关系。如果没有找到该类，则返回{@link DisabledDevSupportManager}。
 */
public class DevSupportManagerFactory {

  private static final String DEVSUPPORT_IMPL_PACKAGE = "com.facebook.react.devsupport";
  private static final String DEVSUPPORT_IMPL_CLASS = "DevSupportManagerImpl";

  public static DevSupportManager create(
      Context applicationContext,
      ReactInstanceManagerDevHelper reactInstanceManagerHelper,
      @Nullable String packagerPathForJSBundleName,
      boolean enableOnCreate,
      int minNumShakes) {

    return create(
        applicationContext,
        reactInstanceManagerHelper,
        packagerPathForJSBundleName,
        enableOnCreate,
        null,
        null,
        minNumShakes,
        null);
  }

  public static DevSupportManager create(
      Context applicationContext,
      ReactInstanceManagerDevHelper reactInstanceManagerHelper,
      @Nullable String packagerPathForJSBundleName,
      boolean enableOnCreate,
      @Nullable RedBoxHandler redBoxHandler,
      @Nullable DevBundleDownloadListener devBundleDownloadListener,
      int minNumShakes,
      @Nullable Map<String, RequestHandler> customPackagerCommandHandlers) {
    //如果不是debug模式，则不支持debug支持的功能，使用Dev虚拟实现
    if (!enableOnCreate) {
      return new DisabledDevSupportManager();
    }
    try {
      //如果是debug模式，则使用DEV实现，支持debug等功能
      // ProGuard在这种情况下非常聪明，如果它检测到lass.forName()的静态字符串调用，它会保留一个类。所以我们生成
      // 一个准动态字符串来混淆它。
      String className =
          new StringBuilder(DEVSUPPORT_IMPL_PACKAGE)
              .append(".")
              .append(DEVSUPPORT_IMPL_CLASS)
              .toString();
      Class<?> devSupportManagerClass = Class.forName(className);
      Constructor constructor =
          devSupportManagerClass.getConstructor(
              Context.class,
              ReactInstanceManagerDevHelper.class,
              String.class,
              boolean.class,
              RedBoxHandler.class,
              DevBundleDownloadListener.class,
              int.class,
              Map.class);
      return (DevSupportManager)
          constructor.newInstance(
              applicationContext,
              reactInstanceManagerHelper,
              packagerPathForJSBundleName,
              true,
              redBoxHandler,
              devBundleDownloadListener,
              minNumShakes,
              customPackagerCommandHandlers);
    } catch (Exception e) {
      throw new RuntimeException(
          "Requested enabled DevSupportManager, but DevSupportManagerImpl class was not found"
              + " or could not be created",
          e);
    }
  }
}
