package com.zedfalcon.guilds.helpers;

import javax.annotation.Nullable;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Geometry {
//    public static int orientation(Point p, Point q, Point r) {
//        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
//
//        if (val == 0) return 0;
//        return (val > 0) ? 1 : 2;
//
//    }

    @Nullable
    public static List<Point> convexHull(List<Point> points) {
        System.out.println("convexHull?: " + points);
        if (points.size() < 3) return null;

        List<Point> hull = new ArrayList<>();
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

    public static Set<Point> findAllPointsWithinPolygonInclusive(List<Point> polygonPoints) {
        Traversal<Point> traversal = new Traversal<>() {
            @Override
            protected Set<Point> getSuccessors(Point point) {
                return getAdjacentPoints(point).stream()
                        .filter((p) -> !super.visited.contains(p))
                        .filter((p) -> pointWithinPolygonInclusive(p, polygonPoints)).collect(Collectors.toSet());
            }
        };
        traversal.addToVisit(polygonPoints.get(0));
        traversal.traverse();

        return traversal.getVisited();
    }

    public static Set<Point> getAdjacentPoints(Point point) {
        return Set.of(
                new Point(point.x + 1, point.y),
                new Point(point.x - 1, point.y),
                new Point(point.x, point.y + 1),
                new Point(point.x, point.y - 1)
        );
    }

//    public static boolean pointWithinPolygonInclusive(Point toTest, List<Point> polygonPoint) {
//        boolean isWithin = false;
//        int i;
//        int j;
//        for (i = 0, j = polygonPoint.size() - 1; i < polygonPoint.size(); j = i++) {
//            if ((polygonPoint.get(i).y >= toTest.y) != (polygonPoint.get(j).y >= toTest.y)
//                    && (toTest.x <= (polygonPoint.get(j).x - polygonPoint.get(i).x)
//                    * (toTest.y - polygonPoint.get(i).y)
//                    / (polygonPoint.get(j).y - polygonPoint.get(i).y)
//                    + polygonPoint.get(i).x)) {
//                isWithin = !isWithin;
//            }
//        }
//        return isWithin;
//    }

    public static boolean pointWithinPolygonInclusive(Point p, List<Point> polygon) {
        // There must be at least 3 vertices in polygon[]
        int n = polygon.size();
        if (n < 3) {
            return false;
        }

        // Create a point for line segment from p to infinite
        // TODO no...
        int INF = 1000000;
        Point extreme = new Point(INF, p.y);

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

    static boolean onSegment(Point p, Point q, Point r) {
        return q.x <= Math.max(p.x, r.x) &&
                q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) &&
                q.y >= Math.min(p.y, r.y);
    }

    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x)
                - (q.x - p.x) * (r.y - q.y);

        if (val == 0) {
            return 0;
        }
        return (val > 0) ? 1 : 2;
    }

    static boolean doIntersect(Point p1, Point q1, Point p2, Point q2) {
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
