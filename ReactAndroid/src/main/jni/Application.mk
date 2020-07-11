# Copyright (c) Facebook, Inc. and its affiliates.
#
# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

#Application.mk：指定 ndk-build 的项目级设置。默认情况下，它位于应用项目目录中的 jni/Application.mk 下。

#APP_BUILD_SCRIPT变量：默认情况下，ndk-build假定Android.mk文件位于项目根目录的相对路径jni/Android.mk中。
#如需从其他位置加载Android.mk 文件，请将 APP_BUILD_SCRIPT 设置为 Android.mk 文件的绝对路径。
APP_BUILD_SCRIPT := Android.mk

#APP_ABI变量：默认情况下，NDK 构建系统会为所有非弃用ABI生成代码。您可以使用APP_ABI设置为特定ABI生成代码。
APP_ABI := armeabi-v7a x86 arm64-v8a x86_64
#APP_PLATFORM变量：声明构建此应用所面向的 Android API 级别，并对应于应用的 minSdkVersion。
APP_PLATFORM := android-16

APP_MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

# NDK_MODULE_PATH是什么?
#   这与Linux中的PATH环境变量相当。NDK_MODULE_PATH的目的是提供一个目录列表，其中包含我们希望ndk-build编译的模
#   块。
# HOST_DIRSEP是什么?
#   在PATH中，目录由一个':'分隔。在NDK_MODULE_PATH中，目录由$(HOST_DIRSEP)分隔。
# APP_MK_DIR, THIRD_PARTY_NDK_DIR等在哪里定义?
#   NDK_MODULE_PATH中的目录(例如:APP_MK_DIR, THIRD_PARTY_NDK_DIR等)是在build.gradle中定义的。
NDK_MODULE_PATH := $(APP_MK_DIR)$(HOST_DIRSEP)$(THIRD_PARTY_NDK_DIR)$(HOST_DIRSEP)$(REACT_COMMON_DIR)$(HOST_DIRSEP)$(APP_MK_DIR)first-party$(HOST_DIRSEP)$(REACT_SRC_DIR)

#APP_STL变量：用于此应用的 C++ 标准库。默认情况下使用 system STL。其他选项包括 c++_shared、c++_static 和 none。
APP_STL := c++_shared

#APP_CFLAGS/APP_CPPFLAGS变量：要为项目中的所有 C/C++ 编译传递的标记。
APP_CFLAGS := -Wall -Werror -fexceptions -frtti -DWITH_INSPECTOR=1
APP_CPPFLAGS := -std=c++1y

# Make sure every shared lib includes a .note.gnu.build-id header
# APP_LDFLAGS：关联可执行文件和共享库时要传递的标记。
APP_LDFLAGS := -Wl,--build-id

# NDK_TOOLCHAIN_VERSION变量：编译器类型、版本，设置clang编译器。
NDK_TOOLCHAIN_VERSION := clang
