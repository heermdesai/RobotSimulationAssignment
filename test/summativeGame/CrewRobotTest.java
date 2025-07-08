package summativeGame;

import java.awt.Color;

import becker.robots.City;
import becker.robots.Direction;

public class CrewRobotTest {

	public static void main(String[] args) {
		// testFindItem_shouldReturnFarthestItem();
		// testFindItem_shouldReturnClosestItem();
		// testFindClosestCorner_shouldReturnClosestCorner();
		// testFindFarthestSpot_shouldReturnFarthestSpot();
		// testIfTwoRobotsAndOneItem_oneShouldPick();
		// testIfTwoRobotsOnSameItem_oneShouldPick();
		// testNoItemsAvailable_shouldStayInSpot();
		// testIfRobotStartsOnItem_shouldPick();
		// testIfKillerNextToItem_shouldPickItem();
	}

	/**
	 * CASE 1: Avoiding Killer When Item Is Nearby
	 *
	 * Scenario: 
	 * - Two items on the map: one at (3, 2) and one at (9, 12) 
	 * - CrewRobot starts at (8, 10) 
	 * - Killer starts at (10, 12), right beside the closer
	 *   item
	 *
	 * Expected Behavior: 
	 * - CrewRobot should select the farther item at (3, 2) to
	 * avoid immediate danger 
	 * - Verifies AI prioritizes safety over proximity when selecting
	 *   items 
	 * - Ensures CrewRobot's pathfinding accounts for killer's position
	 */
	public static void testFindItem_shouldReturnFarthestItem() {
		Coordinates[] itemCoordinates = new Coordinates[2];
		itemCoordinates[0] = new Coordinates(3, 2);
		itemCoordinates[1] = new Coordinates(9, 12);
		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot = new CrewRobot(3, city, 8, 10, Direction.EAST, arena, 100, 100, "crew", 10, 0);
		KillerBot killerRobot = new KillerBot(3, city, 10, 12, Direction.EAST, arena, 100, 0, "killer", 10, 0);

		robots[0] = robot;
		robots[1] = killerRobot;
		Coordinates foundItem = robot.findItem(itemCoordinates, ControlCentre.getRecord(robots));

		if (foundItem.getStreet() == 3 && foundItem.getAvenue() == 2) {
			System.out.println("Closest safe location found!");
		} else {
			System.out.println("Faulty method. ");
		}

	}

	/**
	 * CASE 2: Pick the closest one when there is no killer
	 *
	 * Scenario:
	 *   - Two items on the map: one at (3, 2) and one at (9, 12)
	 *   - CrewRobot starts at (8, 10)
	 *   - There is no killer
	 *
	 * Expected Behavior:
	 *   - CrewRobot should select the farther item at (3, 2) because 
	 *     default killer index gets set to 0, which means the current crew 
	 *     robot gets counted as the killer. So, it should pick the further one 
	 *     to get away from "killer". This wouldn't happen in the actual
	 *     game as there wouldn't be a game without the killer.
	 */
	public static void testFindItem_shouldReturnClosestItem() {
		Coordinates[] itemCoordinates = new Coordinates[2];
		itemCoordinates[0] = new Coordinates(3, 2);
		itemCoordinates[1] = new Coordinates(9, 12);
		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[1];
		CrewRobot robot = new CrewRobot(3, city, 9, 10, Direction.EAST, arena, 100, 100, "crew", 10, 0);

		robots[0] = robot;
		Coordinates foundItem = robot.findItem(itemCoordinates, ControlCentre.getRecord(robots));

		if (foundItem.getStreet() == 3 && foundItem.getAvenue() == 2) {
			System.out.println("Closest location found");
		} else {
			System.out.println("Faulty method. ");
		}

	}

