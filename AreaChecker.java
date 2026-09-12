import java.util.function.Predicate;

public class AreaChecker {

    // 1. Record for 2D points
    public record Point(double x, double y) {
        public static Point p(double x, double y) {
            return new Point(x, y);
        }
    }

    // 2. Record for 2D lines
    public record Line(Point p1, Point p2) {
        public double compareTo(Point p) {
            return Math.signum((p.x() - p1.x()) * (p2.y() - p1.y()) - (p.y() - p1.y()) * (p2.x() - p1.x()));
        }
    }

    // 3. Area interface
    @FunctionalInterface
    public interface Area {
        Predicate<Point> test(boolean includeBorders);
    }

    // --- Factory methods for geometric shapes ---

    public static Area circle(Point center, double radius) {
        return includeBorders -> point -> {
            double distSq = Math.pow(point.x() - center.x(), 2) + Math.pow(point.y() - center.y(), 2);
            double rSq = radius * radius;
            return includeBorders ? distSq <= rSq : distSq < rSq;
        };
    }

    public static Area rectangle(Point leftLower, Point rightUpper) {
        return includeBorders -> point -> {
            boolean xOk = includeBorders 
                ? (point.x() >= leftLower.x() && point.x() <= rightUpper.x())
                : (point.x() > leftLower.x() && point.x() < rightUpper.x());
            boolean yOk = includeBorders 
                ? (point.y() >= leftLower.y() && point.y() <= rightUpper.y())
                : (point.y() > leftLower.y() && point.y() < rightUpper.y());
            return xOk && yOk;
        };
    }

    public static Area halfPlane(Line line, int sign) {
        return includeBorders -> point -> {
            double cmp = line.compareTo(point);
            return (cmp == sign) || (includeBorders && cmp == 0);
        };
    }

    public static void main(String[] args) {
        // Task 1: Circle and half-planes
        Predicate<Point> task1 = point -> {
            var bigCircle = circle(Point.p(0, 0), 2.0).test(true);
            var lineAB = halfPlane(new Line(Point.p(-4, -1), Point.p(0, 1)), -1).test(true);
            var oyRight = halfPlane(new Line(Point.p(0, 0), Point.p(0, 1)), 1).test(true);

            return bigCircle.test(point) && lineAB.test(point) && oyRight.test(point);
        };

        // Task 2: Rectangle
        Predicate<Point> task2 = point -> {
            var rect = rectangle(Point.p(-1, 1.5), Point.p(1.75, 3)).test(true);
            return point.x() >= 0 && rect.test(point);
        };

        // Testing
        System.out.println("=== TESTS ===");
        Point p1 = Point.p(0.5, -0.5);
        Point p2 = Point.p(-3, -1);
        System.out.println("Task 1 (0.5, -0.5): " + task1.test(p1)); // true
        System.out.println("Task 1 (-3, -1):   " + task1.test(p2)); // false

        Point p3 = Point.p(1.0, 2.0);
        Point p4 = Point.p(-0.5, 2.0);
        System.out.println("Task 2 (1.0, 2.0):  " + task2.test(p3)); // true
        System.out.println("Task 2 (-0.5, 2.0): " + task2.test(p4)); // false
    }
}
/*
AreaChecker is a Java-based, single-file geometry framework that determines the relative position of 2D points to composite areas using lambdas and functional interfaces.
Key Features:
Simple Data Structures: 
Uses Java record types to represent 2D points (Point) and lines (Line).
Geometric Shapes (Primitive Areas): 
Supports circles (circle), rectangles (rectangle), and half-planes (halfPlane) via static factory methods.
Boundary Handling: 
Includes an option to specify whether boundary lines/edges are included in the region check (includeBorders).
Composite Regions (Lambda Chaining): 
Predicate<Point> logical conditions from primitive shapes can be freely combined (e.g., AND, OR operations) to define complex geometric regions.
*/

