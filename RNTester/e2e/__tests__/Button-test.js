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

describe('Button', () => {
  //这个上下文内任何测试开始之前运行这个方法
  beforeAll(async () => {
    //重新加载JS bundle
    await device.reloadReactNative();
    //打开<Button>示例页面
    await openComponentWithLabel(
      '<Button>',
      '<Button> Simple React Native button component.',
    );
  });

  //按钮应该可点击，验证onPress属性
  it('Simple button should be tappable', async () => {
    await openExampleWithTitle('Simple Button');
    await element(by.id('simple_button')).tap();
    await expect(element(by.text('Simple has been pressed!'))).toBeVisible();
    await element(by.text('OK')).tap();
  });

  //设置颜色的按钮应该可点击
  //FIXME 这个案例不应该重点测试color颜色是否生效？？
  it('Adjusted color button should be tappable', async () => {
    await openExampleWithTitle('Adjusted color');
    await element(by.id('purple_button')).tap();
    await expect(element(by.text('Purple has been pressed!'))).toBeVisible();
    await element(by.text('OK')).tap();
  });

  //两个使用JustifyContent:'space-between'的按钮可以点击
  //FIXME 这个又验证了什么属性呢？？
  it("Two buttons with JustifyContent:'space-between' should be tappable", async () => {
    await openExampleWithTitle('Fit to text layout');
    await element(by.id('left_button')).tap();
    await expect(element(by.text('Left has been pressed!'))).toBeVisible();
    await element(by.text('OK')).tap();

    await element(by.id('right_button')).tap();
    await expect(element(by.text('Right has been pressed!'))).toBeVisible();
    await element(by.text('OK')).tap();
  });

  //无效按钮不可以交互，验证disable属性
  it('Disabled button should not interact', async () => {
    await openExampleWithTitle('Disabled Button');
    await element(by.id('disabled_button')).tap();
    await expect(
      element(by.text('Disabled has been pressed!')),
    ).toBeNotVisible();
  });
});
