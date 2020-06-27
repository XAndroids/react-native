/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

'use strict';

const createReactNativeComponentClass = require('../Renderer/shims/createReactNativeComponentClass');
const getNativeComponentAttributes = require('./getNativeComponentAttributes');

/**
 * 创建可以像React组件一样使用的值，React组件代表native view Manager。您应该创建封装这些值的JavaScript模块，以便记忆。例如:
 * const View = requireNativeComponent('RCTView');
 */
const requireNativeComponent = (uiViewClassName: string): string =>
  createReactNativeComponentClass(uiViewClassName, () =>
    getNativeComponentAttributes(uiViewClassName),
  );

module.exports = requireNativeComponent;
