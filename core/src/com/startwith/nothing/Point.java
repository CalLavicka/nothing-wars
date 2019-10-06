package com.startwith.nothing;

import java.util.Objects;

public final class Point {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    int x, y;
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
