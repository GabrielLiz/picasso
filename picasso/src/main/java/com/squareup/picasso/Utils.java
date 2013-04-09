package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import java.util.List;

final class Utils {

  private Utils() {
    // No instances.
  }

  static void checkNotMain() {
    if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
      throw new IllegalStateException("Method call should not happen from the main thread.");
    }
  }

  static String createKey(Request request) {
    return createKey(request.path, request.transformations, request.metrics);
  }

  static String createKey(String path, List<Transformation> transformations,
      RequestMetrics metrics) {
    long start = System.nanoTime();
    StringBuilder builder = new StringBuilder();
    builder.append(path);

    if (transformations != null && !transformations.isEmpty()) {
      if (transformations.size() == 1) {
        builder.append('|');
        builder.append(transformations.get(0).toString());
      } else {
        for (Transformation transformation : transformations) {
          builder.append('|');
          builder.append(transformation.toString());
        }
      }
    }

    if (metrics != null) {
      metrics.keyCreationTime = System.nanoTime() - start;
    }

    // TODO Support bitmap options?
    return builder.toString();
  }

  private static final ThreadLocal<Paint> DEBUG_PAINT = new ThreadLocal<Paint>() {
    @Override protected Paint initialValue() {
      return new Paint();
    }
  };

  static Bitmap applyDebugColorFilter(Bitmap source, int loadedFrom) {
    Paint debugPaint = DEBUG_PAINT.get();
    Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

    Canvas canvas = new Canvas(output);
    canvas.drawBitmap(source, 0, 0, null);
    canvas.rotate(45);

    // Draw a white square for the indicator border.
    debugPaint.setColor(0xFFFFFFFF);
    canvas.drawRect(0, -20, 15, 20, debugPaint);

    // Draw a slightly smaller square for the indicator color.
    debugPaint.setColor(RequestMetrics.getColorCodeForCacheHit(loadedFrom));
    canvas.drawRect(0, -18, 13, 18, debugPaint);

    // Do not recycle source bitmap here. This is the image that is stored inside the cache and can
    // be used again when debugging is off.

    return output;
  }
}
