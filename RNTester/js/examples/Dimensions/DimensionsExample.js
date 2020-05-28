/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow
 */

'use strict';

import {Dimensions, Text, useWindowDimensions} from 'react-native';
import * as React from 'react';

class DimensionsSubscription extends React.Component<
  {dim: string},
  {dims: Object},
> {
  state = {
    //Dimensions.get()获取window或者screen尺寸
    //参考：https://reactnative.dev/docs/0.50/dimensions#set
    dims: Dimensions.get(this.props.dim),
  };

  componentDidMount() {
    //Dimensions.addEventListener：当Dimensions对象的值(screen和window)改变的时候，触发change
    Dimensions.addEventListener('change', this._handleDimensionsChange);
  }

  componentWillUnmount() {
    Dimensions.removeEventListener('change', this._handleDimensionsChange);
  }

  _handleDimensionsChange = dimensions => {
    this.setState({
      dims: dimensions[this.props.dim],
    });
  };

  render() {
    return <Text>{JSON.stringify(this.state.dims, null, 2)}</Text>;
  }
}

exports.title = 'Dimensions';
exports.description = 'Dimensions of the viewport';
exports.examples = [
  {
    title: 'useWindowDimensions hook',
    render(): React.Node {
      const DimensionsViaHook = () => {
        //通过useWindowDimensions，当屏幕大小变化的时候，自动更新width和height；
        //参考：https://reactnative.dev/docs/usewindowdimensions
        const dims = useWindowDimensions();
        return <Text>{JSON.stringify(dims, null, 2)}</Text>;
      };
      return <DimensionsViaHook />;
    },
  },
  {
    title: 'Non-component `get` API: window',
    render(): React.Element<any> {
      return <DimensionsSubscription dim="window" />;
    },
  },
  {
    title: 'Non-component `get` API: screen',
    render(): React.Element<any> {
      return <DimensionsSubscription dim="screen" />;
    },
  },
];
