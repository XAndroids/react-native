/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.uimanager;

import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_UI_MANAGER_MODULE_CONSTANTS_END;
import static com.facebook.react.bridge.ReactMarkerConstants.CREATE_UI_MANAGER_MODULE_CONSTANTS_START;
import static com.facebook.react.uimanager.common.UIManagerType.DEFAULT;
import static com.facebook.react.uimanager.common.UIManagerType.FABRIC;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import com.facebook.common.logging.FLog;
import com.facebook.debug.holder.PrinterHolder;
import com.facebook.debug.tags.ReactDebugOverlayTags;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.GuardedRunnable;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.OnBatchCompleteListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMarker;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.UIManager;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.common.ViewUtil;
import com.facebook.react.uimanager.debug.NotThreadSafeViewHierarchyUpdateDebugListener;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.systrace.Systrace;
import com.facebook.systrace.SystraceMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Native模块，允许JS创建和更新native View。
 * <p>
 *
 * <h2>== 事务要求 ==</h2>
 * 该类的一个需求是确保事务性UI更新同时发生，这意味着中间状态不会呈现在屏幕上。例如，如果一个JS应用程序更新View A的
 * 背景为蓝色，View B的宽度为100，都需要同时出现。实际上，这意味着与单个事务相关的所有UI更新代码都必须作为单个代码
 * 块在UI线程上执行。作为多个代码块执行可以允许平台UI系统终端并呈现部分UI状态。
 *
 * <p>为了方便这一点，这个模块对操作进行排队，然后在事务结束的时候通过{@link NativeViewHierarchyManager}应用到
 * 本地视图层次结构。这些CSSNodeDEPRECATED能够根据其样式规则计算布局，然后该布局的结果x/y/宽度/高度被调度为一个操
 * 作，该操作将在当前批处理结束时应用于native view层次结构。
 * <p>
 *
 * <h2>== CSSNodes ==</h2>
 * 为了允许在非UI线程上进行布局和测量，此模块还对native view对应的中间CSSNodeDEPRECATED对象进行操作。TODO(5241
 * 856):调查在UIManageModule中创建许多小对象时的内存使用情况，并考虑实现一个池TODO(5483063):如果没有发生UI更改，
 * 不要在批处理结束时分发视图层次结构
 */
