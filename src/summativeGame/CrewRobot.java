package summativeGame;

import java.util.ArrayList;
import becker.robots.City;
import becker.robots.Direction;

/**
 * The CrewRobot that goes around the playArea and collects various items while
 * avoiding killer robot.
 * 
 * @author heerm
 * @version June 13, 2025
 */
public class CrewRobot extends GameRobot {
	// Declaring private variables for the attributes of the robot
	private int dodgingAbility;
	private String type;
	private Arena arena;
	private boolean frozen;
	private int id;

	// Declaring private variables for values being used to determine the next move
	// and strategize
	private int killerIndex;
	private Coordinates currentTarget;
	private Coordinates currentCornerTarget;
	private Coordinates[] itemCoordinates;
	private GameRecord[] playersInfo;
	private ArrayList<Coordinates> recentPositions = new ArrayList<Coordinates>();
	private int estimatedCatchAbility = 50;
	private int totalEncounters = 0;
	private int successfulDodges = 0;

	/**
	 * Constructor method that initializes the properties of the CrewRobot
	 * 
	 * @param id             - the id of the robot
	 * @param c              - the City the robot is in
	 * @param street         - the y coordinate of the robot
	 * @param avenue         - the x coordinate of the robot
	 * @param d              - the direction the robot is facing
	 * @param arena          - the playArea the robot is playing in
	 * @param energy         - the energy of the robot
	 * @param speed          - the number of steps the robot can move
	 * @param type           - the type (killer/crew/medic) of the robot
	 * @param dodgingAbility - the ability for the robot to dodge the killer if it
	 *                       tries to freeze it
	 * @param catchAbility   - the ability to catch (0)
	 */
	public CrewRobot(int id, City c, int street, int avenue, Direction d, Arena arena, int energy, int speed,
			String type, int dodgingAbility, int catchAbility) {
		super(id, c, street, avenue, d, arena, energy, speed, type, dodgingAbility, catchAbility);
		this.type = "crew";
		catchAbility = 0;
		this.arena = arena;
	}

	/**
	 * Overrides the move method to deplete the energy by 1 every time the robot
	 * moves a square
	 */
	public void move() {
		// If the robot is out of energy it returns without further depleting it
		if (this.getEnergy() <= 0) {
			return;
		}
		// If not, it moves once and reduces the energy by one
		super.move();
		this.setEnergy(this.getEnergy() - 1);
	}

	/**
	 * This is called in the ControlCenter when the robot is frozen. It overrides
	 * the method in GameRobot so that it can dodge it based on chance and estimated
	 * killer catchAbility
	 * 
	 * @param isFrozen - true/false of wether the robot gets frozen or not
	 */
	public void setIsRobotFrozen(boolean isFrozen) {
		// If the robot is frozen, it tries to dodge and adds the interaction to the
		// recordGotFrozen to calculate killer's catchAbility. Otherwise, it sets the
		// isRobotFrozen to false
		if (isFrozen) {
			boolean dodged = this.tryToDodge();
			this.recordGotFrozen(dodged);

			// if the robot dodges, the sets the isRobotFrozen to false and frozen to false.
			// Otherwise, it sets the isRobotFrozen to true and frozen to true.
			if (dodged) {
				super.setIsRobotFrozen(false);
				this.frozen = false;
			} else {
				super.setIsRobotFrozen(true);
				this.frozen = true;
			}
		} else {
			super.setIsRobotFrozen(false);
			this.frozen = false;
		}
	}

	/**
	 * Called in the ControlCenter when it is this robot's turn to move. It
	 * determines which strategy to use, the index of the killer, and whether or not
	 * to skip a turn and regenerate energy
	 * 
	 * @param robots          - the GameRecord array of robots in the game
	 * @param i               - the index of the robot in the array
	 * @param itemCoordinates - the array of Coordinates for where all the items are
	 */
	public MoveStatus moveTurn(GameRecord[] robots, int i, Coordinates[] itemCoordinates) {
		this.itemCoordinates = itemCoordinates;
		this.playersInfo = robots;

		int index;
		// Checks through all players to find which one the killer is
		for (index = 0; index < playersInfo.length; index++) {
			GameRecord player = playersInfo[index];
			// If the name of the robot is "killer" then it assigns that index to
			// killerIndex
			if (player.getName().compareTo("killer") == 0) {
				this.killerIndex = index;
			}
		}

		// If the energy is 0, it skips the turn and resets energy to 90. If the energy
		// is 30 or lower, it takes defensive strategy. Otherwise, it takes the
		// offensive strategy.
		if (this.getEnergy() <= 0) {
			System.out.println(this.id + " setting energy to 90.");
			this.setEnergy(this.getEnergy() + 90);
		} else if (this.getEnergy() >= 30) {
			this.offensiveStrategy(itemCoordinates);
		} else {
			this.defensiveStrategy(arena, this.playersInfo, arena.getCorners());
		}

		robots = playersInfo;
		return new MoveStatus(this.id, -1, " ");
	}

