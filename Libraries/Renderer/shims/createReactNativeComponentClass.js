/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow strict-local
 */

'use strict';

import {ReactNativeViewConfigRegistry} from 'react-native/Libraries/ReactPrivate/ReactNativePrivateInterface';

import type {ViewConfigGetter} from './ReactNativeTypes';

const {register} = ReactNativeViewConfigRegistry;

/**
 * 创建可呈现的ReactNative宿主组件。
 * 对从UIManager加载的view config使用这个方法。
 * 对JavaScript中定义的view config使用createReactNativeComponentClass()。
 * @param {string} config iOS View configuration.
 */
const createReactNativeComponentClass = function(
  name: string,
  callback: ViewConfigGetter,
): string {
  return register(name, callback);
};

module.exports = createReactNativeComponentClass;
