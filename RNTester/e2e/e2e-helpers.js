/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @emails oncall+react_native
 * @format
 */

/* global element, by, expect */

//通过组件过滤然后点击标签，来打开跟列表中的一个组件案例
exports.openComponentWithLabel = async (component, label) => {
  //by.id()：匹配使用testID属性给View分配的id
  //replaceText()：向输入框粘贴文本
  await element(by.id('explorer_search')).replaceText(component);
  //by.label：通过iOS accessibilityLabel，或者Android contentDescription查找元素
  //tab()：模拟点击一个元素
  await element(by.label(label)).tap();
};

//通过过滤示例标题来打开一个单独的示例
exports.openExampleWithTitle = async title => {
  await element(by.id('example_search')).replaceText(title);
};
