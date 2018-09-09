package com.example.admin.healthyslife_android.utils;

import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * This class is used to process images.
 *
 * @author wu jingji
 */
public class ImageUtil {

    private static final String TAG = "ImageUtil";

    private static int decodeYUV420SPtoRedSum(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) {
            return 0;
        }

        final int frameSize = width * height;

        int sum = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int red = getRed(y, u, v);
                sum += red;
            }
        }
        return sum;
    }

    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     *
     * @param yuv420sp Byte array representing a yuv420sp image
     * @param width    Width of the image.
     * @param height   Height of the image.
     * @return int representing the average amount of red in the image.
     */
    public static int decodeYUV420SPtoRedAvg(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) {
            return 0;
        }

        final int frameSize = width * height;

        int sum = decodeYUV420SPtoRedSum(yuv420sp, width, height);
        return (sum / frameSize);
    }

    private static int getRed(int y, int u, int v) {
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        if (r < 0) {
            r = 0;
        } else if (r > 262143) {
            r = 262143;
        }
        if (g < 0) {
            g = 0;
        } else if (g > 262143) {
            g = 262143;
        }
        if (b < 0) {
            b = 0;
        } else if (b > 262143) {
            b = 262143;
        }

        int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        return (pixel >> 16) & 0xff;
    }

    /**
     * Given a yuv420sp image, determine the average amount of red in the image.
     * Note: returns 0 if the image is NULL.
     *
     * @param image a yuv420sp image
     * @return int representing the average amount of red in the image
     */
    public static int decodeYUV420SPtoRedAvg(Image image) {
        if (image == null) {
            Log.i(TAG, "decodeYUV420SPtoRedAvg: decoded image is NULL");
            return 0;
        }
        int height = image.getHeight();
        int width = image.getWidth();

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int sum = 0;
        for (int j = 0; j < height; ++j) {
            int u = 0;
            int v = 0;
            for (int i = 0; i < width; ++i) {
                int y = (0xff & yBuffer.get()) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    u = (0xff & uBuffer.get(i + (j >> 1) * width)) - 128;
                    v = (0xff & vBuffer.get(i + (j >> 1) * width)) - 128;
                }
                int red = getRed(y, u, v);
                sum += red;
            }
        }
        return (sum / (height * width));
    }

    /**
     * Get image info string
     *
     * @param image image
     * @return image info
     */
    public static String imageInfo(Image image) {
        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];

        int yPixelStride = yPlane.getPixelStride();
        int yRowStride = yPlane.getRowStride();
        int uPixelStride = uPlane.getPixelStride();
        int uRowStride = uPlane.getRowStride();
        int vPixelStride = vPlane.getPixelStride();
        int vRowStride = vPlane.getRowStride();

        int yCount = yPlane.getBuffer().remaining();
        int uCount = uPlane.getBuffer().remaining();
        int vCount = vPlane.getBuffer().remaining();
        return String.format(Locale.CHINA,
                "h: %d w: %d | y: %d,%d,%d | u: %d,%d,%d | v: %d,%d,%d",
                image.getHeight(), image.getWidth(),
                yPixelStride, yRowStride, yCount,
                uPixelStride, uRowStride, uCount,
                vPixelStride, vRowStride, vCount);
    }
}
