package vn.edu.fpt.pokemongame;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GameManager {
    public static int[][] BOARD;
    public static final int TILES = 36;
    public static int level = 1;
    public static ArrayList<Integer> cards = new ArrayList<>();
    private static TreeMap<Integer, ArrayList<Point>> points = new TreeMap<>();
    public static HashMap<PointPair, PointPair> validPairs = new HashMap<>();

    //randomly create cards (we can use random, but in the original game, each tile appear 4 times)
    public static void createCards(int y, int x) {
        for (int i = 1; i <= TILES; i++) {
            cards.add(i);
            cards.add(i);
            cards.add(i);
            cards.add(i);
        }
    }

    //shuffle the card positions
    public static void shuffleCards() {
        Collections.shuffle(cards);
    }

    //create a new board
    public static void createRectangle(int y, int x) {
        BOARD = new int[y][x];
    }

    //set the card for each cell in the board
    public static void populateCardsToBoard(boolean isShuffle) {
        int cardIndex = 0;
        for (int i = 0; i < BOARD.length; i++) {
            for (int j = 0; j < BOARD[0].length; j++) {
                if (isShuffle && BOARD[i][j] == 0) {
                    continue;
                }
                BOARD[i][j] = cards.get(cardIndex);
                cardIndex++;
            }
        }
        addPointsToGroup();
        addValidMoves();
    }

    //group points in order to check for valid moves faster
    public static void addPointsToGroup() {
        points.clear();
        for (int i = 0; i < BOARD.length; i++) {
            for (int j = 0; j < BOARD[0].length; j++) {
                if (BOARD[i][j] != 0) {
                    Point p = new Point(j, i);
                    if (!points.containsKey(BOARD[i][j])) {
                        points.put(BOARD[i][j], new ArrayList<>());
                    }
                    points.get(BOARD[i][j]).add(p);
                }
            }
        }
    }

    //add the point pairs as list of valid moves
    public static void addValidMoves() {
        validPairs.clear();
        for (Map.Entry<Integer, ArrayList<Point>> entry : points.entrySet()) {
            ArrayList<Point> points = entry.getValue();
            for (int i = 0; i < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    Point p1 = points.get(i);
                    Point p2 = points.get(j);
                    checkValidMove(p1, p2);
                }
            }
        }
    }

    //remove two cards
    public static void removeCard(Point p1, Point p2) {
        cards.remove(Integer.valueOf(BOARD[p1.y][p1.x]));
        cards.remove(Integer.valueOf(BOARD[p2.y][p2.x]));
        ArrayList<Point> pointList = points.get(BOARD[p1.y][p1.x]);
        pointList.remove(p1);
        pointList.remove(p2);
        if (pointList.size() == 0) {
            points.remove(BOARD[p1.y][p1.x]);
        }
        BOARD[p1.y][p1.x] = 0;
        BOARD[p2.y][p2.x] = 0;

        transformBoard(p1, p2);
        addPointsToGroup();
        addValidMoves();
    }

    public static boolean checkEmptyBoard() {
        return points.isEmpty();
    }

    // algorithms to check if two cards are connected
    public static boolean canTwoPointsConnected(Point p1, Point p2) {
        //check if user is not click the same card
        if (p1.x == p2.x && p1.y == p2.y) {
            return false;
        }

        // check if the points in the card is valid
        if (BOARD[p1.y][p1.x] == 0 || BOARD[p2.y][p2.x] == 0 || BOARD[p1.y][p1.x] != BOARD[p2.y][p2.x]) {
            return false;
        }

        PointPair pp = new PointPair(p1, p2);
        //check if the points are connectable
        return validPairs.containsKey(pp);
    }

    private static void checkValidMove(Point p1, Point p2) {
        PointPair matchingPoint = new PointPair(p1, p2);

        //1. check vertically
        if (p1.x == p2.x && checkLineVertical(p1.y, p2.y, p1.x)) {
            validPairs.put(matchingPoint, null);
            return;
        }

        //2. check horizontally
        if (p1.y == p2.y && checkLineHorizontal(p1.x, p2.x, p1.y)) {
            validPairs.put(matchingPoint, null);
            return;
        }

        //3. check 3 horizontal zigzag line (Z-line)
        PointPair ppZHorizontal = checkRectHorizontal(p1, p2);
        if (ppZHorizontal != null) {
            validPairs.put(matchingPoint, ppZHorizontal);
            return;
        }

        //4. check 3 vertical zigzag line (Z-line)
        PointPair ppZVertical = checkRectVertical(p1, p2);
        if (ppZVertical != null) {
            validPairs.put(matchingPoint, ppZVertical);
            return;
        }

        //5. check L move (L-line)
        PointPair ppL = checkLMove(p1, p2, true);
        if (ppL != null) {
            validPairs.put(matchingPoint, ppL);
            return;
        }

        //6. check J move (L-line)
        PointPair ppLReversed = checkLMove(p1, p2, false);
        if (ppLReversed != null) {
            validPairs.put(matchingPoint, ppLReversed);
            return;
        }

        //7. check C move (U-line)
        PointPair ppUHorizontal = checkUMoveHorizontal(p1, p2, true);
        if (ppUHorizontal != null) {
            validPairs.put(matchingPoint, ppUHorizontal);
            return;
        }

        //8. check C reversed move (U-line)
        PointPair ppUHorizontalReversed = checkUMoveHorizontal(p1, p2, false);
        if (ppUHorizontalReversed != null) {
            validPairs.put(matchingPoint, ppUHorizontalReversed);
            return;
        }

        //9. check U move (U-line)
        PointPair ppUVertical = checkUMoveVertical(p1, p2, true);
        if (ppUVertical != null) {
            validPairs.put(matchingPoint, ppUVertical);
            return;
        }

        //10. check N move (U-line)
        PointPair ppUVerticalReversed = checkUMoveVertical(p1, p2, false);
        if (ppUVerticalReversed != null) {
            validPairs.put(matchingPoint, ppUVerticalReversed);
        }
    }

    private static boolean checkLineHorizontal(int x1, int x2, int y) {
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);
        for (int x = min + 1; x < max; x++) {
            if (BOARD[y][x] != 0) { // the line is blocked
                return false;
            }
        }
        return true;
    }

    private static boolean checkLineVertical(int y1, int y2, int x) {
        int min = Math.min(y1, y2);
        int max = Math.max(y1, y2);
        for (int y = min + 1; y < max; y++) {
            if (BOARD[y][x] != 0) { // the line is blocked
                return false;
            }
        }
        return true;
    }

    private static PointPair checkRectHorizontal(Point p1, Point p2) {
        // minx is left, maxx is right
        Point pMinX = p1, pMaxX = p2;
        if (p1.x > p2.x) {
            pMinX = p2;
            pMaxX = p1;
        }

        for (int x = pMinX.x + 1; x < pMaxX.x; x++) {
            if (checkLineHorizontal(pMinX.x, x, pMinX.y)
                    && checkLineVertical(pMinX.y, pMaxX.y, x)
                    && checkLineHorizontal(x, pMaxX.x, pMaxX.y)
                    && BOARD[pMinX.y][x] == 0
                    && BOARD[pMaxX.y][x] == 0) {
                return new PointPair(new Point(x, p1.y), new Point(x, p2.y));
            }
        }
        return null;
    }

    private static PointPair checkRectVertical(Point p1, Point p2) {
        // miny is up, maxy is down
        Point pMinY = p1, pMaxY = p2;
        if (p1.y > p2.y) {
            pMinY = p2;
            pMaxY = p1;
        }
        for (int y = pMinY.y + 1; y < pMaxY.y; y++) {
            if (checkLineVertical(pMinY.y, y, pMinY.x)
                    && checkLineHorizontal(pMinY.x, pMaxY.x, y)
                    && checkLineVertical(y, pMaxY.y, pMaxY.x)
                    && BOARD[y][pMinY.x] == 0
                    && BOARD[y][pMaxY.x] == 0) {
                return new PointPair(new Point(p1.x, y), new Point(p2.x, y));
            }
        }
        return null;
    }

    private static PointPair checkLMove(Point p1, Point p2, boolean noReverse) {
        Point pMinX = p1, pMaxX = p2;
        if (p1.x > p2.x) {
            pMinX = p2;
            pMaxX = p1;
        }
        if (noReverse && checkLineHorizontal(pMinX.x, pMaxX.x, pMaxX.y) && checkLineVertical(pMinX.y, pMaxX.y, pMinX.x)
                && BOARD[pMaxX.y][pMinX.x] == 0) {
            return new PointPair(new Point(pMinX.x, pMaxX.y), new Point(pMinX.x, pMaxX.y));
        } else if (!noReverse && checkLineHorizontal(pMinX.x, pMaxX.x, pMinX.y)
                && checkLineVertical(pMinX.y, pMaxX.y, pMaxX.x) && BOARD[pMinX.y][pMaxX.x] == 0) {
            return new PointPair(new Point(pMaxX.x, pMinX.y), new Point(pMaxX.x, pMinX.y));
        }
        return null;
    }

    private static PointPair checkUMoveHorizontal(Point p1, Point p2, boolean right) {
        // minx is left, maxx is right
        Point pMinX = p1, pMaxX = p2;
        if (p1.x > p2.x) {
            pMinX = p2;
            pMaxX = p1;
        }
        int x = pMaxX.x;
        int yStart = pMinX.y;
        if (!right) {
            x = pMinX.x;
            yStart = pMaxX.y;
        }
        x += right ? 1 : -1;
        if ((right && !checkLineHorizontal(pMinX.x, x, yStart)) || (!right && !checkLineHorizontal(x, pMaxX.x, yStart))) {
            return null;
        }

        int leftLimit = 0;
        int rightLimit = BOARD[0].length - 1;
        while (x >= leftLimit && x <= rightLimit && BOARD[pMinX.y][x] == 0 && BOARD[pMaxX.y][x] == 0) {
            if (checkLineVertical(pMinX.y, pMaxX.y, x)) {
                return new PointPair(new Point(x, p1.y), new Point(x, p2.y));
            }
            x += right ? 1 : -1;
        }
        // Check if the middle line exceeds x limit
        if (x < leftLimit || x > rightLimit) {
            return new PointPair(new Point(x, p1.y), new Point(x, p2.y));
        }
        return null;
    }

    private static PointPair checkUMoveVertical(Point p1, Point p2, boolean down) {
        // miny is up, maxy is down
        Point pMinY = p1, pMaxY = p2;
        if (p1.y > p2.y) {
            pMinY = p2;
            pMaxY = p1;
        }
        int y = pMaxY.y;
        int xStart = pMinY.x;
        if (!down) {
            y = pMinY.y;
            xStart = pMaxY.x;
        }
        y += down ? 1 : -1;
        if ((down && !checkLineVertical(pMinY.y, y, xStart)) || (!down && !checkLineVertical(y, pMaxY.y, xStart))) {
            return null;
        }

        int upLimit = 0;
        int downLimit = BOARD.length - 1;
        while (y >= upLimit && y <= downLimit && BOARD[y][pMinY.x] == 0 && BOARD[y][pMaxY.x] == 0) {
            if (checkLineHorizontal(pMinY.x, pMaxY.x, y)) {
                return new PointPair(new Point(p1.x, y), new Point(p2.x, y));
            }
            y += down ? 1 : -1;
        }
        // Check if the middle line exceeds y limit
        if (y < upLimit || y > downLimit) {
            return new PointPair(new Point(p1.x, y), new Point(p2.x, y));
        }
        return null;
    }

    private static void transformBoard(Point p1, Point p2) {
        switch (level) {
            case 2: {
                Point pMinY = p1;
                Point pMaxY = p2;
                if (p1.y > p2.y) {
                    pMinY = p2;
                    pMaxY = p1;
                }
                for (int y = pMaxY.y; y < BOARD.length - 1; y++) {
                    BOARD[y][pMaxY.x] = BOARD[y + 1][pMaxY.x];
                }
                BOARD[BOARD.length - 1][pMaxY.x] = 0;
                for (int y = pMinY.y; y < BOARD.length - 1; y++) {
                    BOARD[y][pMinY.x] = BOARD[y + 1][pMinY.x];
                }
                BOARD[BOARD.length - 1][pMinY.x] = 0;
                break;
            }
            case 3: {
                Point pMinY = p1;
                Point pMaxY = p2;
                if (p1.y > p2.y) {
                    pMinY = p2;
                    pMaxY = p1;
                }
                for (int y = pMinY.y; y > 0; y--) {
                    BOARD[y][pMinY.x] = BOARD[y - 1][pMinY.x];
                }
                BOARD[0][pMinY.x] = 0;
                for (int y = pMaxY.y; y > 0; y--) {
                    BOARD[y][pMaxY.x] = BOARD[y - 1][pMaxY.x];
                }
                BOARD[0][pMaxY.x] = 0;
                break;
            }
            case 4: {
                Point pMinX = p1;
                Point pMaxX = p2;
                if (p1.x > p2.x) {
                    pMinX = p2;
                    pMaxX = p1;
                }
                for (int x = pMaxX.x; x < BOARD[0].length - 1; x++) {
                    BOARD[pMaxX.y][x] = BOARD[pMaxX.y][x + 1];
                }
                BOARD[pMaxX.y][BOARD[0].length - 1] = 0;
                for (int x = pMinX.x; x < BOARD[0].length - 1; x++) {
                    BOARD[pMinX.y][x] = BOARD[pMinX.y][x + 1];
                }
                BOARD[pMinX.y][BOARD[0].length - 1] = 0;
                break;
            }
            case 5: {
                Point pMinX = p1;
                Point pMaxX = p2;
                if (p1.x > p2.x) {
                    pMinX = p2;
                    pMaxX = p1;
                }
                for (int x = pMinX.x; x > 0; x--) {
                    BOARD[pMinX.y][x] = BOARD[pMinX.y][x - 1];
                }
                BOARD[pMinX.y][0] = 0;
                for (int x = pMaxX.x; x > 0; x--) {
                    BOARD[pMaxX.y][x] = BOARD[pMaxX.y][x - 1];
                }
                BOARD[pMaxX.y][0] = 0;
                break;
            }
            case 6: {
                Point pBefore = p1;
                Point pAfter = p2;

                int middleX = BOARD[0].length / 2;
                int distancep1 = Math.abs(p1.x - middleX);
                int distancep2 = Math.abs(p2.x - middleX);
                if (distancep2 < distancep1) {
                    pBefore = p2;
                    pAfter = p1;
                }
                if (pBefore.x < BOARD[0].length / 2) {
                    for (int x = pBefore.x; x < BOARD[0].length / 2 - 1; x++) {
                        BOARD[pBefore.y][x] = BOARD[pBefore.y][x + 1];
                    }
                    BOARD[pBefore.y][BOARD[0].length / 2 - 1] = 0;
                } else {
                    for (int x = pBefore.x; x > BOARD[0].length / 2; x--) {
                        BOARD[pBefore.y][x] = BOARD[pBefore.y][x - 1];
                    }
                    BOARD[pBefore.y][BOARD[0].length / 2] = 0;
                }
                if (pAfter.x < BOARD[0].length / 2) {
                    for (int x = pAfter.x; x < BOARD[0].length / 2; x++) {
                        BOARD[pAfter.y][x] = BOARD[pAfter.y][x + 1];
                    }
                    BOARD[pAfter.y][BOARD[0].length / 2 - 1] = 0;
                } else {
                    for (int x = pAfter.x; x > BOARD[0].length / 2; x--) {
                        BOARD[pAfter.y][x] = BOARD[pAfter.y][x - 1];
                    }
                    BOARD[pAfter.y][BOARD[0].length / 2] = 0;
                }
            }
            case 7: {
                Point pBefore = p1;
                Point pAfter = p2;

                int middleX = BOARD[0].length / 2;
                int distancep1 = Math.abs(p1.x - middleX);
                int distancep2 = Math.abs(p2.x - middleX);
                if (distancep2 > distancep1) {
                    pBefore = p2;
                    pAfter = p1;
                }
                if (pBefore.x < BOARD[0].length / 2) {
                    for (int x = pBefore.x; x > 0; x--) {
                        BOARD[pBefore.y][x] = BOARD[pBefore.y][x -1];
                    }
                    BOARD[pBefore.y][0] = 0;
                } else {
                    for (int x = pBefore.x; x < BOARD[0].length - 1; x++) {
                        BOARD[pBefore.y][x] = BOARD[pBefore.y][x + 1];
                    }
                    BOARD[pBefore.y][BOARD[0].length - 1] = 0;
                }
                if (pAfter.x < BOARD[0].length / 2) {
                    for (int x = pAfter.x; x > 0; x--) {
                        BOARD[pAfter.y][x] = BOARD[pAfter.y][x -1];
                    }
                    BOARD[pAfter.y][0] = 0;
                } else {
                    for (int x = pAfter.x; x < BOARD[0].length - 1; x++) {
                        BOARD[pAfter.y][x] = BOARD[pAfter.y][x + 1];
                    }
                    BOARD[pAfter.y][BOARD[0].length - 1] = 0;
                }
            }
            default:
                break;
        }
    }
}