	/**
	 * CASE 3: Pick the closest corner where there is no killer
	 *
	 * Scenario:
	 *   - Two items on the map: one at (3, 2) and one at (9, 12)
	 *   - CrewRobot starts at (1, 2)
	 *   - Killer is at (8, 10)
	 *
	 * Expected Behavior:
	 *   - CrewRobot should select (0,0) as it is the closest corner that
	 *   	is also away from the killer. 
	 */
	public static void testFindClosestCorner_shouldReturnClosestCorner() {
		Coordinates[] itemCoordinates = new Coordinates[2];
		itemCoordinates[0] = new Coordinates(3, 2);
		itemCoordinates[1] = new Coordinates(9, 12);
		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot = new CrewRobot(3, city, 1, 2, Direction.EAST, arena, 100, 100, "crew", 10, 0);
		KillerBot killerRobot = new KillerBot(3, city, 8, 10, Direction.EAST, arena, 100, 0, "killer", 10, 0);
		killerRobot.setColor(Color.BLACK);

		robots[0] = robot;
		robots[1] = killerRobot;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		Coordinates[] sortedCorners = robot.findClosestCorner(arena.getCorners(), playersInfo);
		Coordinates targetCorner = sortedCorners[0];

		if (targetCorner.getStreet() == 0 && targetCorner.getAvenue() == 0) {
			System.out.println("Closest corner found");
		} else {
			System.out.println("Correct location: " + targetCorner);
			System.out.println("Faulty method. ");
		}

	}

	/**
	 * CASE 4: Pick the farthest spot from the killer
	 *
	 * Scenario:
	 *   - Two items on the map: one at (3, 2) and one at (9, 12)
	 *   - CrewRobot starts at (12, 1)
	 *   - Killer is at (12, 0)
	 *
	 * Expected Behavior:
	 *   - CrewRobot should select (0, 23) as it is the farthest 
	 *     away point from the killer
	 */
	public static void testFindFarthestSpot_shouldReturnFarthestSpot() {
		Coordinates[] itemCoordinates = new Coordinates[2];
		itemCoordinates[0] = new Coordinates(3, 2);
		itemCoordinates[1] = new Coordinates(9, 12);
		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot = new CrewRobot(3, city, 12, 1, Direction.EAST, arena, 25, 100, "crew", 10, 0);
		KillerBot killerRobot = new KillerBot(3, city, 12, 0, Direction.EAST, arena, 100, 0, "killer", 10, 0);
		killerRobot.setColor(Color.BLACK);

		robots[0] = robot;
		robots[1] = killerRobot;
		Coordinates foundItem = robot.findFarthestSpot(arena, ControlCentre.getRecord(robots));

		if (foundItem.getStreet() == 0 && foundItem.getAvenue() == 23) {
			System.out.println("Farthest location found");
		} else {
			System.out.println("Faulty method. ");
		}

	}

	/**
	 * CASE 5: There are two robots equally distanced from the item
	 *
	 * Scenario:
	 *   - One item on the map at (3, 2)
	 *   - One CrewRobot starts at (1, 1) and the other at (1, 3)
	 *   - No killer
	 *
	 * Expected Behavior:
	 *   - Only one of the CrewRobots should go and pick up the item
	 */
	public static void testIfTwoRobotsAndOneItem_oneShouldPick() {
		Coordinates[] itemCoordinates = new Coordinates[1];
		itemCoordinates[0] = new Coordinates(3, 2);
		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot1 = new CrewRobot(1, city, 1, 1, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);
		CrewRobot robot2 = new CrewRobot(2, city, 1, 3, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);

		robots[0] = robot1;
		robots[1] = robot2;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		robot1.moveTurn(playersInfo, 1, itemCoordinates);
		robot2.moveTurn(playersInfo, 1, itemCoordinates);
	}

