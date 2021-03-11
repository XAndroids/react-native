/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow
 * @format
 */

'use strict';

import NativeDeviceEventManager from '../../Libraries/NativeModules/specs/NativeDeviceEventManager';
import RCTDeviceEventEmitter from '../EventEmitter/RCTDeviceEventEmitter';

const DEVICE_BACK_EVENT = 'hardwareBackPress';

type BackPressEventName = 'backPress' | 'hardwareBackPress';

const _backPressSubscriptions = [];

//注册hardwareBackPress事件，监听Native返回按钮发出的事件
RCTDeviceEventEmitter.addListener(DEVICE_BACK_EVENT, function() {
  //JS端返回按钮事件观察者，回调观察者方法
  for (let i = _backPressSubscriptions.length - 1; i >= 0; i--) {
    if (_backPressSubscriptions[i]()) {
      return;
    }
  }

  //如果没有观察者，则调用默认的处理，退出App
  BackHandler.exitApp();
});

/**
 * Detect hardware button presses for back navigation.
 *
 * Android: Detect hardware back button presses, and programmatically invoke the default back button
 * functionality to exit the app if there are no listeners or if none of the listeners return true.
 *
 * tvOS: Detect presses of the menu button on the TV remote.  (Still to be implemented:
 * programmatically disable menu button handling
 * functionality to exit the app if there are no listeners or if none of the listeners return true.)
 *
 * iOS: Not applicable.
 *
 * The event subscriptions are called in reverse order (i.e. last registered subscription first),
 * and if one subscription returns true then subscriptions registered earlier will not be called.
 *
 * Example:
 *
 * ```javascript
 * BackHandler.addEventListener('hardwareBackPress', function() {
 *  // this.onMainScreen and this.goBack are just examples, you need to use your own implementation here
 *  // Typically you would use the navigator here to go to the last state.
 *
 *  if (!this.onMainScreen()) {
 *    this.goBack();
 *    return true;
 *  }
 *  return false;
 * });
 * ```
 */
type TBackHandler = {|
  +exitApp: () => void,
  +addEventListener: (
    eventName: BackPressEventName,
    handler: Function,
  ) => {remove: () => void},
  +removeEventListener: (
    eventName: BackPressEventName,
    handler: Function,
  ) => void,
|};
const BackHandler: TBackHandler = {
  /**
  * 默认退出应用
  */
  exitApp: function(): void {
    if (!NativeDeviceEventManager) {
      return;
    }

    //调用默认返回按键处理器
    NativeDeviceEventManager.invokeDefaultBackPressHandler();
  },

   /**
   * 添加一个事件处理器，支持的事件：
   * - `hardwareBackPress`:当按下Android硬件后退按钮或tvOS菜单按钮时触发
   */
  addEventListener: function(
    eventName: BackPressEventName,
    handler: Function,
  ): {remove: () => void} {
    if (_backPressSubscriptions.indexOf(handler) === -1) {
      _backPressSubscriptions.push(handler);
    }
    return {
      remove: (): void => BackHandler.removeEventListener(eventName, handler),
    };
  },

   /**
   * 移除事件处理器
   */
  removeEventListener: function(
    eventName: BackPressEventName,
    handler: Function,
  ): void {
    if (_backPressSubscriptions.indexOf(handler) !== -1) {
      _backPressSubscriptions.splice(
        _backPressSubscriptions.indexOf(handler),
        1,
      );
    }
  },
};

module.exports = BackHandler;
