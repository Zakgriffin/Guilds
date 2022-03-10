package com.zedfalcon.guilds.helpers;

import javax.annotation.Nullable;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Geometry {

    public static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    @Nullable
    public static List<Point> convexHull(List<Point> points) {
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

    public static Set<Point> findAllPointsWithinPolygon(List<Point> polygonPoints) {
        Traversal<Point> traversal = new Traversal<>() {
            @Override
            protected Set<Point> getSuccessors(Point point) {
                return getAdjacentPoints(point).stream()
                        .filter((p) -> pointWithinPolygon(p, polygonPoints)).collect(Collectors.toSet());
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

    public static boolean pointWithinPolygon(Point toTest, List<Point> polygonPoint) {
        boolean isWithin = false;
        int i;
        int j;
        for (i = 0, j = polygonPoint.size() - 1; i < polygonPoint.size(); j = i++) {
            if ((polygonPoint.get(i).y > toTest.y) != (polygonPoint.get(j).y > toTest.y)
                    && (toTest.x < (polygonPoint.get(j).x - polygonPoint.get(i).x)
                    * (toTest.y - polygonPoint.get(i).y)
                    / (polygonPoint.get(j).y - polygonPoint.get(i).y)
                    + polygonPoint.get(i).x)) {
                isWithin = !isWithin;
            }
        }
        return isWithin;
    }
}
