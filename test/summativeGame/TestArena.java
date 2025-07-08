package summativeGame;

import java.util.Random;

import becker.robots.City;
import becker.robots.Direction;
import becker.robots.Thing;
import becker.robots.Wall;

public class TestArena extends Arena {
	// Declaring and initializing final variables for the dimensions of
	// the cafeteria
	public static final int HEIGHT = 13;
	public static final int WIDTH = 24;

	// Declaring private variables for properties of the arena
	private City playArea;
	private int numThings = 15;

	// Making a list of the coordinates of the item with the length of the
	// predetermined number of things
	Coordinates[] itemCoordinates = new Coordinates[numThings];

	/**
	 * Constructor method that builds the arena and sets up the walls, and calls the
	 * scatterThings() method
	 */
	public TestArena() {
		scatterThings(playArea);
	}

	public City buildCity() {
		playArea = new City(HEIGHT, WIDTH);
		playArea.showThingCounts(true);

		// making the top horizontal wall with "width" length
		for (int c = 0; c < WIDTH; c++) {
			new Wall(playArea, 0, c, Direction.NORTH);
		}
		// making the bottom horizontal wall with "width" length
		for (int c = 0; c < WIDTH; c++) {
			new Wall(playArea, HEIGHT - 1, c, Direction.SOUTH);
		}
		// making the left vertical wall with "width" length
		for (int r = 0; r < HEIGHT; r++) {
			new Wall(playArea, r, 0, Direction.WEST);
		}
		// making the right vertical wall with "width" length
		for (int r = 0; r < HEIGHT; r++) {
			new Wall(playArea, r, WIDTH - 1, Direction.EAST);
		}
		
		return playArea;
	}

	public TestArena(Coordinates[] itemCoordinates) {
		super(itemCoordinates);
		
	}

	public void scatterThings(Coordinates[] itemCoordinates) {
		for(int i = 0; i < itemCoordinates.length; i++) {
			Coordinates coord = itemCoordinates[i];
			this.placeThing(coord.getStreet(), coord.getAvenue());
		}
	}
	
	/**
	 * Places things at the given coordinates
	 * 
	 * @param city - the city the arena is in
	 * @param r    - the row/height
	 * @param c    - the column/width
	 */
	public void placeThing(int r, int c) {
		new Thing(this.getCity(), r, c);
	}

	/**
	 * Returns the playArea
	 * 
	 * @return - playArea
	 */
	public City getCity() {
		return this.playArea;
	}

	/**
	 * Returns the width of the playArea
	 * 
	 * @return - the width
	 */
	public int getWidth() {
		return this.WIDTH;
	}

	/**
	 * Returns the height of the playArea
	 * 
	 * @return - the height
	 */
	public int getHeight() {
		return this.HEIGHT;
	}

	/**
	 * Returns the list of items
	 * 
	 * @return - the coordinates of where all the items are
	 */
	public Coordinates[] getListofItems() {
		return itemCoordinates;
	}

	/**
	 * Returns the coordinates of the corners
	 * 
	 * @return - list of coordinates of the corners
	 */
	public Coordinates[] getCorners() {
		Coordinates[] corners = new Coordinates[4];
		corners[0] = new Coordinates(0, 0);
		corners[1] = new Coordinates(0, WIDTH - 1);
		corners[2] = new Coordinates(HEIGHT - 1, 0);
		corners[3] = new Coordinates(HEIGHT - 1, WIDTH - 1);
		return corners;
	}
}
