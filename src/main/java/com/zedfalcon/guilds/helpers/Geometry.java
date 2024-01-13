package com.zedfalcon.guilds.helpers;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Geometry {
    public static boolean pointWithinPolygonInclusive(BlockPos test, List<BlockPos> points) {
        boolean result = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if (
                    (points.get(i).getZ() > test.getZ()) != (points.get(j).getZ() > test.getZ()) &&
                            (test.getX() < (points.get(j).getX() - points.get(i).getX()) * (test.getZ() - points.get(i).getZ()) / (points.get(j).getZ() - points.get(i).getZ()) + points.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }

    public static int orientation(BlockPos p, BlockPos q, BlockPos r) {
        int val = (q.getZ() - p.getZ()) * (r.getX() - q.getX()) -
                (q.getX() - p.getX()) * (r.getZ() - q.getZ());

        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    public static List<BlockPos> convexHull(List<BlockPos> points) {
        int n = points.size();
        List<BlockPos> hull = new ArrayList<>();
        if (n < 3) return hull;

        int l = 0;
        for (int i = 1; i < n; i++)
            if (points.get(i).getX() < points.get(l).getX())
                l = i;

        int p = l, q;
        do {
            hull.add(points.get(p));
            q = (p + 1) % n;
            for (int i = 0; i < n; i++) {
                if (orientation(points.get(p), points.get(i), points.get(q)) == 2)
                    q = i;
            }
            p = q;
        } while (p != l);

        return hull;
    }
}