@ReactModule(name = UIManagerModule.NAME)
public class UIManagerModule extends ReactContextBaseJavaModule
    implements OnBatchCompleteListener, LifecycleEventListener, UIManager {

  /** Enables lazy discovery of a specific {@link ViewManager} by its name. */
  public interface ViewManagerResolver {
    /**
     * {@class UIManagerModule} class uses this method to get a ViewManager by its name. This is the
     * same name that comes from JS by {@code UIManager.ViewManagerName} call.
     */
    @Nullable
    ViewManager getViewManager(String viewManagerName);

    /**
     * Provides a list of view manager names to register in JS as {@code UIManager.ViewManagerName}
     */
    List<String> getViewManagerNames();
  }

  /** Resolves a name coming from native side to a name of the event that is exposed to JS. */
  public interface CustomEventNamesResolver {
    /** Returns custom event name by the provided event name. */
    @Nullable
    String resolveCustomEventName(String eventName);
  }

  public static final String NAME = "UIManager";

  private static final boolean DEBUG =
      PrinterHolder.getPrinter().shouldDisplayLogMessage(ReactDebugOverlayTags.UI_MANAGER);

  private final EventDispatcher mEventDispatcher;
  private final Map<String, Object> mModuleConstants;
  private final Map<String, Object> mCustomDirectEvents;
  private final ViewManagerRegistry mViewManagerRegistry;
  private final UIImplementation mUIImplementation;
  private final MemoryTrimCallback mMemoryTrimCallback = new MemoryTrimCallback();
  private final List<UIManagerModuleListener> mListeners = new ArrayList<>();
  private @Nullable Map<String, WritableMap> mViewManagerConstantsCache;
  private volatile int mViewManagerConstantsCacheSize;

  private int mBatchId = 0;

  @SuppressWarnings("deprecated")
  public UIManagerModule(
      ReactApplicationContext reactContext,
      ViewManagerResolver viewManagerResolver,
      int minTimeLeftInFrameForNonBatchedOperationMs) {
    this(
        reactContext,
        viewManagerResolver,
        new UIImplementationProvider(),
        minTimeLeftInFrameForNonBatchedOperationMs);
  }

  @SuppressWarnings("deprecated")
  public UIManagerModule(
      ReactApplicationContext reactContext,
      List<ViewManager> viewManagersList,
      int minTimeLeftInFrameForNonBatchedOperationMs) {
    this(
        reactContext,
        viewManagersList,
        new UIImplementationProvider(),
        minTimeLeftInFrameForNonBatchedOperationMs);
  }

  @Deprecated
  public UIManagerModule(
      ReactApplicationContext reactContext,
      ViewManagerResolver viewManagerResolver,
      UIImplementationProvider uiImplementationProvider,
      int minTimeLeftInFrameForNonBatchedOperationMs) {
    super(reactContext);
    DisplayMetricsHolder.initDisplayMetricsIfNotInitialized(reactContext);
    mEventDispatcher = new EventDispatcher(reactContext);
    mModuleConstants = createConstants(viewManagerResolver);
    mCustomDirectEvents = UIManagerModuleConstants.getDirectEventTypeConstants();
    mViewManagerRegistry = new ViewManagerRegistry(viewManagerResolver);
    mUIImplementation =
        uiImplementationProvider.createUIImplementation(
            reactContext,
            mViewManagerRegistry,
            mEventDispatcher,
            minTimeLeftInFrameForNonBatchedOperationMs);

    reactContext.addLifecycleEventListener(this);
  }

  @Deprecated
  public UIManagerModule(
      ReactApplicationContext reactContext,
      List<ViewManager> viewManagersList,
      UIImplementationProvider uiImplementationProvider,
      int minTimeLeftInFrameForNonBatchedOperationMs) {
    super(reactContext);
    DisplayMetricsHolder.initDisplayMetricsIfNotInitialized(reactContext);
    mEventDispatcher = new EventDispatcher(reactContext);
    mCustomDirectEvents = MapBuilder.newHashMap();
    mModuleConstants = createConstants(viewManagersList, null, mCustomDirectEvents);
    mViewManagerRegistry = new ViewManagerRegistry(viewManagersList);
    mUIImplementation =
        uiImplementationProvider.createUIImplementation(
            reactContext,
            mViewManagerRegistry,
            mEventDispatcher,
            minTimeLeftInFrameForNonBatchedOperationMs);

    reactContext.addLifecycleEventListener(this);
  }

  /**
   * This method gives an access to the {@link UIImplementation} object that can be used to execute
   * operations on the view hierarchy.
   */
  public UIImplementation getUIImplementation() {
    return mUIImplementation;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Map<String, Object> getConstants() {
    return mModuleConstants;
  }

  @Override
  public void initialize() {
    getReactApplicationContext().registerComponentCallbacks(mMemoryTrimCallback);
    mEventDispatcher.registerEventEmitter(
        DEFAULT, getReactApplicationContext().getJSModule(RCTEventEmitter.class));
  }

  @Override
  public void onHostResume() {
    mUIImplementation.onHostResume();
  }

  @Override
  public void onHostPause() {
    mUIImplementation.onHostPause();
  }

  @Override
  public void onHostDestroy() {
    mUIImplementation.onHostDestroy();
  }

  @Override
  public void onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy();
    mEventDispatcher.onCatalystInstanceDestroyed();

    getReactApplicationContext().unregisterComponentCallbacks(mMemoryTrimCallback);
    YogaNodePool.get().clear();
    ViewManagerPropertyUpdater.clear();
  }

  /**
   * This method is intended to reuse the {@link ViewManagerRegistry} with FabricUIManager. Do not
   * use this method as this will be removed in the near future.
   */
  @Deprecated
  public ViewManagerRegistry getViewManagerRegistry_DO_NOT_USE() {
    return mViewManagerRegistry;
  }

  private static Map<String, Object> createConstants(ViewManagerResolver viewManagerResolver) {
    ReactMarker.logMarker(CREATE_UI_MANAGER_MODULE_CONSTANTS_START);
    SystraceMessage.beginSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "CreateUIManagerConstants")
        .arg("Lazy", true)
        .flush();
    try {
      return UIManagerModuleConstantsHelper.createConstants(viewManagerResolver);
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
      ReactMarker.logMarker(CREATE_UI_MANAGER_MODULE_CONSTANTS_END);
    }
  }

  private static Map<String, Object> createConstants(
      List<ViewManager> viewManagers,
      @Nullable Map<String, Object> customBubblingEvents,
      @Nullable Map<String, Object> customDirectEvents) {
    ReactMarker.logMarker(CREATE_UI_MANAGER_MODULE_CONSTANTS_START);
    SystraceMessage.beginSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "CreateUIManagerConstants")
        .arg("Lazy", false)
        .flush();
    try {
      return UIManagerModuleConstantsHelper.createConstants(
          viewManagers, customBubblingEvents, customDirectEvents);
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
      ReactMarker.logMarker(CREATE_UI_MANAGER_MODULE_CONSTANTS_END);
    }
  }

  /**
   * Helper method to pre-compute the constants for a view manager. This method ensures that we
   * don't block for getting the constants for view managers during TTI
   *
   * @param viewManagerNames
   */
  @Deprecated
  public void preComputeConstantsForViewManager(List<String> viewManagerNames) {
    Map<String, WritableMap> constantsMap = new ArrayMap<>();
    for (String viewManagerName : viewManagerNames) {
      WritableMap constants = computeConstantsForViewManager(viewManagerName);
      if (constants != null) {
        constantsMap.put(viewManagerName, constants);
      }
    }

    // To ensure that this is thread safe, we return an unmodifiableMap
    // We use mViewManagerConstantsCacheSize to count the times we access the contents of the map
    // Once we have accessed all the values, we free this cache
    // Assumption is that JS gets the constants only once for each viewManager.
    // Using this mechanism prevents expensive synchronized blocks, due to the nature of how this is
    // accessed - write one, read multiple times, and then throw the data away.
    mViewManagerConstantsCacheSize = viewManagerNames.size();
    mViewManagerConstantsCache = Collections.unmodifiableMap(constantsMap);
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  public @Nullable WritableMap getConstantsForViewManager(final String viewManagerName) {
    if (mViewManagerConstantsCache != null
        && mViewManagerConstantsCache.containsKey(viewManagerName)) {
      WritableMap constants = mViewManagerConstantsCache.get(viewManagerName);
      if (--mViewManagerConstantsCacheSize <= 0) {
        // Looks like we have read all the values from the cache, so we may as well free this cache
        mViewManagerConstantsCache = null;
      }
      return constants;
    } else {
      return computeConstantsForViewManager(viewManagerName);
    }
  }

  private @Nullable WritableMap computeConstantsForViewManager(final String viewManagerName) {
    ViewManager targetView =
        viewManagerName != null ? mUIImplementation.resolveViewManager(viewManagerName) : null;
    if (targetView == null) {
      return null;
    }

    SystraceMessage.beginSection(
            Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "UIManagerModule.getConstantsForViewManager")
        .arg("ViewManager", targetView.getName())
        .arg("Lazy", true)
        .flush();
    try {
      Map<String, Object> viewManagerConstants =
          UIManagerModuleConstantsHelper.createConstantsForViewManager(
              targetView, null, null, null, mCustomDirectEvents);
      if (viewManagerConstants != null) {
        return Arguments.makeNativeMap(viewManagerConstants);
      }
      return null;
    } finally {
      SystraceMessage.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE).flush();
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  public WritableMap getDefaultEventTypes() {
    return Arguments.makeNativeMap(UIManagerModuleConstantsHelper.getDefaultExportableEventTypes());
  }

  /** Resolves Direct Event name exposed to JS from the one known to the Native side. */
  public CustomEventNamesResolver getDirectEventNamesResolver() {
    return new CustomEventNamesResolver() {
      @Override
      public @Nullable String resolveCustomEventName(String eventName) {
        Map<String, String> customEventType =
            (Map<String, String>) mCustomDirectEvents.get(eventName);
        if (customEventType != null) {
          return customEventType.get("registrationName");
        }
        return eventName;
      }
    };
  }

  @Override
  public void profileNextBatch() {
    mUIImplementation.profileNextBatch();
  }

  @Override
  public Map<String, Long> getPerformanceCounters() {
    return mUIImplementation.getProfiledBatchPerfCounters();
  }

  public <T extends View> int addRootView(final T rootView) {
    return addRootView(rootView, null, null);
  }

  /**
   * Used by native animated module to bypass the process of updating the values through the shadow
   * view hierarchy. This method will directly update native views, which means that updates for
   * layout-related propertied won't be handled properly. Make sure you know what you're doing
   * before calling this method :)
   */
  @Override
  public void synchronouslyUpdateViewOnUIThread(int tag, ReadableMap props) {
    int uiManagerType = ViewUtil.getUIManagerType(tag);
    if (uiManagerType == FABRIC) {
      UIManager fabricUIManager =
          UIManagerHelper.getUIManager(getReactApplicationContext(), uiManagerType);
      fabricUIManager.synchronouslyUpdateViewOnUIThread(tag, props);
    } else {
      mUIImplementation.synchronouslyUpdateViewOnUIThread(tag, new ReactStylesDiffMap(props));
    }
  }

  /**
   * 注册一个新的根视图。JS可以使用manageChildren返回的标签来添加/删除子视图。
   *
   * <p>注意，这个必须在getWidth()/getHeight()实际返回一些东西之后调用。参见CatalystApplicationFragment作为一个例子。
   */
  @Override
  public <T extends View> int addRootView(
      final T rootView, WritableMap initialProps, @Nullable String initialUITemplate) {
    Systrace.beginSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "UIManagerModule.addRootView");
    final int tag = ReactRootViewTagGenerator.getNextRootViewTag();
    final ReactApplicationContext reactApplicationContext = getReactApplicationContext();
    final ThemedReactContext themedRootContext =
        new ThemedReactContext(reactApplicationContext, rootView.getContext());

    mUIImplementation.registerRootView(rootView, tag, themedRootContext);
    Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
    return tag;
  }

  /** 注销一个新的根视图。*/
  @ReactMethod
  public void removeRootView(int rootViewTag) {
    mUIImplementation.removeRootView(rootViewTag);
  }

  public void updateNodeSize(int nodeViewTag, int newWidth, int newHeight) {
    getReactApplicationContext().assertOnNativeModulesQueueThread();

    mUIImplementation.updateNodeSize(nodeViewTag, newWidth, newHeight);
  }

  /**
   * Sets local data for a shadow node corresponded with given tag. In some cases we need a way to
   * specify some environmental data to shadow node to improve layout (or do something similar), so
   * {@code localData} serves these needs. For example, any stateful embedded native views may
   * benefit from this. Have in mind that this data is not supposed to interfere with the state of
   * the shadow view. Please respect one-directional data flow of React.
   */
  public void setViewLocalData(final int tag, final Object data) {
    final ReactApplicationContext reactApplicationContext = getReactApplicationContext();

    reactApplicationContext.assertOnUiQueueThread();

    reactApplicationContext.runOnNativeModulesQueueThread(
        new GuardedRunnable(reactApplicationContext) {
          @Override
          public void runGuarded() {
            mUIImplementation.setViewLocalData(tag, data);
          }
        });
  }

  //创建视图
  @ReactMethod
  public void createView(int tag, String className, int rootViewTag, ReadableMap props) {
    //JS代码中写的控件最终都是通过这个入口createView，创建Android能够识别的View
    if (DEBUG) {
      String message =
          "(UIManager.createView) tag: " + tag + ", class: " + className + ", props: " + props;
      FLog.d(ReactConstants.TAG, message);
      PrinterHolder.getPrinter().logMessage(ReactDebugOverlayTags.UI_MANAGER, message);
    }
    mUIImplementation.createView(tag, className, rootViewTag, props);
  }

  //更新视图
  @ReactMethod
  public void updateView(int tag, String className, ReadableMap props) {
    if (DEBUG) {
      String message =
          "(UIManager.updateView) tag: " + tag + ", class: " + className + ", props: " + props;
      FLog.d(ReactConstants.TAG, message);
      PrinterHolder.getPrinter().logMessage(ReactDebugOverlayTags.UI_MANAGER, message);
    }
    int uiManagerType = ViewUtil.getUIManagerType(tag);
    if (uiManagerType == FABRIC) {
      UIManager fabricUIManager =
          UIManagerHelper.getUIManager(getReactApplicationContext(), uiManagerType);
      fabricUIManager.synchronouslyUpdateViewOnUIThread(tag, props);
    } else {
      mUIImplementation.updateView(tag, className, props);
    }
  }

  /**
   * 在JS父视图中添加/移除/移动视图的接口。
   * @param viewTag 父视图的视图标记
   * @param moveFrom 父视图中要从中移动视图的索引列表
   * @param moveTo 与moveFrom平行，父视图中要移动视图的索引列表
   * @param addChildTags 要添加到父视图的视图标记列表
   * @param addAtIndices 与addChildTags并行，是插入子标记的索引列表
   * @param removeFrom 要永久删除的视图索引列表。对应视图和数据结构的内存应该被回收。
   */
  @ReactMethod
  public void manageChildren(
      int viewTag,
      @Nullable ReadableArray moveFrom,
      @Nullable ReadableArray moveTo,
      @Nullable ReadableArray addChildTags,
      @Nullable ReadableArray addAtIndices,
      @Nullable ReadableArray removeFrom) {
    if (DEBUG) {
      String message =
          "(UIManager.manageChildren) tag: "
              + viewTag
              + ", moveFrom: "
              + moveFrom
              + ", moveTo: "
              + moveTo
              + ", addTags: "
              + addChildTags
              + ", atIndices: "
              + addAtIndices
              + ", removeFrom: "
              + removeFrom;
      FLog.d(ReactConstants.TAG, message);
      PrinterHolder.getPrinter().logMessage(ReactDebugOverlayTags.UI_MANAGER, message);
    }
    mUIImplementation.manageChildren(
        viewTag, moveFrom, moveTo, addChildTags, addAtIndices, removeFrom);
  }

  /**
   * 快速跟踪初始添加视图的界面。假设子视图标签是有序的
   * @param viewTag 父视图的view标签
   * @param childrenTags 要按顺序添加到父标签的数组
   */
  @ReactMethod
  public void setChildren(int viewTag, ReadableArray childrenTags) {
    if (DEBUG) {
      String message = "(UIManager.setChildren) tag: " + viewTag + ", children: " + childrenTags;
      FLog.d(ReactConstants.TAG, message);
      PrinterHolder.getPrinter().logMessage(ReactDebugOverlayTags.UI_MANAGER, message);
    }
    mUIImplementation.setChildren(viewTag, childrenTags);
  }

  /**
   * Replaces the View specified by oldTag with the View specified by newTag within oldTag's parent.
   * This resolves to a simple {@link #manageChildren} call, but React doesn't have enough info in
   * JS to formulate it itself.
   *
   * @deprecated This method will not be available in Fabric UIManager.
   */
  @ReactMethod
  @Deprecated
  public void replaceExistingNonRootView(int oldTag, int newTag) {
    mUIImplementation.replaceExistingNonRootView(oldTag, newTag);
  }

  /**
   * Method which takes a container tag and then releases all subviews for that container upon
   * receipt. TODO: The method name is incorrect and will be renamed, #6033872
   *
   * @param containerTag the tag of the container for which the subviews must be removed
   * @deprecated This method will not be available in Fabric UIManager.
   */
  @ReactMethod
  @Deprecated
  public void removeSubviewsFromContainerWithID(int containerTag) {
    mUIImplementation.removeSubviewsFromContainerWithID(containerTag);
  }

  /**
   * Determines the location on screen, width, and height of the given view and returns the values
   * via an async callback.
   */
  @ReactMethod
  public void measure(int reactTag, Callback callback) {
    mUIImplementation.measure(reactTag, callback);
  }

  /**
   * Determines the location on screen, width, and height of the given view relative to the device
   * screen and returns the values via an async callback. This is the absolute position including
   * things like the status bar
   */
  @ReactMethod
  public void measureInWindow(int reactTag, Callback callback) {
    mUIImplementation.measureInWindow(reactTag, callback);
  }

  /**
   * Measures the view specified by tag relative to the given ancestorTag. This means that the
   * returned x, y are relative to the origin x, y of the ancestor view. Results are stored in the
   * given outputBuffer. We allow ancestor view and measured view to be the same, in which case the
   * position always will be (0, 0) and method will only measure the view dimensions.
   *
   * <p>NB: Unlike {@link #measure}, this will measure relative to the view layout, not the visible
   * window which can cause unexpected results when measuring relative to things like ScrollViews
   * that can have offset content on the screen.
   */
  @ReactMethod
  public void measureLayout(
      int tag, int ancestorTag, Callback errorCallback, Callback successCallback) {
    mUIImplementation.measureLayout(tag, ancestorTag, errorCallback, successCallback);
  }

  /**
   * Like {@link #measure} and {@link #measureLayout} but measures relative to the immediate parent.
   *
   * <p>NB: Unlike {@link #measure}, this will measure relative to the view layout, not the visible
   * window which can cause unexpected results when measuring relative to things like ScrollViews
   * that can have offset content on the screen.
   *
   * @deprecated This method will not be part of Fabric.
   */
  @ReactMethod
  @Deprecated
  public void measureLayoutRelativeToParent(
      int tag, Callback errorCallback, Callback successCallback) {
    mUIImplementation.measureLayoutRelativeToParent(tag, errorCallback, successCallback);
  }

  /**
   * Find the touch target child native view in the supplied root view hierarchy, given a react
   * target location.
   *
   * <p>This method is currently used only by Element Inspector DevTool.
   *
   * @param reactTag the tag of the root view to traverse
   * @param point an array containing both X and Y target location
   * @param callback will be called if with the identified child view react ID, and measurement
   *     info. If no view was found, callback will be invoked with no data.
   */
  @ReactMethod
  public void findSubviewIn(
      final int reactTag, final ReadableArray point, final Callback callback) {
    mUIImplementation.findSubviewIn(
        reactTag,
        Math.round(PixelUtil.toPixelFromDIP(point.getDouble(0))),
        Math.round(PixelUtil.toPixelFromDIP(point.getDouble(1))),
        callback);
  }

  /**
   * Check if the first shadow node is the descendant of the second shadow node
   *
   * @deprecated This method will not be part of Fabric.
   */
  @ReactMethod
  @Deprecated
  public void viewIsDescendantOf(
      final int reactTag, final int ancestorReactTag, final Callback callback) {
    mUIImplementation.viewIsDescendantOf(reactTag, ancestorReactTag, callback);
  }

  @ReactMethod
  public void setJSResponder(int reactTag, boolean blockNativeResponder) {
    mUIImplementation.setJSResponder(reactTag, blockNativeResponder);
  }

  @ReactMethod
  public void clearJSResponder() {
    mUIImplementation.clearJSResponder();
  }

  @ReactMethod
  public void dispatchViewManagerCommand(
      int reactTag, Dynamic commandId, @Nullable ReadableArray commandArgs) {
    // TODO: this is a temporary approach to support ViewManagerCommands in Fabric until
    // the dispatchViewManagerCommand() method is supported by Fabric JS API.
    if (commandId.getType() == ReadableType.Number) {
      final int commandIdNum = commandId.asInt();
      UIManagerHelper.getUIManager(
              getReactApplicationContext(), ViewUtil.getUIManagerType(reactTag))
          .dispatchCommand(reactTag, commandIdNum, commandArgs);
    } else if (commandId.getType() == ReadableType.String) {
      final String commandIdStr = commandId.asString();
      UIManagerHelper.getUIManager(
              getReactApplicationContext(), ViewUtil.getUIManagerType(reactTag))
          .dispatchCommand(reactTag, commandIdStr, commandArgs);
    }
  }

  /** Deprecated, use {@link #dispatchCommand(int, String, ReadableArray)} instead. */
  @Deprecated
  @Override
  public void dispatchCommand(int reactTag, int commandId, @Nullable ReadableArray commandArgs) {
    mUIImplementation.dispatchViewManagerCommand(reactTag, commandId, commandArgs);
  }

  @Override
  public void dispatchCommand(int reactTag, String commandId, @Nullable ReadableArray commandArgs) {
    mUIImplementation.dispatchViewManagerCommand(reactTag, commandId, commandArgs);
  }

  /** @deprecated use {@link SoundManager#playTouchSound()} instead. */
  @ReactMethod
  @Deprecated
  public void playTouchSound() {
    AudioManager audioManager =
        (AudioManager) getReactApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
    }
  }

  /**
   * Show a PopupMenu.
   *
   * @param reactTag the tag of the anchor view (the PopupMenu is displayed next to this view); this
   *     needs to be the tag of a native view (shadow views can not be anchors)
   * @param items the menu items as an array of strings
   * @param error will be called if there is an error displaying the menu
   * @param success will be called with the position of the selected item as the first argument, or
   *     no arguments if the menu is dismissed
   */
  @ReactMethod
  public void showPopupMenu(int reactTag, ReadableArray items, Callback error, Callback success) {
    mUIImplementation.showPopupMenu(reactTag, items, error, success);
  }

  @ReactMethod
  public void dismissPopupMenu() {
    mUIImplementation.dismissPopupMenu();
  }

  /**
   * LayoutAnimation API on Android is currently experimental. Therefore, it needs to be enabled
   * explicitly in order to avoid regression in existing application written for iOS using this API.
   *
   * <p>Warning : This method will be removed in future version of React Native, and layout
   * animation will be enabled by default, so always check for its existence before invoking it.
   *
   * <p>TODO(9139831) : remove this method once layout animation is fully stable.
   *
   * @param enabled whether layout animation is enabled or not
   */
  @ReactMethod
  public void setLayoutAnimationEnabledExperimental(boolean enabled) {
    mUIImplementation.setLayoutAnimationEnabledExperimental(enabled);
  }

  /**
   * Configure an animation to be used for the native layout changes, and native views creation. The
   * animation will only apply during the current batch operations.
   *
   * <p>TODO(7728153) : animating view deletion is currently not supported.
   *
   * @param config the configuration of the animation for view addition/removal/update.
   * @param success will be called when the animation completes, or when the animation get
   *     interrupted. In this case, callback parameter will be false.
   * @param error will be called if there was an error processing the animation
   */
  @ReactMethod
  public void configureNextLayoutAnimation(ReadableMap config, Callback success, Callback error) {
    mUIImplementation.configureNextLayoutAnimation(config, success);
  }

  /**
   * 为了实现类javadoc中提到的事务要求，我们只在完成了一批JS->Java调用之后，才将UI更改提交给实际的视图层次结构。
   * 我们知道这是安全的，因为所有的JS->Java的调用都是由Java->JS触发的（例如，一个触摸时间的传递或'renderApplication'
   * 的执行）都以一个JS->Java事务结束。
   *
   * <p>更好的方法是当UI事务完成时，让JS显式的向这个模块发送信号。但现在，iOSJ就是这么做的，我们应该同时更新JS和本地
   * 代码并做这个改变。
   *
   * <p>TODO(5279396): Make JS UI library explicitly notify the native UI module of the end of a UI
   * transaction using a standard native call
   */
  @Override
  public void onBatchComplete() {
    int batchId = mBatchId;
    mBatchId++;

    SystraceMessage.beginSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE, "onBatchCompleteUI")
        .arg("BatchId", batchId)
        .flush();
    for (UIManagerModuleListener listener : mListeners) {
      listener.willDispatchViewUpdates(this);
    }
    try {
      mUIImplementation.dispatchViewUpdates(batchId);
    } finally {
      Systrace.endSection(Systrace.TRACE_TAG_REACT_JAVA_BRIDGE);
    }
  }

  public void setViewHierarchyUpdateDebugListener(
      @Nullable NotThreadSafeViewHierarchyUpdateDebugListener listener) {
    mUIImplementation.setViewHierarchyUpdateDebugListener(listener);
  }

  public EventDispatcher getEventDispatcher() {
    return mEventDispatcher;
  }

  @ReactMethod
  public void sendAccessibilityEvent(int tag, int eventType) {
    mUIImplementation.sendAccessibilityEvent(tag, eventType);
  }

  /**
   * Schedule a block to be executed on the UI thread. Useful if you need to execute view logic
   * after all currently queued view updates have completed.
   *
   * @param block that contains UI logic you want to execute.
   *     <p>Usage Example:
   *     <p>UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
   *     uiManager.addUIBlock(new UIBlock() { public void execute (NativeViewHierarchyManager nvhm)
   *     { View view = nvhm.resolveView(tag); // ...execute your code on View (e.g. snapshot the
   *     view) } });
   */
  public void addUIBlock(UIBlock block) {
    mUIImplementation.addUIBlock(block);
  }

  /**
   * Schedule a block to be executed on the UI thread. Useful if you need to execute view logic
   * before all currently queued view updates have completed.
   *
   * @param block that contains UI logic you want to execute.
   */
  public void prependUIBlock(UIBlock block) {
    mUIImplementation.prependUIBlock(block);
  }

  public void addUIManagerListener(UIManagerModuleListener listener) {
    mListeners.add(listener);
  }

  public void removeUIManagerListener(UIManagerModuleListener listener) {
    mListeners.remove(listener);
  }

  /**
   * Given a reactTag from a component, find its root node tag, if possible. Otherwise, this will
   * return 0. If the reactTag belongs to a root node, this will return the same reactTag.
   *
   * @param reactTag the component tag
   * @return the rootTag
   */
  public int resolveRootTagFromReactTag(int reactTag) {
    return ViewUtil.isRootTag(reactTag)
        ? reactTag
        : mUIImplementation.resolveRootTagFromReactTag(reactTag);
  }

  /** Dirties the node associated with the given react tag */
  public void invalidateNodeLayout(int tag) {
    ReactShadowNode node = mUIImplementation.resolveShadowNode(tag);
    if (node == null) {
      FLog.w(
          ReactConstants.TAG,
          "Warning : attempted to dirty a non-existent react shadow node. reactTag=" + tag);
      return;
    }
    node.dirty();
    mUIImplementation.dispatchViewUpdates(-1);
  }

  /**
   * Updates the styles of the {@link ReactShadowNode} based on the Measure specs received by
   * parameters.
   */
  public void updateRootLayoutSpecs(
      final int rootViewTag, final int widthMeasureSpec, final int heightMeasureSpec) {
    ReactApplicationContext reactApplicationContext = getReactApplicationContext();
    reactApplicationContext.runOnNativeModulesQueueThread(
        new GuardedRunnable(reactApplicationContext) {
          @Override
          public void runGuarded() {
            mUIImplementation.updateRootView(rootViewTag, widthMeasureSpec, heightMeasureSpec);
            mUIImplementation.dispatchViewUpdates(-1);
          }
        });
  }

  /** Listener that drops the CSSNode pool on low memory when the app is backgrounded. */
  private class MemoryTrimCallback implements ComponentCallbacks2 {

    @Override
    public void onTrimMemory(int level) {
      if (level >= TRIM_MEMORY_MODERATE) {
        YogaNodePool.get().clear();
      }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {}

    @Override
    public void onLowMemory() {}
  }

  public View resolveView(int tag) {
    UiThreadUtil.assertOnUiThread();
    return mUIImplementation
        .getUIViewOperationQueue()
        .getNativeViewHierarchyManager()
        .resolveView(tag);
  }
}
