/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import com.facebook.proguard.annotations.DoNotStrip;

/**
 * 接口表示类，是JS中相同名称模块的接口。调用这个接口上的函数将会导致调用JS中相应的方法。
 *
 * <p>当扩展JavaScriptModule并将其注册到一个CatalystInstance时，所有的公开方法都假定是一个与这个类同名的JS模块
 * 上的实现。调用从{@link ReactContext#getJSModule} or {@link CatalystInstance#getJSModule}返回的对象上的
 * 方法将导致在JS中调用由模块导出的那些名称的方法。
 *
 * <p>NB: JavaScriptModule不允许方法名重载，因为JS不允许方法名重载。
 */
@DoNotStrip
public interface JavaScriptModule {}
