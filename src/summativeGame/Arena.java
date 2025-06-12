package summativeGame;

import becker.robots.*;
import java.util.Random;

public class Arena {

	public static final int HEIGHT = 13;
	public static final int WIDTH = 24;
	private City playArea;

	int numThings = 4;

	Coordinates[] itemCoordinates = new Coordinates[numThings];

	public Arena() {

		playArea = new City(HEIGHT, WIDTH);

		playArea.showThingCounts(true);

		// Top
		for (int c = 0; c < WIDTH; c++) {
			new Wall(playArea, 0, c, Direction.NORTH);
		}

		// Bottom
		for (int c = 0; c < WIDTH; c++) {
			new Wall(playArea, HEIGHT - 1, c, Direction.SOUTH);
		}

		// Left
		for (int r = 0; r < HEIGHT; r++) {
			new Wall(playArea, r, 0, Direction.WEST);
		}

		// Right
		for (int r = 0; r < HEIGHT; r++) {
			new Wall(playArea, r, WIDTH - 1, Direction.EAST);
		}

		scatterThings(playArea);

	}

	private void scatterThings(City city) {
		// Define the corner coordinates to avoid placing Things
		int[] boxRowsSet = { 0, 0, HEIGHT - 1, HEIGHT - 1 };
		int[] boxColsSet = { 0, WIDTH - 1, 0, WIDTH - 1 };
		Random rand = new Random();
		
		for (int i = 0; i < numThings; i++) {
			int r, c;
			boolean invalidPosition;
			do {
				r = rand.nextInt(HEIGHT);
				c = rand.nextInt(WIDTH);
				invalidPosition = false;
				for (int j = 0; j < 4; j++) {
					if (r == boxRowsSet[j] && c == boxColsSet[j]) {
						invalidPosition = true;
						break;
					}
				}
			} while (invalidPosition);
			new Thing(city, r, c);
			itemCoordinates[i] = new Coordinates(r, c);
		}
	}

	public City getCity() {
		return this.playArea;
	}

	public Coordinates[] getListofItems() {
		return itemCoordinates;
	}

	public Coordinates[] getCorners() {
		Coordinates[] corners = new Coordinates[4];
		corners[0] = new Coordinates(0, 0);
		corners[1] = new Coordinates(0, this.WIDTH - 1);
		corners[2] = new Coordinates(this.HEIGHT - 1, 0);
		corners[3] = new Coordinates(this.HEIGHT - 1, this.WIDTH - 1);
		return corners;
	}
	
	public int getWidth() {
		return this.WIDTH;
	}
	
	public int getHeight() {
		return this.HEIGHT;
	}
}