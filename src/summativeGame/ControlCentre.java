package summativeGame;

import java.awt.Color;
import java.util.ArrayList;

import becker.robots.*;
//import java.util.Random;

public class ControlCentre {
	public static void main(String[] args) {
		// Step 1: Create Arena
		Arena arena = new Arena();
		City city = arena.getCity();
		Coordinates[] itemCoordinates = arena.getListofItems();

		int numPlayers = 5;
		GameRobot[] robots = new GameRobot[numPlayers];
		robots[0] = new MedicRobot(1, city, 11, 2, Direction.EAST, arena, 100, 50, "medic", 10, 0);
		robots[1] = new CrewRobot(2, city, 2, 15, Direction.EAST, arena, 100, 7, "crew", 10, 0);
		robots[2] = new CrewRobot(3, city, 8, 10, Direction.EAST, arena, 100, 7, "crew", 10, 0);
		robots[3] = new CrewRobot(3, city, 8, 10, Direction.EAST, arena, 100, 7, "crew", 10, 0);
		robots[4] = new KillerBot(4, city, 2, 8, Direction.EAST, arena, 100, 5, "killer", 0, 0);

		robots[4].setColor(Color.ORANGE);
		robots[0].setColor(Color.MAGENTA);

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

			GameRecord[] record = getRecord(robots);

			MoveStatus status = new MoveStatus(-1, -1, "");
			// Updating record and robots move their turn
			if (!robots[index].isRobotFrozen()) {
				status = robots[index].moveTurn(record, index, itemCoordinates);
				// MoveMessage got returned
				// check moveMovessage
				// if targetRobotId and operation is freeze
				// this.freezeRobotById(targetRobotoId)
				// else operation is heal
				// this.unfreezeRobotById(targetRob
			}

			if (status != null && status.getTargetRobotId() != -1) {
				int targetId = status.getTargetRobotId();
				if (status.getOperation().equalsIgnoreCase("freeze")) {
					robots[targetId].setIsRobotFrozen(true);
				} else if (status.getOperation().equalsIgnoreCase("unfreeze")) {
					robots[targetId].setIsRobotFrozen(false);
				}
			}

			/*
			 * for(int i=0;i<robots.length;i++) {
			 * robots[i].setAvenue(record[i].getAvenue());
			 * robots[i].setStreet(record[i].getStreet());
			 * robots[i].setEnergy(record[i].getEnergy());
			 * robots[i].SetisRobotFrozen(record[i].isRobotFrozen());
			 * robots[i].setSpeed(record[i].getSpeed());
			 * robots[i].setDodgeChance(record[i].getDodgeChance());
			 * robots[i].setCatchChance(record[i].getCatchChance()); }
			 */

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
				System.out.println("Killer wins! All crewmates and medics frozen.");
			}
		}

	}

	public static GameRecord[] getRecord(GameRobot[] robots) {

		GameRecord[] record = new GameRecord[robots.length];

		for (int i = 0; i < robots.length; i++) {
			// street, avenue, energy, speed(double), type, frozen, dodgeChance, catchChance
			record[i] = new GameRecord(robots[i].getID(), robots[i].getStreet(), robots[i].getAvenue(),
					robots[i].getEnergy(), robots[i].getSpeed(), robots[i].getName(), robots[i].isRobotFrozen(),
					robots[i].getDodgeChance(), robots[i].getCatchChance());
		}
		return record;
	}

	public static boolean isAllItemsDroppped(Coordinates[] itemCoordinates) {

		for (int i = 0; i < itemCoordinates.length; i++) {
			if (itemCoordinates[i].getIsFinalPosition() == false) {
				return false;
			}
		}
		return true;
	}

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
