/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.uimanager;

import androidx.annotation.Nullable;
import com.facebook.yoga.YogaAlign;
import com.facebook.yoga.YogaBaselineFunction;
import com.facebook.yoga.YogaDirection;
import com.facebook.yoga.YogaDisplay;
import com.facebook.yoga.YogaFlexDirection;
import com.facebook.yoga.YogaJustify;
import com.facebook.yoga.YogaMeasureFunction;
import com.facebook.yoga.YogaNode;
import com.facebook.yoga.YogaOverflow;
import com.facebook.yoga.YogaPositionType;
import com.facebook.yoga.YogaValue;
import com.facebook.yoga.YogaWrap;

/**
 * 表示React节点虚拟树的基本节点类。Shadow节点主要用于布局，因此它继承了{@link YogaNode}来允许这样做。它们还帮助
 * 处理react-based的view的所有布局节点{@link YogaNode}的公共基子类。它通过继承{@link YogaNode}添加额外的功能。
 *
 * <p>这个类的实例通过@{link UIManagerModule}从JS接收属性更新。子类可以使用{@link #updateShadowNode}来持久化
 * 节点实例中与特定视图类型对应的一些已更新的字段。
 *
 * <p>{@link ReactShadowNode}的子类应该只从对应于某种类型的native 视图的{@link ViewManager}创建。他们只有从
 * JS线程被更新和访问。如果需要的话，{@link ViewManager}的子类可以选择使用基类{@link ReactShadowNode}或者自定
 * 义子类。
 *
 * {@link ReactShadowNode}节点的主要用例是计算布局。尽管这可能会被继承。一些例子请参考ARTGroupYogaNode或React
 * TextYogaNode。
 *
 * <p>This class allows for the native view hierarchy to not be an exact copy of the hierarchy
 * received from JS by keeping track of both JS children (e.g. {@link #getChildCount()} and
 * separately native children (e.g. {@link #getNativeChildCount()}). See {@link
 * NativeViewHierarchyOptimizer} for more information.
 */
public interface ReactShadowNode<T extends ReactShadowNode> {

  /**
   * Nodes that return {@code true} will be treated as "virtual" nodes. That is, nodes that are not
   * mapped into native views or Yoga nodes (e.g. nested text node). By default this method returns
   * {@code false}.
   */
  boolean isVirtual();

  /**
   * Nodes that return {@code true} will be treated as a root view for the virtual nodes tree. It
   * means that all of its descendants will be "virtual" nodes. Good example is {@code InputText}
   * view that may have children {@code Text} nodes but this whole hierarchy will be mapped to a
   * single android {@link EditText} view.
   */
  boolean isVirtualAnchor();

  /**
   * Nodes that return {@code true} will not manage (and and remove) child Yoga nodes. For example
   * {@link ReactTextInputShadowNode} or {@link ReactTextShadowNode} have child nodes, which do not
   * want Yoga to lay out, so in the eyes of Yoga it is a leaf node. Override this method in
   * subclass to enforce this requirement.
   */
  boolean isYogaLeafNode();

  /**
   * When constructing the native tree, nodes that return {@code true} will be treated as leaves.
   * Instead of adding this view's native children as subviews of it, they will be added as subviews
   * of an ancestor. In other words, this view wants to support native children but it cannot host
   * them itself (e.g. it isn't a ViewGroup).
   */
  boolean hoistNativeChildren();

  String getViewClass();

  boolean hasUpdates();

  void markUpdateSeen();

  void markUpdated();

  boolean hasUnseenUpdates();

  void dirty();

  boolean isDirty();

  void addChildAt(T child, int i);

  T removeChildAt(int i);

  int getChildCount();

  T getChildAt(int i);

  int indexOf(T child);

  void removeAndDisposeAllChildren();

  /**
   * 在计算布局之前，该方法将由{@link UIManagerModule}每批调用一次。将只对标记为更新的{@link #markUpdated()}
   * 或需要layouting(标记为{@link #dirty()})的节点调用。
   */
  void onBeforeLayout(NativeViewHierarchyOptimizer nativeViewHierarchyOptimizer);

