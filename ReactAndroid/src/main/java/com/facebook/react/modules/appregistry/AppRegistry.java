/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.modules.appregistry;

import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.WritableMap;

/** JS module接口 - 给定一个key启动React应用程序的主要入口点。*/
public interface AppRegistry extends JavaScriptModule {

  void runApplication(String appKey, WritableMap appParameters);

  void unmountApplicationComponentAtRootTag(int rootNodeTag);

  void startHeadlessTask(int taskId, String taskKey, WritableMap data);
}
