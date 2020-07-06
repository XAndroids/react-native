/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.bridge;

public interface JavaScriptExecutorFactory {
  JavaScriptExecutor create() throws Exception;

  /**
   * 这个特定的JavaScriptExecutor采样分析器在运行时通常是一个单例的，因此，该方法存在于这里而不在{@link JavaSc
   * riptExecutor}中。
   */
  void startSamplingProfiler();

  /**
   * 停止采样配置文件
   * @param filename 抽样分析器的结果转储到的文件名
   */
  void stopSamplingProfiler(String filename);
}