  void updateProperties(ReactStylesDiffMap props);

  void onAfterUpdateTransaction();

  /**
   * Called after layout step at the end of the UI batch from {@link UIManagerModule}. May be used
   * to enqueue additional ui operations for the native view. Will only be called on nodes marked as
   * updated either with {@link #dirty()} or {@link #markUpdated()}.
   *
   * @param uiViewOperationQueue interface for enqueueing UI operations
   */
  void onCollectExtraUpdates(UIViewOperationQueue uiViewOperationQueue);

  /** @return true if layout (position or dimensions) changed, false otherwise. */

  /* package */ boolean dispatchUpdates(
      float absoluteX,
      float absoluteY,
      UIViewOperationQueue uiViewOperationQueue,
      NativeViewHierarchyOptimizer nativeViewHierarchyOptimizer);

  int getReactTag();

  void setReactTag(int reactTag);

  int getRootTag();

  void setRootTag(int rootTag);

  void setViewClassName(String viewClassName);

  @Nullable
  T getParent();

  // Returns the node that is responsible for laying out this node.
  @Nullable
  T getLayoutParent();

  void setLayoutParent(@Nullable T layoutParent);

  /**
   * Get the {@link ThemedReactContext} associated with this {@link ReactShadowNode}. This will
   * never change during the lifetime of a {@link ReactShadowNode} instance, but different instances
   * can have different contexts; don't cache any calculations based on theme values globally.
   */
  ThemedReactContext getThemedContext();

  void setThemedContext(ThemedReactContext themedContext);

  boolean shouldNotifyOnLayout();

  void calculateLayout();

  void calculateLayout(float width, float height);

  boolean hasNewLayout();

  void markLayoutSeen();

  /**
   * Adds a child that the native view hierarchy will have at this index in the native view
   * corresponding to this node.
   */
  void addNativeChildAt(T child, int nativeIndex);

  T removeNativeChildAt(int i);

  void removeAllNativeChildren();

  int getNativeChildCount();

  int indexOfNativeChild(T nativeChild);

  @Nullable
  T getNativeParent();

  /**
   * Sets whether this node only contributes to the layout of its children without doing any drawing
   * or functionality itself.
   */
  void setIsLayoutOnly(boolean isLayoutOnly);

  boolean isLayoutOnly();

  NativeKind getNativeKind();

  int getTotalNativeChildren();

  boolean isDescendantOf(T ancestorNode);

  /**
   * @return a {@link String} representation of the Yoga hierarchy of this {@link ReactShadowNode}
   */
  String getHierarchyInfo();

  /*
   * In some cases we need a way to specify some environmental data to shadow node
   * to improve layout (or do something similar), so {@code localData} serves these needs.
   * For example, any stateful embedded native views may benefit from this.
   * Have in mind that this data is not supposed to interfere with the state of
   * the shadow node.
   * Please respect one-directional data flow of React.
   * Use  {@link UIManagerModule#setViewLocalData} to set this property
   * (to provide local/environmental data for a shadow node) from the main thread.
   */
  void setLocalData(Object data);

