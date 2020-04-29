/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @emails oncall+react_native
 * @format
 */

/* global device, element, by, expect */
const {
  openComponentWithLabel,
  openExampleWithTitle,
} = require('../e2e-helpers');

describe('Touchable', () => {
  beforeAll(async () => {
    await device.reloadReactNative();
    await openComponentWithLabel(
      '<Touchable*',
      '<Touchable*> and onPress Touchable and onPress examples.',
    );
  });

  //可点击的Highlight可以被点击
  //验证TouchableHighlight onPress()属性
  it('Touchable Highlight should be tappable', async () => {
    await openExampleWithTitle('<TouchableHighlight>');
    const buttonID = 'touchable_highlight_image_button';
    const button2ID = 'touchable_highlight_text_button';
    const consoleID = 'touchable_highlight_console';

    //第一次点击图片按钮，展示TouchableHighlight onPress文案
    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText(
      'TouchableHighlight onPress',
    );
    //第二次点击图片按钮，展示2x TouchableHighlight onPress文案
    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText(
      '2x TouchableHighlight onPress',
    );
    //点击文案按钮，展示3x TouchableHighlight onPress
    await element(by.id(button2ID)).tap();
    await expect(element(by.id(consoleID))).toHaveText(
      '3x TouchableHighlight onPress',
    );
  });

  //可点击无反馈按钮可以被点击
  //验证TouchableWithoutFeedback onPress()属性
  it('Touchable Without Feedback should be tappable', async () => {
    await openExampleWithTitle('<TouchableWithoutFeedback>');

    const buttonID = 'touchable_without_feedback_button';
    const consoleID = 'touchable_without_feedback_console';

    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText(
      'TouchableWithoutFeedback onPress',
    );

    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText(
      '2x TouchableWithoutFeedback onPress',
    );
  });

  //文案可以被点击
  //验证Text onPress()属性
  it('Text should be tappable', async () => {
    await openExampleWithTitle('<Text onPress={fn}> with highlight');

    const buttonID = 'tappable_text';
    const consoleID = 'tappable_text_console';

    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText('text onPress');

    await element(by.id(buttonID)).tap();
    await expect(element(by.id(consoleID))).toHaveText('2x text onPress');
  });
});
