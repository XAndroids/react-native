/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

import android.content.Context;
import com.facebook.react.common.DebugServerException;

/**
 * 一个存储JS bundle信息，并允许{@link JSBundleLoaderDelegate} (e.g.{@link CatalystInstance})通过
 * {@link ReactBridge}加载正确bundle的类。
 */
public abstract class JSBundleLoader {

  /**
   * 这个loader推荐用于你的应用的release版本。在这种情况下，应用使用本地JS executor。JS bundle将从本地代码的assets
   * 中读取，以节省将大字符串传递到本地内存的时间。
   */
  public static JSBundleLoader createAssetLoader(
      final Context context, final String assetUrl, final boolean loadSynchronously) {
    return new JSBundleLoader() {
      @Override
      public String loadScript(JSBundleLoaderDelegate delegate) {
        delegate.loadScriptFromAssets(context.getAssets(), assetUrl, loadSynchronously);
        return assetUrl;
      }
    };
  }

  /**
   * This loader loads bundle from file system. The bundle will be read in native code to save on
   * passing large strings from java to native memory.
   */
  public static JSBundleLoader createFileLoader(final String fileName) {
    return createFileLoader(fileName, fileName, false);
  }

  public static JSBundleLoader createFileLoader(
      final String fileName, final String assetUrl, final boolean loadSynchronously) {
    return new JSBundleLoader() {
      @Override
      public String loadScript(JSBundleLoaderDelegate delegate) {
        delegate.loadScriptFromFile(fileName, assetUrl, loadSynchronously);
        return fileName;
      }
    };
  }

  /**
   * 这个loader在bundle从dev server重新加载时使用。在这种情况下，加载器希望JS bundler被预取并存储在本地文件中。
   * 这样做是为了避免在java和native代码之间传递大字符串，并避免在java中分配内存来容纳整个JS包。提供下载bundle的
   * 正确的 {@param sourceURL}是JS堆栈跟踪正确工作的必要条件，并允许源映射正确的标识这些。
   */
  public static JSBundleLoader createCachedBundleFromNetworkLoader(
      final String sourceURL, final String cachedFileLocation) {
    return new JSBundleLoader() {
      @Override
      public String loadScript(JSBundleLoaderDelegate delegate) {
        try {
          delegate.loadScriptFromFile(cachedFileLocation, sourceURL, false);
          return sourceURL;
        } catch (Exception e) {
          throw DebugServerException.makeGeneric(sourceURL, e.getMessage(), e);
        }
      }
    };
  }

  /**
   * 这个loader用于从dev server加载增量包。我们将每个增量消息传递给加载器，并用C++处理它。将其作为字符串传递会由于
   * 内存副本而导致效率低下，这将在后续处理中得到解决。
   */
  public static JSBundleLoader createDeltaFromNetworkLoader(
      final String sourceURL, final NativeDeltaClient nativeDeltaClient) {
    return new JSBundleLoader() {
      @Override
      public String loadScript(JSBundleLoaderDelegate delegate) {
        try {
          delegate.loadScriptFromDeltaBundle(sourceURL, nativeDeltaClient, false);
          return sourceURL;
        } catch (Exception e) {
          throw DebugServerException.makeGeneric(sourceURL, e.getMessage(), e);
        }
      }
    };
  }

  /**
   * 这个加载器在启用代理调试时使用。在这种情况下，从设备获取bundle是没有意义的，因为远程执行器无论如何都要做这件事。
   */
  public static JSBundleLoader createRemoteDebuggerBundleLoader(
      final String proxySourceURL, final String realSourceURL) {
    return new JSBundleLoader() {
      @Override
      public String loadScript(JSBundleLoaderDelegate delegate) {
        delegate.setSourceURLs(realSourceURL, proxySourceURL);
        return realSourceURL;
      }
    };
  }

  /** 加载script，返回它加载代码的URL。*/
  public abstract String loadScript(JSBundleLoaderDelegate delegate);
}
