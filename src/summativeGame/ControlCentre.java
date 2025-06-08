package summativeGame;

import java.util.ArrayList;

import becker.robots.*;
//import java.util.Random;

public class ControlCentre {
	public static void main(String[] args) {
		// Step 1: Create Arena
		Arena arena = new Arena();
		City city = arena.getCity();
		Coordinates[] itemCoordinates = arena.getListofItems();

		int numThings = 6;
		int numPlayers = 2;

		GameRobot[] robots = new GameRobot[numPlayers];
		// robots[0]=new medicRobot(city, 5, 10,Direction.EAST, arena, 100,
		// 20,"medic",10,0);
		robots[0] = new CrewRobot(city, 8, 10, Direction.EAST, arena, 20, 2, "crew", 10, 0);
		robots[1] = new CrewRobot(city, 2, 5, Direction.NORTH, arena, 79, 7, "crew", 5, 0);
		//robots[2] = new killerBot(city, 0, 0, Direction.NORTH, arena, 79, 7, "killer", 0, 10);
		// robots[2]=new KillerBot(city, 6, 12,Direction.EAST, arena, 100,
		// 20,"killer",10,0);

		// Testing X and Y location for Robots
//			for(int j=0;j<numPlayers;j++) {
//				System.out.print(robots[j].getAvenue() + " " );
//				System.out.print(robots[j].getStreet() + " " );
//
//			}
//			System.out.println();

		// Testing X, Y and IsPicked for items
//			for(int j=0;j<numThings;j++) {
//				//System.out.print(robots[1].getItemInfo()[j].getIsPicked() + " ");
//				//System.out.print(robots[1].getItemInfo()[j].getAvenue() + " ");
//				//System.out.print(robots[1].getItemInfo()[j].getStreet()+ " ");
//				System.out.println(itemCoordinates[j]);
//
//			}
		// System.out.println();

		// Updating record and robots move their turn
		// robots=robots[i].moveTurn(robots,i,itemCoordinates);

		boolean allPlayerFrozen = false;
		boolean allItemsDropped = false;
		int index = 0;

		// need a winning and loosing condition
		while (!(allPlayerFrozen || allItemsDropped)) {

			// Updating record and robots move their turn
			if (!robots[index].isRobotFrozen()) {
				robots = robots[index].moveTurn(robots, index, itemCoordinates);
			}

			index++;
			if (index == robots.length) {
				index = 0;
			}

			allPlayerFrozen = isAllRobotFrozen(robots);
			allItemsDropped = isAllItemsDroppped(itemCoordinates);

			if (allItemsDropped) {
				System.out.println("Crew wins! All items successfully dropped.");
			}

			if (allPlayerFrozen) {
				System.out.println("Killer wins! All crewmates frozen.");
			}
		}

				}

//		for(int i = 0; i < itemCoordinates.length; i++) {
//			System.out.println(itemCoordinates[i]);
//		}

	// Method 1
	public static boolean isAllItemsDroppped(Coordinates[] itemCoordinates) {

		for (int i = 0; i < itemCoordinates.length; i++) {
			if (itemCoordinates[i].getIsFinalPosition() == false) {
				return false;
			}
		}
		return true;
	}

	// method 2
	public static boolean isAllRobotFrozen(GameRobot[] robots) {
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
