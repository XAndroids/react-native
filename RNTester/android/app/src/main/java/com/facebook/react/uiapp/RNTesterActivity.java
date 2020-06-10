/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.uiapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

public class RNTesterActivity extends ReactActivity {
  //RNTesterActivity代理
  public static class RNTesterActivityDelegate extends ReactActivityDelegate {
    private static final String PARAM_ROUTE = "route";
    private Bundle mInitialProps = null;
    private final @Nullable ReactActivity mActivity;

    public RNTesterActivityDelegate(ReactActivity activity, String mainComponentName) {
      super(activity, mainComponentName);
      this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      //在调用使用它的super之前得到远程参数
      Bundle bundle = mActivity.getIntent().getExtras();
      if (bundle != null && bundle.containsKey(PARAM_ROUTE)) {
        String routeUri =
            new StringBuilder("rntester://example/")
                .append(bundle.getString(PARAM_ROUTE))
                .append("Example")
                .toString();
        mInitialProps = new Bundle();
        mInitialProps.putString("exampleFromAppetizeParams", routeUri);
      }
      super.onCreate(savedInstanceState);
    }

    @Override
    protected Bundle getLaunchOptions() {
      return mInitialProps;
    }
  }

  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new RNTesterActivityDelegate(this, getMainComponentName());
  }

  @Override
  protected String getMainComponentName() {
    //返回JavaScript注册的主组件的名称。用于安排组件的渲染。如"MoviesApp"
    return "RNTesterApp";
  }
}
