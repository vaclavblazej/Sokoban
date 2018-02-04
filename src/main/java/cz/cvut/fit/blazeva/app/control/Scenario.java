package cz.cvut.fit.blazeva.app.control;

import cz.cvut.fit.blazeva.app.model.Goal;
import cz.cvut.fit.blazeva.app.model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cz.cvut.fit.blazeva.util.DemoUtils.ioResourceToBufferedReader;

public class Scenario {

    public enum TileTypes {
        BOX, WALL, EMPTY;
    }

    public int size;
    public boolean won;

    public TileTypes[][] map;

    public List<Goal> goals = new ArrayList<>();


    public Player player = new Player();


    public Scenario(String name) {
        won = false;
        try {
            System.out.println("loading scenario " + name);
            try (final BufferedReader br = ioResourceToBufferedReader("cz/cvut/fit/blazeva/levels/" + name)) {
                String line;
                line = br.readLine();
                size = Integer.parseInt(line);
                map = new TileTypes[size][size];
                for (int j = size - 1; j >= 0 && (line = br.readLine()) != null; --j) {
                    for (int i = 0; i < line.length(); ++i) {
                        switch (line.charAt(i)) {
                            case 'W': // wall
                                map[i][j] = TileTypes.WALL;
                                break;
                            case 'b': // box
                                map[i][j] = TileTypes.BOX;
                                break;
                            case 'g': // goal
                                goals.add(new Goal(i, j));
                                map[i][j] = TileTypes.EMPTY;
                                break;
                            case 'p': // player
                                player.x = i;
                                player.y = j;
                                map[i][j] = TileTypes.EMPTY;
                                break;
                            case '-': // empty space
                                map[i][j] = TileTypes.EMPTY;
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR LOADING LEVEL");
        }
    }

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
            if (map[goal.x][goal.y] == TileTypes.BOX) finished++;
        }
        if (finished == goals.size()) {
            won = true;
        }
    }

}
