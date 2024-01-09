package com.zedfalcon.guilds.helpers;

import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

    @Nullable
    public static List<Vec2f> convexHull(List<Vec2f> points) {
        if (points.size() < 3) return null;

        List<Vec2f> hull = new ArrayList<>();
        int l = 0;
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).x < points.get(l).x) {
                l = i;
            }
        }
        int p = l, q;
        do {
            hull.add(points.get(p));
            q = (p + 1) % points.size();
            for (int i = 0; i < points.size(); i++) {
                if (orientation(points.get(p), points.get(i), points.get(q)) == 2) {
                    q = i;
                }
            }
            p = q;

        } while (p != l);
        return hull;
    }

    public static boolean pointWithinPolygonInclusive(Vec2f p, List<Vec2f> polygon) {
        // There must be at least 3 vertices in polygon[]
        int n = polygon.size();
        if (n < 3) {
            return false;
        }

        // Create a point for line segment from p to infinite
        // TODO no...
        int INF = 1000000;
        Vec2f extreme = new Vec2f(INF, p.y);

        // Count intersections of the above line
        // with sides of polygon
        int count = 0, i = 0;
        do {
            int next = (i + 1) % n;

            // Check if the line segment from 'p' to
            // 'extreme' intersects with the line
            // segment from 'polygon[i]' to 'polygon[next]'
            if (doIntersect(polygon.get(i), polygon.get(next), p, extreme)) {
                // If the point 'p' is collinear with line
                // segment 'i-next', then check if it lies
                // on segment. If it lies, return true, otherwise false
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0) {
                    return onSegment(polygon.get(i), p, polygon.get(next));
                }

                count++;
            }
            i = next;
        } while (i != 0);

        return (count % 2 == 1);
    }

    static boolean onSegment(Vec2f p, Vec2f q, Vec2f r) {
        return q.x <= Math.max(p.x, r.x) &&
                q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) &&
                q.y >= Math.min(p.y, r.y);
    }

    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(Vec2f p, Vec2f q, Vec2f r) {
        float val = (q.y - p.y) * (r.x - q.x)
                - (q.x - p.x) * (r.y - q.y);

        if (val == 0) {
            return 0;
        }
        return (val > 0) ? 1 : 2;
    }

    static boolean doIntersect(Vec2f p1, Vec2f q1, Vec2f p2, Vec2f q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4) {
            return true;
        } else if (o1 == 0 && onSegment(p1, p2, q1)) {
            return true;
        } else if (o2 == 0 && onSegment(p1, q2, q1)) {
            return true;
        } else if (o3 == 0 && onSegment(p2, p1, q2)) {
            return true;
        } else return o4 == 0 && onSegment(p2, q1, q2);
    }
}
