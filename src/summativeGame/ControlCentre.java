package summativeGame;

import java.awt.Color;
import becker.robots.*;

/**
 * Control Center (application class) that runs the game
 * 
 * @author Samarvir, Aditya, Heer
 * @version June 13th, 2025
 */
public class ControlCentre {

	/**
	 * Main method that runs the game
	 */
	public static void main(String[] args) {

		Arena arena = new Arena();
		City city = arena.getCity();
		Coordinates[] itemCoordinates = arena.getListofItems();

		// Initializing the robots inside the arena
		int numPlayers = 5;
		GameRobot[] robots = new GameRobot[numPlayers];
		robots[0] = new MedicRobot(0, city, 11, 2, Direction.EAST, arena, 100, 5, "medic", 10, 0);
		robots[0].setColor(Color.BLACK);
		robots[1] = new CrewRobot(2, city, 2, 15, Direction.EAST, arena, 100, 7, "crew", 10, 0);
		robots[2] = new CrewRobot(3, city, 8, 10, Direction.EAST, arena, 100, 8, "crew", 10, 0);
		robots[3] = new CrewRobot(4, city, 10, 3, Direction.SOUTH, arena, 100, 8, "crew", 10, 0);
		robots[4] = new KillerBot(1, city, 2, 8, Direction.EAST, arena, 100, 5, "killer", 0, 100);
		robots[4].setColor(Color.BLUE);
	
		int index = 0;

		// call the moveTurn method for all robots
		while (true) {

			GameRecord[] record = getRecord(robots); // gets the updated record

			// if the robot is frozen, skip its turn
			if (!robots[index].isRobotFrozen()) {
				MoveStatus status = robots[index].moveTurn(record, index, itemCoordinates); // each robot take their
																							// turn

				// Updates the robot if any robot is frozen or unfrozen
				if (status != null && status.getTargetRobotId() != -1) {
					int targetId = status.getTargetRobotId();
					if (targetId >= 0 && targetId < robots.length) {

						// freeze the robot
						if (status.getOperation().equalsIgnoreCase("freeze")) {
							robots[targetId].setIsRobotFrozen(true);
							System.out.println("Robot " + robots[targetId].getID() + " (" + robots[targetId].getName()
									+ ") has been FROZEN!");
						}
						// unfreeze the robot
						else if (status.getOperation().equalsIgnoreCase("unfreeze")) {
							robots[targetId].setIsRobotFrozen(false);
							System.out.println("Robot " + robots[targetId].getID() + " (" + robots[targetId].getName()
									+ ") has been UNFROZEN!");
						}
					}
				}
			}

			index++;

			// Reset index to 0 after all robot moves their turn
			if (index == robots.length) {
				index = 0;
			}

			// Checking winning or loosing conditions for the game
			boolean allItemsDropped = isAllItemsDroppped(itemCoordinates);
			boolean allPlayerFrozen = isAllRobotFrozen(robots);

			// Crewmate and medic robots wins if all items are dropped to correct place
			if (allItemsDropped) {
				System.out.println("\n--- GAME OVER ---");
				System.out.println("Crew wins! All items successfully dropped.");
				break;
			}

			// Killer robot wins if all crew and medic robots are frozen
			if (allPlayerFrozen) {
				System.out.println("\n--- GAME OVER ---");
				System.out.println("Killer wins! All crewmates and medics frozen.");
				break;
			}
		}
	}

	/**
	 * Method that gives the updated record having all information of robots
	 * 
	 * @param robots : array of objects of all robots
	 * @return : returns the record of informations of all robots
	 */
	public static GameRecord[] getRecord(GameRobot[] robots) {

		GameRecord[] record = new GameRecord[robots.length];

		// geting the informations from robot array
		for (int i = 0; i < robots.length; i++) {
			record[i] = new GameRecord(robots[i].getID(), robots[i].getStreet(), robots[i].getAvenue(),
					robots[i].getEnergy(), robots[i].getRobotSpeed(), robots[i].getName(), robots[i].isRobotFrozen(),
					robots[i].getDodgeChance(), robots[i].getCatchChance());
		}
		return record;
	}

	/**
	 * Method to check if all items are dropped by the crewmates
	 * 
	 * @param itemCoordinates : Contains the informations of items
	 * @return : return whether all items are dropped to right place
	 */
	public static boolean isAllItemsDroppped(Coordinates[] itemCoordinates) {

		// Checks all items if they are dropped to correct location
		for (int i = 0; i < itemCoordinates.length; i++) {
			if (itemCoordinates[i].getIsFinalPosition() == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to check if all crew and medic robots are frozen
	 * 
	 * @param robots: array of objects of all robots
	 * @return : returns if the all crew and medic robots have frozen or not
	 */
	public static boolean isAllRobotFrozen(GameRobot[] robots) {

		// checks for all robots except for killer robot
		for (int i = 0; i < robots.length; i++) {
			if (!robots[i].getName().equalsIgnoreCase("killer")) {
				if (robots[i].isRobotFrozen() == false) {
					return false;
				}
			}
		}
		return true;
	}
}