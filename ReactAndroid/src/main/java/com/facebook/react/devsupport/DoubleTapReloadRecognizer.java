/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.devsupport;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * 一个类允许识别"R"的双击，用于重载{@link AbstractReactActivity},{@link RedBoxDialog}
 * 和{@link ReactActivity}的JS。
 */
public class DoubleTapReloadRecognizer {
  private boolean mDoRefresh = false;
  private static final long DOUBLE_TAP_DELAY = 200;

  public boolean didDoubleTapR(int keyCode, View view) {
    if (keyCode == KeyEvent.KEYCODE_R && !(view instanceof EditText)) {
      if (mDoRefresh) {
        mDoRefresh = false;
        return true;
      } else {
        mDoRefresh = true;
        new Handler()
            .postDelayed(
                new Runnable() {
                  @Override
                  public void run() {
                    mDoRefresh = false;
                  }
                },
                DOUBLE_TAP_DELAY);
      }
    }
    return false;
  }
}
