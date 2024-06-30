package com.littlekai.heneikenobjdetection.helper;

import android.graphics.RectF;

public class BoxOverlap {
    public static boolean isPartiallyOverlapping(RectF rectF1, RectF rectF2) {
        // Check if one of the rectangles is completely inside the other
        if (rectF1.contains(rectF2) || rectF2.contains(rectF1)) {
            return true;
        }

        if (rectF1.left <= rectF2.right && rectF1.right > rectF2.left) {
            return true;
        }

        // Check for overlapping on the top side
        if (rectF1.top <= rectF2.bottom && rectF1.bottom > rectF2.top) {
            return true;
        }

        // Check for overlapping on the right side
        if (rectF1.right >= rectF2.left && rectF1.left < rectF2.right) {
            return true;
        }

        // Check for overlapping on the bottom side
        if (rectF1.bottom >= rectF2.top && rectF1.top < rectF2.bottom) {
            return true;
        }

        // No overlapping found
        return false;

        // Check if any of the rectangle's corners are inside the other rectangle
//        return isPointInside(rectF1.left, rectF1.top, rectF2) ||
//                isPointInside(rectF1.left, rectF1.bottom, rectF2) ||
//                isPointInside(rectF1.right, rectF1.top, rectF2) ||
//                isPointInside(rectF1.right, rectF1.bottom, rectF2);
    }

    private static boolean isPointInside(float x, float y, RectF rectF) {
        return rectF.left <= x && x <= rectF.right &&
                rectF.top <= y && y <= rectF.bottom;
    }

    public static String checkContainment(RectF rectF1, RectF rectF2) {
        double area1 = rectF1.width() * rectF1.height();
        double area2 = rectF2.width() * rectF2.height();

        if (isPartiallyOverlapping(rectF1, rectF2)) {
            if (area1 > area2) {
                return "Obj1 is larger and contains Obj2";
            } else if (area1 < area2) {
                return "Obj2 is larger and contains Obj1";
            } else {
                return "Obj1 and Obj2 have the same size and overlap";
            }
        } else {
            return "Obj1 and Obj2 do not overlap";
        }
    }

}
