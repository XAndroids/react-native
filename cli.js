#!/usr/bin/env node
// 这句话是一个shebang line实例, 作用是告诉系统运行这个文件的解释器是node；
// 比如，本来需要这样运行node ./cli.js，但是加上了这句后就可以直接./cli.js运行了

/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 */

'use strict';

var cli = require('@react-native-community/cli');

if (require.main === module) {
  cli.run();
}

module.exports = cli;
