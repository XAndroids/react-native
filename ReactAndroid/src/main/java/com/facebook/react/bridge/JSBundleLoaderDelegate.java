/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import android.content.Context;
import android.content.res.AssetManager;

/** 用于使用{@link JSBundleLoader}初始化JavaScript的类的接口*/
public interface JSBundleLoaderDelegate {

  /**
   * 从Android asset中加载一个JS Bundle。 See {@link JSBundleLoader#createAssetLoader(Context,String, boolean)}
   */
  void loadScriptFromAssets(AssetManager assetManager, String assetURL, boolean loadSynchronously);

  /**
   * 从文件系统加载JS Bundle。 See {@link JSBundleLoader#createFileLoader(String)} and {@link JSBundleLoader
   * #createCachedBundleFromNetworkLoader(String, String)}
   */
  void loadScriptFromFile(String fileName, String sourceURL, boolean loadSynchronously);

  /**
   * 从Metro加载delta bundle。See {@link JSBundleLoader#createDeltaFromNetworkLoader(String, NativeDeltaClient)}
   */
  void loadScriptFromDeltaBundle(
      String sourceURL, NativeDeltaClient deltaClient, boolean loadSynchronously);

  /**
   * 这个API用于JS bundle不是在设备上执行，而是在主机上执行的情况。在这种情况下，我们必须为JS包提供两个源url：一个
   * 用于设备，另一个用于远程调试机器。
   *
   * @param deviceURL 可从此设备访问源URL
   * @param remoteURL 可从执行JS的远程机器访问的源URL
   */
  void setSourceURLs(String deviceURL, String remoteURL);
}
