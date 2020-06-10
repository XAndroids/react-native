/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import java.util.List;

/** 用于初始化JSI Modules到JSI Brdidge的接口。*/
public interface JSIModulePackage {

  /**返回包含JSI Module列表的{@link List<JSIModuleSpec>}。*/
   List<JSIModuleSpec> getJSIModules(
      ReactApplicationContext reactApplicationContext, JavaScriptContextHolder jsContext);
}
