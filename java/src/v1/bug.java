package v1;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;

// Bug0 implementation?
public class bug extends RobotPlayer {
    private static MapLocation prevDest = null;
    private static HashSet<MapLocation> line = null;
    private static int obstacleStartDist = 0;
    private static int bugState = 0;
    private static MapLocation nextObstacle = null;
    private static int nextObstacleDist = 10000;
    private static Direction heading;
    private static Direction bugDir;

    static public void move_toward_goal(RobotController rc, MapLocation goal) throws GameActionException
    {
        // Check if you are at goal
        if (rc.getLocation().equals(goal))
        {
            return;
        }
        if (!rc.isMovementReady())
        {
            return;
        }
        Direction dir = rc.getLocation().directionTo(goal);
        // FOllow if possible
        if (rc.canMove(dir))
        {
            rc.move(dir);
            heading = null;
        }
        else
        {
            MapLocation planned_location = rc.getLocation().add(dir);
            if (heading == null)
            {
                heading = dir;
            }
            //cycle through dirs
            for (int i = 0; i < 8; i++)
            {
                if (rc.canMove(heading))
                {
                    rc.move(heading);
                    heading = heading.rotateRight();
                    break;
                }
                else
                {

                    planned_location = rc.getLocation().add(heading);

                    heading = heading.rotateLeft();

                }
            }
        }
    }

    private static final int PRV_LENGTH = 60;
    private static Direction[] prv = new Direction[PRV_LENGTH];
    private static int pathingCnt = 0;
    private static MapLocation lastPathingTarget = null;
    private static int lastPathingTurn = 0;

    public static String indicator = "";

    static public void move_toward_goal2(RobotController rc, MapLocation location) throws GameActionException
    {
// reset queue when target location changes or there's gap in between calls
        if (!location.equals(lastPathingTarget) || lastPathingTurn < turnCount - 1) {
            pathingCnt = 0;
        }
        lastPathingTarget = location;
        lastPathingTurn = turnCount;

        if (rc.isMovementReady()) {
            if (pathingCnt == 0) {
                Direction dir = rc.getLocation().directionTo(location);
                while ((!rc.canMove(dir))
                        && pathingCnt != 8) {
                    MapLocation loc = rc.getLocation().add(dir);
                    if (rc.onTheMap(loc) && rc.senseRobotAtLocation(loc) != null) {
                        // a robot is blocking our way, reset and use follow instead
                        pathingCnt = 0;
                        indicator += "use follow,";
                        follow(location);
                        return;
                    }
                    prv[pathingCnt] = dir;
                    pathingCnt++;
                    dir = dir.rotateLeft();
                }
                if (pathingCnt != 8) {
                    rc.move(dir);
                } else {
                    // we are blocked in all directions, nothing to do
                    indicator += "perma blocked,";
                    pathingCnt = 0;
                    return;
                }
            } else {
                while (pathingCnt > 0 && canPass(prv[pathingCnt - 1])) {
                    pathingCnt--;
                }
                while (pathingCnt > 0 && !canPass(prv[pathingCnt - 1].rotateLeft())) {
                    prv[pathingCnt] = prv[pathingCnt - 1].rotateLeft();;
                    pathingCnt++;
                    if (pathingCnt == PRV_LENGTH) {
                        pathingCnt = 0;
                        return;
                    }
                }
                Direction moveDir = pathingCnt == 0? prv[pathingCnt] : prv[pathingCnt - 1].rotateLeft();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                } else {
                    // a robot blocking us while we are following wall, wait
                    indicator += "blocked";
                    return;
                }
            }
        }
    }

    static void follow(MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);

        if (rc.isMovementReady()) {
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else if (rc.canMove(dir.rotateRight())) {
                rc.move(dir.rotateRight());
            } else if (rc.canMove(dir.rotateLeft())) {
                rc.move(dir.rotateLeft());
            } else if (rc.canMove(dir.rotateRight().rotateRight())) {
                rc.move(dir.rotateRight().rotateRight());
            } else if (rc.canMove(dir.rotateLeft().rotateLeft())) {
                rc.move(dir.rotateLeft().rotateLeft());
            } else {
                randomMove();
            }
        }
    }

    static void randomMove() throws GameActionException {
        int starting_i = rng.nextInt(directions.length);
        for (int i = starting_i; i < starting_i + 8; i++) {
            Direction dir = directions[i % 8];
            if (rc.canMove(dir)) rc.move(dir);
        }
    }

    static boolean canPass(MapLocation loc) throws GameActionException
    {

        if (!rc.onTheMap(loc) || !rc.senseMapInfo(loc).isPassable()) return false;
        return true;

    }

    static boolean canPass(Direction dir) throws GameActionException
    {

        return canPass(rc.getLocation().add(dir));

    }

    private static HashSet<MapLocation> createLine(MapLocation a, MapLocation b) {
        HashSet<MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx,dy);
        int r = d/2;
        if (dx > dy) {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if (r >= dx) {
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        }
        else {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if (r >= dy) {
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }
}