	/**
	 * Strategy where the robot goes to pick up items around the playArea
	 * 
	 * @param itemCoordinates - array of Coordinates of all the items
	 */
	protected void offensiveStrategy(Coordinates[] itemCoordinates) {
		// If there is nothing in the backpack, the robot will find a new target and
		// pick it up. Otherwise, it will find a corner to drop it in.
		if (this.countThingsInBackpack() == 0) {

			// If the robot doesn't have a target, it will find a new one.
			if (this.currentTarget == null) {
				currentTarget = this.findItem(itemCoordinates, playersInfo);
			}

			// If the robot does have a target, it will move towards it
			if (this.currentTarget != null) {
				this.moveTowards(currentTarget);

				// If the robot is at the location of the item, it will pick it up
				if (this.getStreet() == currentTarget.getStreet() && this.getAvenue() == currentTarget.getAvenue()) {

					// If the robot can pick up an item, it will pick it up, otherwise it will set
					// the currentTarget back to null
					if (this.canPickThing()) {
						this.pickThing();
						currentTarget.setIsPicked(true);
					} else {
						currentTarget = null;
					}
				} else {
					System.out.println(this.id + "on the way to " + this.currentTarget);
				}
			}
		} else {

			// If the robot does not know which corner to go to, it will find one.
			if (currentCornerTarget == null) {
				Coordinates[] sortedCornersList = findClosestCorner(this.arena.getCorners(), playersInfo);
				currentCornerTarget = sortedCornersList[0];
			}

			// It moves towards the corner
			this.moveTowards(currentCornerTarget);

			// If the robot is at the corner, it will put the item down, set the
			// finalPosition to true, and set currentTarget and cornerTarget to null.
			if (this.getStreet() == currentCornerTarget.getStreet()
					&& this.getAvenue() == currentCornerTarget.getAvenue()) {
				this.putThing();
				currentTarget.setIsFinalPosition(true);
				currentCornerTarget = null;
				currentTarget = null;
			}
		}
	}

	/**
	 * Sorts using selection sort, to find the closest corner that is far from the
	 * killer
	 * 
	 * @param corners - the list of the corners
	 * @return - the sorted list of corners based on distance to self, as well as
	 *         killer
	 */
	protected Coordinates[] findClosestCorner(Coordinates[] corners, GameRecord[] playersInfo) {
		Coordinates killerCoord = new Coordinates(playersInfo[killerIndex].getStreet(),
				playersInfo[killerIndex].getAvenue());
		Coordinates selfCoord = new Coordinates(this.getStreet(), this.getAvenue());
		Coordinates[] sortedCorners = corners.clone();

		// For all the elements in the list
		for (int i = 0; i < sortedCorners.length; i++) {
			int minIndex = i;

			// Loop through the rest of the list
			for (int j = i + 1; j < sortedCorners.length; j++) {
				// Finding distance to self for current and best option so far
				int currentSelfDistance = findDistance(selfCoord, sortedCorners[j]);
				int bestSelfDistance = findDistance(selfCoord, sortedCorners[minIndex]);

				// If the current corner is closer to self, set it as the new best. Otherwise,
				// if both are equally close to self, check which one is farther from the killer
				if (currentSelfDistance < bestSelfDistance) {
					minIndex = j;
				} else if (currentSelfDistance == bestSelfDistance) {
					int currentKillerDistance = findDistance(killerCoord, sortedCorners[j]);
					int bestKillerDistance = findDistance(killerCoord, sortedCorners[minIndex]);

	                // If current is farther from killer, set it as the new best
					if (currentKillerDistance > bestKillerDistance) {
						minIndex = j;
					}
				}
			}

	        // Swaps the coordinates from closest to farthest (based on self distance, with killer tie-breaker)
			Coordinates temp = sortedCorners[i];
			sortedCorners[i] = sortedCorners[minIndex];
			sortedCorners[minIndex] = temp;
		}

		return sortedCorners;
	}

