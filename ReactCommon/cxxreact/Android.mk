# Copyright (c) Facebook, Inc. and its affiliates.
#
# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

#LOCAL_PATH变量：表示源文件在开发树中的位置，构建系统提供的宏函数 my-dir 将返回当前目录（Android.mk 文件本身所在
#的目录）的路径。
LOCAL_PATH := $(call my-dir)

#CLEAR_VARS变量：其值由构建系统提供，指向一个特殊的 GNU Makefile，后者会为您清除许多 LOCAL_XXX 变量。
include $(CLEAR_VARS)

#LOCAL_MODULE变量：您要构建的模块的名称。
LOCAL_MODULE := reactnative

#LOCAL_SRC_FILES：列举源文件，以空格分隔多个文件。必须包含要构建到模块中的 C 和/或 C++ 源文件列表。
LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.cpp)

#LOCAL_C_INCLUDES变量：指定相对于NDK root目录的路径列表，以便在编译所有源文件（C、C++ 和 Assembly）时添加到
#include 搜索路径中。
LOCAL_C_INCLUDES := $(LOCAL_PATH)/..
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

#LOCAL_CFLAGS变量：用于设置在构建 C 和 C++ 源文件时构建系统要传递的编译器标记。
LOCAL_CFLAGS := \
  -DLOG_TAG=\"ReactNative\"
LOCAL_CFLAGS += -fexceptions -frtti -Wno-unused-lambda-capture

#LOCAL_STATIC_LIBRARIES变量：存储当前模块依赖的静态库模块列表。
LOCAL_STATIC_LIBRARIES := boost jsi
#LOCAL_SHARED_LIBRARIES变量：列出此模块在运行时依赖的共享库模块。此信息是链接时必需的信息，用于将相应的信息嵌入到
#生成的文件中。
LOCAL_SHARED_LIBRARIES := jsinspector libfolly_json glog

#帮助系统将一切连接到一起
#BUILD_SHARED_LIBRARY变量：指向一个 GNU Makefile 脚本，该脚本会收集您自最近 include 以来在 LOCAL_XXX 变量中
#定义的所有信息
include $(BUILD_STATIC_LIBRARY)

#import-module函数：用于按模块名称来查找和包含模块的 Android.mk 文件。
$(call import-module,fb)
$(call import-module,folly)
$(call import-module,jsc)
$(call import-module,glog)
$(call import-module,jsi)
$(call import-module,jsinspector)
$(call import-module,hermes/inspector)
