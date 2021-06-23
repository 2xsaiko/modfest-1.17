package trucc.util;

import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import net.dblsaiko.qcommon.croco.Mat4;
import net.dblsaiko.qcommon.croco.Vec2;
import net.dblsaiko.qcommon.croco.Vec3;

// Class adapted from https://github.com/dulnan/catenary-curve
// (Copyright (c) 2018 Jan Hug <me@dulnan.net>, released under the MIT license)

public class Catenary {
    private static final double EPSILON = 1e-6;

    public static List<Vec3> draw(Vec3 point1, Vec3 point2, float chainLength, int segments, int iterationLimit) {
        Vec3 up = new Vec3(0.0f, -1.0f, 0.0f);
        Vec3 direction = point2.sub(point1).getNormalized();
        Vec3 planeNormal;

        if (direction.dot(up) > 0.99) {
            planeNormal = new Vec3(1.0f, 0.0f, 0.0f);
        } else {
            planeNormal = direction.cross(up).getNormalized();
        }

        Vec3 right = up.cross(planeNormal);
        Mat4 tr = new Mat4(
            right.x, up.x, 0.0f, point1.x,
            right.y, up.y, 0.0f, point1.y,
            right.z, up.z, 0.0f, point1.z,
            0.0f, 0.0f, 0.0f, 1.0f
        );
        Vec3 relPoint2 = point2.sub(point1);
        Vec2 projP2 = new Vec2(right.dot(relPoint2), up.dot(relPoint2));

        return draw(Vec2.ORIGIN, projP2, chainLength, segments, iterationLimit)
            .stream()
            .map(vec -> tr.mul(new Vec3(vec.x, vec.y, 0)))
            .toList();
    }

    public static List<Vec2> draw(Vec2 point1, Vec2 point2, float chainLength, int segments, int iterationLimit) {
        boolean isFlipped = point1.x > point2.x;
        Vec2 p1 = isFlipped ? point2 : point1;
        Vec2 p2 = isFlipped ? point1 : point2;

        float distance = p2.sub(p1).getLength();
        List<Vec2> curveData;

        // Prevent "expensive" catenary calculations if it would only result
        // in a straight line.
        if (distance < chainLength) {
            float diff = p2.x - p1.x;

            // If the distance on the x axis of both points is too small, don't
            // calculate a catenary.
            if (diff > 0.01) {
                float h = p2.x - p1.x;
                float v = p2.y - p1.y;
                double a = -getCatenaryParameter(h, v, chainLength, iterationLimit);
                double x = (a * Math.log((chainLength + v) / (chainLength - v)) - h) * 0.5f;
                double y = a * Math.cosh(x / a);
                double offsetX = p1.x - x;
                double offsetY = p1.y - y;
                curveData = getCurve(a, p1, p2, offsetX, offsetY, segments);
            } else {
                float mx = (p1.x + p2.x) * 0.5f;
                float my = (p1.y + p2.y + chainLength) * 0.5f;

                curveData = List.of(p1, new Vec2(mx, my), p2);
            }
        } else {
            curveData = List.of(p1, p2);
        }

        return curveData;
    }

    /**
     * Determines catenary parameter.
     *
     * @param h      horizontal distance of both points
     * @param v      vertical distance of both points
     * @param length the catenary length
     * @param limit  maximum amount of iterations to find parameter
     * @return the catenary parameter
     */
    private static double getCatenaryParameter(float h, float v, float length, float limit) {
        float m = MathHelper.sqrt(length * length - v * v) / h;
        double x = acosh(m) + 1;
        double prevx = -1;
        float count = 0;

        while (Math.abs(x - prevx) > EPSILON && count < limit) {
            prevx = x;
            x = x - (Math.sinh(x) - m * x) / (Math.cosh(x) - m);
            count++;
        }

        return h / (2 * x);
    }

    /**
     * Calculate the catenary curve.
     * Increasing the segments value will produce a catenary closer
     * to reality, but will require more calcluations.
     *
     * @param a        The catenary parameter.
     * @param p1       First point
     * @param p2       Second point
     * @param offsetX  The calculated offset on the x axis.
     * @param offsetY  The calculated offset on the y axis.
     * @param segments How many "parts" the chain should be made of.
     */
    private static List<Vec2> getCurve(double a, Vec2 p1, Vec2 p2, double offsetX, double offsetY, int segments) {
        List<Vec2> data = new ArrayList<>(segments + 1);
        data.add(new Vec2(p1.x, (float) (a * Math.cosh((p1.x - offsetX) / a) + offsetY)));

        float d = p2.x - p1.x;
        int length = segments - 1;

        for (int i = 0; i < length; i++) {
            float x = p1.x + d * (i + 0.5f) / length;
            double y = a * Math.cosh((x - offsetX) / a) + offsetY;
            data.add(new Vec2(x, (float) y));
        }

        data.add(new Vec2(p2.x, (float) (a * Math.cosh((p2.x - offsetX) / a) + offsetY)));

        return data;
    }

    private static double acosh(double x) {
        return Math.log(x + Math.sqrt(x * x - 1));
    }
}