  /**
   * Returns the offset within the native children owned by all layout-only nodes in the subtree
   * rooted at this node for the given child. Put another way, this returns the number of native
   * nodes (nodes not optimized out of the native tree) that are a) to the left (visited before by a
   * DFS) of the given child in the subtree rooted at this node and b) do not have a native parent
   * in this subtree (which means that the given child will be a sibling of theirs in the final
   * native hierarchy since they'll get attached to the same native parent).
   *
   * <p>Basically, a view might have children that have been optimized away by {@link
   * NativeViewHierarchyOptimizer}. Since those children will then add their native children to this
   * view, we now have ranges of native children that correspond to single unoptimized children. The
   * purpose of this method is to return the index within the native children that corresponds to
   * the **start** of the native children that belong to the given child. Also, note that all of the
   * children of a view might be optimized away, so this could return the same value for multiple
   * different children.
   *
   * <p>Example. Native children are represented by (N) where N is the no-opt child they came from.
   * If no children are optimized away it'd look like this: (0) (1) (2) (3) ... (n)
   *
   * <p>In case some children are optimized away, it might look like this: (0) (1) (1) (1) (3) (3)
   * (4)
   *
   * <p>In that case: getNativeOffsetForChild(Node 0) => 0 getNativeOffsetForChild(Node 1) => 1
   * getNativeOffsetForChild(Node 2) => 4 getNativeOffsetForChild(Node 3) => 4
   *
   * <p>getNativeOffsetForChild(Node 4) => 6
   */
  int getNativeOffsetForChild(T child);

  float getLayoutX();

  float getLayoutY();

  float getLayoutWidth();

  float getLayoutHeight();

  /** @return the x position of the corresponding view on the screen, rounded to pixels */
  int getScreenX();

  /** @return the y position of the corresponding view on the screen, rounded to pixels */
  int getScreenY();

  /** @return width corrected for rounding to pixels. */
  int getScreenWidth();

  /** @return height corrected for rounding to pixels. */
  int getScreenHeight();

  YogaDirection getLayoutDirection();

  void setLayoutDirection(YogaDirection direction);

  YogaValue getStyleWidth();

  void setStyleWidth(float widthPx);

  void setStyleWidthPercent(float percent);

  void setStyleWidthAuto();

  void setStyleMinWidth(float widthPx);

  void setStyleMinWidthPercent(float percent);

  void setStyleMaxWidth(float widthPx);

  void setStyleMaxWidthPercent(float percent);

  YogaValue getStyleHeight();

  void setStyleHeight(float heightPx);

  void setStyleHeightPercent(float percent);

  void setStyleHeightAuto();

  void setStyleMinHeight(float widthPx);

  void setStyleMinHeightPercent(float percent);

  void setStyleMaxHeight(float widthPx);

  void setStyleMaxHeightPercent(float percent);

  void setFlex(float flex);

  void setFlexGrow(float flexGrow);

  void setFlexShrink(float flexShrink);

  void setFlexBasis(float flexBasis);

  void setFlexBasisAuto();

  void setFlexBasisPercent(float percent);

  void setStyleAspectRatio(float aspectRatio);

  void setFlexDirection(YogaFlexDirection flexDirection);

  void setFlexWrap(YogaWrap wrap);

  void setAlignSelf(YogaAlign alignSelf);

  void setAlignItems(YogaAlign alignItems);

  void setAlignContent(YogaAlign alignContent);

  void setJustifyContent(YogaJustify justifyContent);

  void setOverflow(YogaOverflow overflow);

  void setDisplay(YogaDisplay display);

  void setMargin(int spacingType, float margin);

  void setMarginPercent(int spacingType, float percent);

  void setMarginAuto(int spacingType);

  float getPadding(int spacingType);

  YogaValue getStylePadding(int spacingType);

  void setDefaultPadding(int spacingType, float padding);

  void setPadding(int spacingType, float padding);

  void setPaddingPercent(int spacingType, float percent);

  void setBorder(int spacingType, float borderWidth);

  void setPosition(int spacingType, float position);

  void setPositionPercent(int spacingType, float percent);

  void setPositionType(YogaPositionType positionType);

  void setShouldNotifyOnLayout(boolean shouldNotifyOnLayout);

  void setBaselineFunction(YogaBaselineFunction baselineFunction);

  void setMeasureFunction(YogaMeasureFunction measureFunction);

  boolean isMeasureDefined();

  void dispose();

  void setMeasureSpecs(int widthMeasureSpec, int heightMeasureSpec);

  Integer getWidthMeasureSpec();

  Integer getHeightMeasureSpec();

  Iterable<? extends ReactShadowNode> calculateLayoutOnChildren();
}
