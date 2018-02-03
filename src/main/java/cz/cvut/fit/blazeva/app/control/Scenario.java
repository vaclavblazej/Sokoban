package cz.cvut.fit.blazeva.app.control;

import cz.cvut.fit.blazeva.app.model.Goal;
import cz.cvut.fit.blazeva.app.model.Player;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

    public enum TileTypes {
        BOX, WALL, EMPTY;
    }

    public int size = 10;
    public boolean won = false;

    public TileTypes[][] map = new TileTypes[size][size];

    {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = i == 0 || i == size - 1 || j == 0 || j == size - 1 ? TileTypes.WALL : TileTypes.EMPTY;
            }
        }
        map[3][3] = map[2][2] = TileTypes.BOX;
        map[4][5] = TileTypes.WALL;
    }

    public List<Goal> goals = new ArrayList<>();
    {
        goals.add(new Goal(2, 3));
        goals.add(new Goal(4, 3));
    }

    public Player player = new Player();


    private boolean moveAll(int x, int y, int dx, int dy) {
        switch (map[x][y]) {
            case WALL:
                return false;
            case BOX:
                final boolean b = moveAll(x + dx, y + dy, dx, dy);
                if (b) {
                    TileTypes tmp = map[x][y];
                    map[x][y] = map[x + dx][y + dy];
                    map[x + dx][y + dy] = tmp;
                }
                return b;
            case EMPTY:
                return true;
            default:
                return false;
        }
    }

    public void move(int dx, int dy) {
        int nx = player.x + dx;
        int ny = player.y + dy;
        nx = Math.max(Math.min(nx, size - 1), 0);
        ny = Math.max(Math.min(ny, size - 1), 0);
        if (moveAll(nx, ny, dx, dy)) {
            player.x = nx;
            player.y = ny;
        }
        int finished = 0;
        for (Goal goal : goals) {
            if(map[goal.x][goal.y] == TileTypes.BOX) finished++;
        }
        if(finished == goals.size()){
            won = true;
        }
    }

}
