package summativeGame;

import becker.robots.*;


public abstract class GameRobot extends RobotSE {
	private Arena arena;                 // Reference to the game arena
	private int energy;                  // Current energy level of the robot
	private int speed;                   // Number of tiles robot can move per turn
	private int x;                       // Current street (y-coordinate) for internal tracking
	private int y;                       // Current avenue (x-coordinate) for internal tracking
	private String name;                // Type/name of robot (e.g., "killer", "medic", etc.)
	private boolean frozen;             // Whether the robot is currently frozen
	private Coordinates[] itemInfo;     // Positions of items on the board (set each turn)
	private int catchingAbility;        // % chance robot can successfully pick up an item
	private int id;                     // Unique ID for robot (used for indexing and identification)

	/**
	 * Constructor for all game robots.
	 * @param id - unique robot ID
	 * @param c - City map from becker.robots
	 * @param street - initial y-coordinate (row)
	 * @param avenue - initial x-coordinate (column)
	 * @param d - initial direction the robot is facing
	 * @param arena - reference to the arena object
	 * @param energy - starting energy
	 * @param speed - movement speed (tiles per turn)
	 * @param type - robot type/name (e.g., "killer")
	 * @param dodgeChance - not used in base class
	 * @param catchChance - chance of successfully catching (picking) items
	 */
	public GameRobot(int id, City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, int dodgeChance, int catchChance) {
		super(c, street, avenue, d);
		this.arena = arena;
		this.energy = energy;
		this.speed = speed;
		this.name = type;
		this.x = street;
		this.y = avenue;
		this.frozen = false;
		this.catchingAbility = catchChance;
		this.id = id;
	}

	// Getter Methods

	/**
	 * @return name of the robot (its type, like "killer")
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return robot's unique ID
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * @return current avenue (column) position
	 */
	@Override
	public int getAvenue() {
		return super.getAvenue();
	}

	/**
	 * @return current street (row) position
	 */
	@Override
	public int getStreet() {
		return super.getStreet();
	}

	/**
	 * @return current energy level of the robot
	 */
	public int getEnergy() {
		return energy;
	}

	/**
	 * @return robot's speed
	 */
	public int getRobotSpeed() {
		return speed;
	}

	/**
	 * @return true if robot is frozen, false otherwise
	 */
	public boolean isRobotFrozen() {
		return frozen;
	}

	/**
	 * @return robot's chance of catching another robot
	 */
	public int getCatchChance() {
		return catchingAbility;
	}

	/**
	 * @return robot's chance of dodging 
	 */
	public int getDodgeChance() {
		return 0;
	}

	// Item-Related Methods

	/**
	 * Picks up multiple items from the current location.
	 * @param numThings - number of things to pick up
	 */
	public void pickThing(int numThings) {
		for (int i = 0; i < numThings; i++) {
			this.pickThing();
		}
	}

	/**
	 * @return array of item coordinates (locations of all items on map)
	 */
	public Coordinates[] getItemInfo() {
		return this.itemInfo;
	}

	/**
	 * Sets the locations of all items on the board.
	 * @param itemInfo - array of coordinates representing item positions
	 */
	public void setItemInfo(Coordinates[] itemInfo) {
		this.itemInfo = itemInfo;
	}

	// Setter Methods

	/**
	 * Sets the robot's name (its type).
	 * @param name - type of the robot
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets internal x-coordinate (avenue) tracking variable.
	 * @param x - avenue (column) position
	 */
	public void setAvenue(int x) {
		this.x = x;
	}

	/**
	 * Sets internal y-coordinate (street) tracking variable.
	 * @param y - street (row) position
	 */
	public void setStreet(int y) {
		this.y = y;
	}

	/**
	 * Sets robot's energy level.
	 * @param energy - new energy value
	 */
	public void setEnergy(int energy) {
		this.energy = energy;
	}

	/**
	 * Sets whether the robot is frozen.
	 * @param isFrozen - true to freeze the robot, false to unfreeze
	 */
	public void setIsRobotFrozen(boolean isFrozen) {
		this.frozen = isFrozen;
	}

	/**
	 * Abstract method to define robot behavior each turn.
	 * To be implemented by each robot type 
	 *
	 * @param robots - array of all GameRecords
	 * @param i - index of the current robot in the array
	 * @param itemInfo - array of item coordinates for this turn
	 * @return MoveStatus - the action taken by the robot this turn
	 */
	public abstract MoveStatus moveTurn(GameRecord[] robots, int i, Coordinates[] itemInfo);
}