	/**
	 * Moves the robot towards a target
	 * 
	 * @param target - the end point of where the robot is trying to get to
	 */
	public void moveTowards(Coordinates target) {
		// The speed is the number of steps the robot can move every turn
		int numSteps = this.getRobotSpeed();

		// While the number of steps is greater than 0, and the robot still has energy
		while (numSteps > 0 && this.getEnergy() > 0) {
			Coordinates nextSpot;

			// If the avenue is greater is turns left, if the avenue is smaller is
			// turns right. If the street is greater it turns north, if
			// the street and smaller it turns south.
			if (this.getAvenue() > target.getAvenue()) {
				this.changeDirection(Direction.WEST);
				nextSpot = new Coordinates(this.getStreet(), this.getAvenue() - 1);
			} else if (this.getAvenue() < target.getAvenue()) {
				this.changeDirection(Direction.EAST);
				nextSpot = new Coordinates(this.getStreet(), this.getAvenue() + 1);
			} else if (this.getStreet() > target.getStreet()) {
				this.changeDirection(Direction.NORTH);
				nextSpot = new Coordinates(this.getStreet() - 1, this.getAvenue());
			} else if (this.getStreet() < target.getStreet()) {
				this.changeDirection(Direction.SOUTH);
				nextSpot = new Coordinates(this.getStreet() + 1, this.getAvenue());
			} else {
				break;
			}

			// If the next spot is one of the last three spots it traveled to, it won't go
			// there
			if (recentPositions.contains(nextSpot)) {
				break;
			}

			// Moves once and reduces the number of steps left by 1
			this.move();
			numSteps--;

			// Adds the new position to the recentPositions list and removes the oldest one
			recentPositions.add(new Coordinates(this.getStreet(), this.getAvenue()));
			if (recentPositions.size() > 3) {
				recentPositions.remove(0);
			}
		}

	}