	/**
	 * CASE 6: There are two robots starting on the same point, that also
	 * 			has an item there
	 *
	 * Scenario:
	 *   - One item on the map at (3, 2) and another at (5, 2)
	 *   - Two CrewRobots start at (3, 2)
	 *   - No killer
	 *
	 * Expected Behavior:
	 *   - Only one of the CrewRobots should pick up the item they start on,
	 *     the other should pick up the other item
	 */
	public static void testIfTwoRobotsOnSameItem_oneShouldPick() {
		Coordinates[] itemCoordinates = new Coordinates[2];
		itemCoordinates[0] = new Coordinates(3, 2);
		itemCoordinates[1] = new Coordinates(5, 2);

		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot1 = new CrewRobot(1, city, 3, 2, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);
		CrewRobot robot2 = new CrewRobot(2, city, 3, 2, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);

		robots[0] = robot1;
		robots[1] = robot2;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		for (int i = 0; i < 2; i++) {
			robot1.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 1 done moving");
			robot2.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 2 done moving");
		}
	}

	/**
	 * CASE 7: There are two robots starting on the same point, but there
	 * 			aren't any items for them to pick up
	 *
	 * Scenario:
	 *   - No items to pick up
	 *   - Two CrewRobots start at (3, 2)
	 *   - No killer
	 *
	 * Expected Behavior:
	 *   - The CrewRobots stay in their original spots
	 */
	public static void testNoItemsAvailable_shouldStayInSpot() {
		Coordinates[] itemCoordinates = new Coordinates[0];

		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot1 = new CrewRobot(1, city, 3, 2, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);
		CrewRobot robot2 = new CrewRobot(2, city, 3, 2, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);

		robots[0] = robot1;
		robots[1] = robot2;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		for (int i = 0; i < 2; i++) {
			robot1.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 1 done moving");
			robot2.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 2 done moving");
		}
	}

	/**
	 * CASE 8: There is one robot and it starts on the item
	 *
	 * Scenario:
	 *   - One item at (3, 2)
	 *   - One CrewRobot starts at (3, 2)
	 *   - No killer
	 *
	 * Expected Behavior:
	 *   - The CrewRobot should pick up the item at the start and
	 *     bring it to the corner
	 */
	public static void testIfRobotStartsOnItem_shouldPick() {
		Coordinates[] itemCoordinates = new Coordinates[1];
		itemCoordinates[0] = new Coordinates(3, 2);

		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[1];
		CrewRobot robot1 = new CrewRobot(1, city, 3, 2, Direction.SOUTH, arena, 100, 7, "crew", 10, 0);

		robots[0] = robot1;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		for (int i = 0; i < 2; i++) {
			robot1.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 1 done moving");
		}
	}

	/**
	 * CASE 9: There is one crew robot and one killer who is right
	 *         next to the item
	 *
	 * Scenario:
	 *   - One item at (3, 2)
	 *   - One CrewRobot starts at (5, 2)
	 *   - One KillerBot that starts at (3, 3)
	 *
	 * Expected Behavior:
	 *   - The CrewRobot should pick up the item if it is
	 *   	high on energy, as it then takes the offensive strategy
	 */
	public static void testIfKillerNextToItem_shouldPickItem() {
		Coordinates[] itemCoordinates = new Coordinates[1];
		itemCoordinates[0] = new Coordinates(3, 2);

		TestArena arena = new TestArena(itemCoordinates);
		City city = arena.buildCity();
		arena.itemCoordinates = itemCoordinates;
		arena.scatterThings(itemCoordinates);

		GameRobot[] robots = new GameRobot[2];
		CrewRobot robot1 = new CrewRobot(1, city, 5, 2, Direction.SOUTH, arena, 100, 4, "crew", 10, 0);
		KillerBot robot2 = new KillerBot(2, city, 3, 3, Direction.SOUTH, arena, 100, 4, "killer", 0, 55);
		robot2.setColor(Color.BLACK);

		robots[0] = robot1;
		robots[1] = robot2;
		GameRecord[] playersInfo = ControlCentre.getRecord(robots);

		for (int i = 0; i < 2; i++) {
			robot1.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 1 done moving");
			robot2.moveTurn(playersInfo, 1, itemCoordinates);
			System.out.println("robot 2 done moving");
		}
	}
}
