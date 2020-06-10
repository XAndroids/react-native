/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.react.views.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import java.util.HashMap;
import java.util.Map;

/**
 * 这个类负责加载和缓存Typeface对象。它首先会尝试从assets/fonts文件夹中加载字体，如果它在这个文件夹中找不到合适的
 * Typeface，就会选择最匹配的系统字体。支持的自定义字体扩展名是.ttf和.otf。对于每个字体族，都支持bold，italic和
 * bold_italic。给定一个"family"字体族，在assts/fonts文件夹下需要family.ttf(.otf) family_bold.ttf(.otf)
 * family_italic.ttf(otf)和family_bold_italic.ttf(.otf)
 */
public class ReactFontManager {

  private static final String[] EXTENSIONS = {"", "_bold", "_italic", "_bold_italic"};
  private static final String[] FILE_EXTENSIONS = {".ttf", ".otf"};
  private static final String FONTS_ASSET_PATH = "fonts/";

  //单例
  private static ReactFontManager sReactFontManagerInstance;

  private final Map<String, FontFamily> mFontCache;
  private final Map<String, Typeface> mCustomTypefaceCache;

  private ReactFontManager() {
    mFontCache = new HashMap<>();
    mCustomTypefaceCache = new HashMap<>();
  }

  public static ReactFontManager getInstance() {
    if (sReactFontManagerInstance == null) {
      sReactFontManagerInstance = new ReactFontManager();
    }
    return sReactFontManagerInstance;
  }

  public @Nullable Typeface getTypeface(
      String fontFamilyName, int style, AssetManager assetManager) {
    return getTypeface(fontFamilyName, style, 0, assetManager);
  }

  public @Nullable Typeface getTypeface(
      String fontFamilyName, int style, int weight, AssetManager assetManager) {
    if (mCustomTypefaceCache.containsKey(fontFamilyName)) {
      Typeface typeface = mCustomTypefaceCache.get(fontFamilyName);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && weight >= 100 && weight <= 1000) {
        return Typeface.create(typeface, weight, (style & Typeface.ITALIC) != 0);
      }
      return Typeface.create(typeface, style);
    }

    FontFamily fontFamily = mFontCache.get(fontFamilyName);
    if (fontFamily == null) {
      fontFamily = new FontFamily();
      mFontCache.put(fontFamilyName, fontFamily);
    }

    Typeface typeface = fontFamily.getTypeface(style);
    if (typeface == null) {
      typeface = createTypeface(fontFamilyName, style, assetManager);
      if (typeface != null) {
        fontFamily.setTypeface(style, typeface);
      }
    }

    return typeface;
  }

  /**
   * 这个方法允许你从res/font文件夹使用提供的font family名称加载自定义字体。字体可能是一个.ttf，.otf或者XML
   * (https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml)。为了支持多个字体样式
   * 或者字重，你必须以XML格式提供字体。
   *
   * ReactFontManager.getInstance().addCustomFont(this, "Srisakdi", R.font.srisakdi);
   */
  public void addCustomFont(@NonNull Context context, @NonNull String fontFamily, int fontId) {
    Typeface font = ResourcesCompat.getFont(context, fontId);
    if (font != null) {
      mCustomTypefaceCache.put(fontFamily, font);
    }
  }

  /**
   * Add additional font family, or replace the exist one in the font memory cache.
   *
   * @param style
   * @see {@link Typeface#DEFAULT}
   * @see {@link Typeface#BOLD}
   * @see {@link Typeface#ITALIC}
   * @see {@link Typeface#BOLD_ITALIC}
   */
  public void setTypeface(String fontFamilyName, int style, Typeface typeface) {
    if (typeface != null) {
      FontFamily fontFamily = mFontCache.get(fontFamilyName);
      if (fontFamily == null) {
        fontFamily = new FontFamily();
        mFontCache.put(fontFamilyName, fontFamily);
      }
      fontFamily.setTypeface(style, typeface);
    }
  }

  private static @Nullable Typeface createTypeface(
      String fontFamilyName, int style, AssetManager assetManager) {
    String extension = EXTENSIONS[style];
    for (String fileExtension : FILE_EXTENSIONS) {
      String fileName =
          new StringBuilder()
              .append(FONTS_ASSET_PATH)
              .append(fontFamilyName)
              .append(extension)
              .append(fileExtension)
              .toString();
      try {
        return Typeface.createFromAsset(assetManager, fileName);
      } catch (RuntimeException e) {
        // unfortunately Typeface.createFromAsset throws an exception instead of returning null
        // if the typeface doesn't exist
      }
    }

    return Typeface.create(fontFamilyName, style);
  }

  private static class FontFamily {

    private SparseArray<Typeface> mTypefaceSparseArray;

    private FontFamily() {
      mTypefaceSparseArray = new SparseArray<>(4);
    }

    public Typeface getTypeface(int style) {
      return mTypefaceSparseArray.get(style);
    }

    public void setTypeface(int style, Typeface typeface) {
      mTypefaceSparseArray.put(style, typeface);
    }
  }
}