	/**
	 * Uses bubble sort to find the closest item that is also far from the killer
	 * 
	 * @param itemCoordinates - array of Coordinates of all the items
	 * @param playersInfo2    - the record of playes and their attributes
	 * @return - the sorted list of items from closest to farthest (considering the
	 *         distance to killer too)
	 */
	protected Coordinates findItem(Coordinates[] itemCoordinates, GameRecord[] playersInfo2) {
		// Gets killer's current location
		Coordinates killerCoord = new Coordinates(playersInfo2[killerIndex].getStreet(),
				playersInfo2[killerIndex].getAvenue());

		// Creates an ArrayList to store items that have not been picked up
		ArrayList<Coordinates> availableItems = new ArrayList<Coordinates>();

		// Loops through all items and adds unpicked items to the list
		for (int i = 0; i < itemCoordinates.length; i++) {
			Coordinates item = itemCoordinates[i];
			// if the item hasn't been picked, it adds it to available items
			if (item.getIsPicked() == false) {
				availableItems.add(item);
			}
		}

		// Compares and sorts the available items
		for (int i = 0; i < availableItems.size() - 1; i++) {
			// Each pass bubbles the farthest item to the front
			for (int j = 0; j < availableItems.size() - i - 1; j++) {
				// Finding distance to killer for adjacent items
				int killerDist1 = findDistance(availableItems.get(j), killerCoord);
				int killerDist2 = findDistance(availableItems.get(j + 1), killerCoord);

				// If the next item is farther from killer, swap them
				if (killerDist1 < killerDist2) {
					Coordinates temp = availableItems.get(j);
					availableItems.set(j, availableItems.get(j + 1));
					availableItems.set(j + 1, temp);
				}
				// If distances to killer are the same, compare distance to self (hardcoded
				// reference point)
				else if (killerDist1 == killerDist2) {
					int selfDist1 = findDistance(availableItems.get(j).getAvenue(), availableItems.get(j).getStreet());
					int selfDist2 = findDistance(availableItems.get(j + 1).getAvenue(),
							availableItems.get(j + 1).getStreet());

					// If current item is farther from self than the next, swap them
					if (selfDist1 > selfDist2) {
						Coordinates temp = availableItems.get(j);
						availableItems.set(j, availableItems.get(j + 1));
						availableItems.set(j + 1, temp);
					}
				}
			}
		}

		// Returns the farthest available item from killer (with tie-breaker by
		// closeness to self)
		if (availableItems.size() > 0) {
			return availableItems.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Finds the distance between any object and the robot
	 * 
	 * @param avenue - the avenue (x-coordinate) of the other robot
	 * @param street - the street (y-coordinate) of the other robot
	 * @return - the distance
	 */
	protected int findDistance(int avenue, int street) {
		int distance;
		distance = Math.abs(this.getAvenue() - avenue) + Math.abs(this.getStreet() - street);

		return distance;
	}

	/**
	 * Finds the distance between any 2 coordinates
	 * 
	 * @param coord1 - the coordinates of one item/robot/spot
	 * @param coord2 - the coordinates of one item/robot/spot
	 * @return - the distance
	 */
	private int findDistance(Coordinates coord1, Coordinates coord2) {
		int distance;
		distance = Math.abs(coord1.getAvenue() - coord2.getAvenue())
				+ Math.abs(coord1.getStreet() - coord2.getStreet());

		return distance;
	}

	/**
	 * Changes the direction to the given direction
	 * 
	 * @param d - the end direction
	 */
	private void changeDirection(Direction d) {
		// While the robot is not facing the desired direction, it will turn right
		while (this.getDirection() != d) {
			this.turnRight();
		}
	}

	/**
	 * Trues to dodge based on a random chance and the dodgeAbility
	 * 
	 * @return
	 */
	public boolean tryToDodge() {
		int chance = (int) (Math.random() * 100);
		return (this.dodgingAbility + chance) >= 50;
	}

	/**
	 * Keeps track of all interactions between this robot and killer and updates the
	 * estimated catch abilty
	 * 
	 * @param escaped - whether or not the robot was able to get away when tagged
	 */
	public void recordGotFrozen(boolean escaped) {
		totalEncounters++;

		// If it was able to get away, it increments the successful dodges by one
		if (escaped == true) {
			successfulDodges++;
		}

		// Updates estimated catchABility of the robot
		estimatedCatchAbility = 100 - (int) (((double) successfulDodges / totalEncounters) * 100);
	}

	/**
	 * Strategy where the robot checks the estimated catchAbility and the distance
	 * to the killer and decides whether to find an item to pick up or run away to
	 * the farthest spot.
	 * 
	 * @param arena        - the playeArea the game is being played in
	 * @param playersInfo2 - the record of information of all the robots
	 * @param corners      - the list of the corners
	 */
	private void defensiveStrategy(Arena arena, GameRecord[] playersInfo2, Coordinates[] corners) {
		Coordinates killerCoord = new Coordinates(playersInfo2[killerIndex].getStreet(),
				playersInfo2[killerIndex].getAvenue());
		Coordinates currentCoord = new Coordinates(this.getStreet(), this.getAvenue());

		// If the estimated catch ability is high, or if the killer is really close, it
		// will go to the farthest spot. Otherwise, it will use the offensive strategy
		// and find an item to pick up.
		if (estimatedCatchAbility > 50 || findDistance(killerCoord, currentCoord) <= 4) {
			Coordinates farthestSpot = this.findFarthestSpot(arena, playersInfo2);
			this.moveTowards(farthestSpot);
		} else {
			this.offensiveStrategy(itemCoordinates);
		}
	}

	/**
	 * Uses bubble sort to determine the open spot the robot can get to within this
	 * turn that is also the farthest from the killer
	 * 
	 * @param arena        - the playeArea the game is being played in
	 * @param playersInfo2 - the record of information of all the robots
	 * @return - the farthest spot
	 */
	protected Coordinates findFarthestSpot(Arena arena, GameRecord[] playersInfo2) {
		// Gets killer's current location
		GameRecord killer = playersInfo2[killerIndex];
		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());

		// Gets a list of all empty spots in the arena
		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo2);

		// Compares and sorts the open spots
		for (int i = 0; i < openSpots.size() - 1; i++) {

			// Each pass bubbles the farthest spot to the front
			for (int j = 0; j < openSpots.size() - i - 1; j++) {

				// Finding distance from killer for adjacent spots
				int currentDistance = findDistance(openSpots.get(j), killerPosition);
				int nextDistance = findDistance(openSpots.get(j + 1), killerPosition);

				// If the next spot is farther from killer, swap them
				if (currentDistance < nextDistance) {
					Coordinates temp = openSpots.get(j);
					openSpots.set(j, openSpots.get(j + 1));
					openSpots.set(j + 1, temp);
				}
			}
		}

		// Returns the farthest open spot from killer
		if (openSpots.size() > 0) {
			return openSpots.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Gets spots that don't currently have a player there
	 * 
	 * @param arena        - the playeArea the game is being played in
	 * @param playersInfo2 - the record of information of all the robots
	 * @return - the ArrayList of empty spots in the playArea
	 */
	private ArrayList<Coordinates> getEmptySpots(Arena arena, GameRecord[] playersInfo2) {
		// Creates an ArrayList to store open spots in the arena
		ArrayList<Coordinates> openSpots = new ArrayList<Coordinates>();

		// Loops through every position in the arena grid by avenue
		for (int avenue = 0; avenue < arena.getWidth(); avenue++) {

			// Loops through every street for the current avenue
			for (int street = 0; street < arena.getHeight(); street++) {
				boolean occupied = false;

				// Loops through all player positions to check if any are on this spot
				for (int i = 0; i < playersInfo2.length; i++) {
					GameRecord currentRobot = playersInfo2[i];

					// If a robot is at this spot, marks it as occupied and stop checking
					if (currentRobot.getAvenue() == avenue && currentRobot.getStreet() == street) {
						occupied = true;
						break;
					}
				}

				// If no robot was found at this spot, add it to the list of open spots
				if (occupied == false) {
					openSpots.add(new Coordinates(street, avenue));
				}
			}
		}
		return openSpots;
	}
}
