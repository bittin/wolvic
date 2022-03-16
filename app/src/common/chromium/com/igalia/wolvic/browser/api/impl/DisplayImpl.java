package com.igalia.wolvic.browser.api.impl;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.igalia.wolvic.browser.api.WDisplay;
import com.igalia.wolvic.browser.api.WResult;

import org.chromium.weblayer.CaptureScreenShotCallback;
import org.chromium.weblayer.Tab;

import kotlin.NotImplementedError;

public class DisplayImpl implements WDisplay {
    @NonNull BrowserDisplay mDisplay;
    @NonNull
    Tab mTab;
    private int mWidth = 1;
    private Surface mSurface;

    public DisplayImpl(@NonNull BrowserDisplay display, @NonNull Tab tab) {
        mDisplay = display;
        mTab = tab;
    }

    @NonNull BrowserDisplay getBrowserDisplay() {
        return mDisplay;
    }

    @Override
    public void surfaceChanged(@NonNull Surface surface, int width, int height) {
        mWidth = width;
        mDisplay.surfaceChanged(surface, width, height);
        if (mSurface != null && mSurface != surface) {
            // Workaround for what it looks like a Chrome bug.
            // When Surface is a different instance from previous one, chrome doesn't render
            // correctly to in until navigation changes.
            mTab.getNavigationController().reload();
        }
        mSurface = surface;

    }

    @Override
    public void surfaceChanged(@NonNull Surface surface, int left, int top, int width, int height) {
        surfaceChanged(surface, width, height);
    }

    @Override
    public void surfaceDestroyed() {
        mDisplay.surfaceDestroyed();
    }

    @NonNull
    @Override
    public WResult<Bitmap> capturePixels() {
        return capturePixelsWithAspectPreservingSize(mWidth);
    }

    @NonNull
    @Override
    public WResult<Bitmap> capturePixelsWithAspectPreservingSize(int width) {
        WResult<Bitmap> result = WResult.create();
        float scale = (float)width / (float)mWidth;
        mTab.captureScreenShot(scale, (bitmap, errorCode) -> {
            if (errorCode == 0) {
                result.complete(bitmap);
            } else {
                result.completeExceptionally(new RuntimeException("Error code:" + errorCode));
            }
        });
        return result;
    }

}
