package summativeGame;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import becker.robots.City;
import becker.robots.Direction;

public class CrewRobot extends GameRobot {
	private int dodgingAbility;
	private String type;
	private Coordinates[] itemCoordinates;
	private GameRecord[] playersInfo;
	private Arena arena;

	private int killerIndex;
	private Coordinates currentTarget;
	private Coordinates currentCornerTarget;

	private ArrayList<Coordinates> recentPositions = new ArrayList<Coordinates>();
	private int estimatedCatchAbility = 50;
	private int totalEncounters = 0;
	private int successfulDodges = 0;
	private int id;

	public CrewRobot(int id, City c, int street, int avenue, Direction d, Arena arena, int energy, int speed,
			String type, int dodgingAbility, int catchAbility) {
		super(id, c, street, avenue, d, arena, energy, speed, type, dodgingAbility, catchAbility);
		this.type = "crew";
		// this.dodgingAbility = dodgingAbility;
		catchAbility = 0;
		this.arena = arena;
	}

	public void move() {
		// System.out.println(this.getName() + " moving to (" + this.getStreet() + ", "
		// + this.getAvenue() + ")");

		if (this.getEnergy() <= 0) {
			return;
		}
		super.move();
		this.setEnergy(this.getEnergy() - 1);
	}

//	public void setIsRobotFrozen(boolean isFrozen) {
//		boolean dodged = false;
//		
//		dodged = this.tryToDodge();             
//        this.recordGotFrozen(dodged);
//		this.frozen = dodged;
//
//	}

	public void SetisRobotFrozen(boolean isFrozen) {
		if (isFrozen) {
			boolean dodged = this.tryToDodge();
			this.recordGotFrozen(dodged);

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

	public MoveStatus moveTurn(GameRecord[] robots, int i, Coordinates[] itemCoordinates) {
		// System.out.println(this.getName() + " has " + this.getEnergy() + " energy
		// left.");
		this.itemCoordinates = itemCoordinates;
		this.playersInfo = robots;

		int index;
		for (index = 0; index < playersInfo.length; index++) {
			GameRecord player = playersInfo[index];
			if (player.getName().compareTo("killer") == 0) {
				this.killerIndex = index;
			}
		}

		if (this.getEnergy() <= 0) {
			System.out.println(this.id + " setting energy to 90.");
			this.setEnergy(this.getEnergy() + 90);
		} else if (this.getEnergy() >= 30) {
			// System.out.println(this.id + "taking offensive stretegy.");
			this.offensiveStrategy(itemCoordinates);
			// System.out.println(this.id + "took offensive stretegy.");
		} else {
			// System.out.println(this.id + "taking defensive stretegy.");
			this.defensiveStrategy(arena, this.playersInfo, arena.getCorners());
			// System.out.println(this.id + "took defensive stretegy.");
		}

		robots = playersInfo;
		return new MoveStatus(this.id, -1, " ");
	}

	private void offensiveStrategy(Coordinates[] itemCoordinates) {
		// Coordinates[] sortedCornersList = findClosestCorner(corners);

//		for (int i = 0; i < sortedCornersList.length; i++) {
//			System.out.println(sortedCornersList[i]);
//	}

		System.out.println(this.id + "Using offensive strategy.");

		if (this.countThingsInBackpack() == 0) {
			if (this.currentTarget == null) {
				currentTarget = this.findItem(itemCoordinates, playersInfo);
				System.out.println(this.id + "current target is " + currentTarget);
			}
			if (this.currentTarget != null) {
				System.out.println(this.id + "offensive strategy moving item toward " + this.currentTarget);
				this.moveTowards(currentTarget);
				if (this.getStreet() == currentTarget.getStreet() && this.getAvenue() == currentTarget.getAvenue()) {
					System.out.println(this.id + " on the location picking thing");
					if (this.canPickThing()) {
						this.pickThing();
						currentTarget.setIsPicked(true);
						System.out.println(this.id + "picked thing");

					} else {
						System.out.println(this.id + " can't pick");
						currentTarget = null;
					}
					// currentTarget = null;
				} else {
					System.out.println(this.id + "on the way " + this.currentTarget);
				}
			}
			// }
		} else {
			System.out.println(this.id + "Dropping thing now.");
			if (currentCornerTarget == null) {
				Coordinates[] sortedCornersList = findClosestCorner(this.arena.getCorners());
				currentCornerTarget = sortedCornersList[0];
			}
			this.moveTowards(currentCornerTarget);
			if (this.getStreet() == currentCornerTarget.getStreet()
					&& this.getAvenue() == currentCornerTarget.getAvenue()) {
				this.putThing();
				currentTarget.setIsFinalPosition(true);
				currentCornerTarget = null;
				currentTarget = null;
			}
		}
	}

	private Coordinates[] findClosestCorner(Coordinates[] corners) {
		Coordinates killerCoord = new Coordinates(playersInfo[killerIndex].getStreet(),
				playersInfo[killerIndex].getAvenue());
		Coordinates[] sortedCorners = corners;

		for (int i = 0; i < sortedCorners.length; i++) {
			int minIndex = i;
			for (int j = i + 1; j < sortedCorners.length; j++) {
				int currentKillerDistance = findDistance(killerCoord, sortedCorners[j]);
				int bestKillerDistance = findDistance(sortedCorners[minIndex].getAvenue(),
						sortedCorners[minIndex].getStreet());

				if (currentKillerDistance > bestKillerDistance) {
					minIndex = j;
				} else if (currentKillerDistance == bestKillerDistance) {
					int currentSelfDistance = findDistance(sortedCorners[j].getAvenue(), sortedCorners[j].getStreet());
					int bestSelfDistance = findDistance(sortedCorners[minIndex].getAvenue(),
							sortedCorners[minIndex].getStreet());

					if (currentSelfDistance < bestSelfDistance) {
						minIndex = j;
					}
				}
			}
			Coordinates temp = sortedCorners[i];
			sortedCorners[i] = sortedCorners[minIndex];
			sortedCorners[minIndex] = temp;
		}
		return sortedCorners;
	}

	public void moveTowards(Coordinates target) {
		int numSteps = this.getRobotSpeed();
		while (numSteps > 0 && this.getEnergy() > 0) {
			Coordinates nextSpot;
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

			if (recentPositions.contains(nextSpot)) {
				break;
			}
			System.out.println(this.id + "calling move() method now to nextSpot " + nextSpot);
			this.move();
			numSteps--;

			recentPositions.add(new Coordinates(this.getStreet(), this.getAvenue()));
			if (recentPositions.size() > 3) {
				recentPositions.remove(0);
			}
		}

	}

	private Coordinates findItem(Coordinates[] itemCoordinates, GameRecord[] playersInfo2) {
		for (int j = 0; j < itemCoordinates.length - 1; j++) {
			for (int k = 1; k < itemCoordinates.length - j; k++) {
				Coordinates item1 = itemCoordinates[k - 1];
				Coordinates item2 = itemCoordinates[k];

				if (findDistance(item1.getStreet(), item1.getAvenue()) > findDistance(item2.getStreet(),
						item2.getAvenue()) || (item1.getIsPicked() == true && item2.getIsPicked() == false)) {
					Coordinates temp = itemCoordinates[k - 1];
					itemCoordinates[k - 1] = itemCoordinates[k];
					itemCoordinates[k] = temp;
				}
			}
		}
		return itemCoordinates[0];
	}

	private int findDistance(int avenue, int street) {
		int distance;
		distance = Math.abs(this.getAvenue() - avenue) + Math.abs(this.getStreet() - street);

		return distance;
	}

	private int findDistance(Coordinates coord1, Coordinates coord2) {
		int distance;
		distance = Math.abs(coord1.getAvenue() - coord2.getAvenue())
				+ Math.abs(coord1.getStreet() - coord2.getStreet());

		return distance;
	}

	private void changeDirection(Direction d) {
		while (this.getDirection() != d) {
			this.turnRight();
		}
	}

	public boolean tryToDodge() {
		int chance = (int) (Math.random() * 100);
		return (this.dodgingAbility + chance) >= 50;
	}

	public void recordGotFrozen(boolean escaped) {
		totalEncounters++;
		if (escaped == true) {
			successfulDodges++;
		}

		estimatedCatchAbility = 100 - (int) (((double) successfulDodges / totalEncounters) * 100);
		System.out.println(this.id + "estimated catchability = " + estimatedCatchAbility);
	}

//	private void goToCorner(Coordinates[] cornersList) {
//		Coordinates[] sortedCornersList = cornersList;
//		Coordinates closestCorner = sortedCornersList[0];
//		this.moveTowards(closestCorner);
//	}

	private void defensiveStrategy(Arena arena, GameRecord[] playersInfo2, Coordinates[] corners) {
		Coordinates killerCoord = new Coordinates(playersInfo2[killerIndex].getStreet(),
				playersInfo2[killerIndex].getAvenue());
		Coordinates currentCoord = new Coordinates(this.getStreet(), this.getAvenue());

		// ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);

		if (estimatedCatchAbility > 50 || findDistance(killerCoord, currentCoord) <= 4) {
			System.out.println(this.id + "Using defensive strategy.");
			Coordinates farthestSpot = this.findFarthestSpot(arena, playersInfo2);
			this.moveTowards(farthestSpot);
		} else {
			System.out.println(this.id + "Using offensive strategy inside defensive strategy.");

			this.offensiveStrategy(itemCoordinates);
//            Coordinates safeSpot = this.findSafeSpot(arena, playersInfo);
//            this.moveTowards(safeSpot);
		}
	}

//	private Coordinates findSafeSpot(Arena arena, GameRobot[] playersInfo) {
//		GameRobot killer = playersInfo[killerIndex];
//		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());
//		Coordinates currentPosition = new Coordinates(this.getStreet(), this.getAvenue());
//
//		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);
//		ArrayList<Coordinates> reachableSpots = new ArrayList<Coordinates>();
//
//		for (int i = 0; i < reachableSpots.size(); i++) {
//			Coordinates openSpot = reachableSpots.get(i);
//			int distance = findDistance(currentPosition, openSpot);
//			if (distance <= this.getSpeed()) {
//				reachableSpots.add(openSpot);
//			}
//		}
//
//		for (int i = 1; i < reachableSpots.size(); i++) {
//			Coordinates spot = reachableSpots.get(i);
//			int distanceToSpot = findDistance(spot, killerPosition);
//			int j = i - 1;
//
//			while (j >= 0 && (findDistance(reachableSpots.get(j), killerPosition)) < distanceToSpot) {
//				reachableSpots.set(j + 1, openSpots.get(j));
//				j--;
//			}
//			reachableSpots.set(j + 1, spot);
//		}
//
//		if (reachableSpots.size() > 0) {
//			return reachableSpots.get(0);
//		} else {
//			return null;
//		}
//	}

	private Coordinates findFarthestSpot(Arena arena, GameRecord[] playersInfo2) {
		GameRecord killer = playersInfo2[killerIndex];
		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());

		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo2);

		for (int i = 0; i < openSpots.size() - 1; i++) {
			for (int j = 0; j < openSpots.size() - i - 1; j++) {
				int currentDistance = findDistance(openSpots.get(j), killerPosition);
				int nextDistance = findDistance(openSpots.get(j + 1), killerPosition);
				if (currentDistance < nextDistance) {
					Coordinates temp = openSpots.get(j);
					openSpots.set(j, openSpots.get(j + 1));
					openSpots.set(j + 1, temp);
				}
			}
		}
		if (openSpots.size() > 0) {
			return openSpots.get(0);
		} else {
			return null;
		}
	}

	private ArrayList<Coordinates> getEmptySpots(Arena arena, GameRecord[] playersInfo2) {
		ArrayList<Coordinates> openSpots = new ArrayList<Coordinates>();
		for (int avenue = 0; avenue < arena.getWidth(); avenue++) {
			for (int street = 0; street < arena.getHeight(); street++) {
				boolean occupied = false;
				for (int i = 0; i < playersInfo2.length; i++) {
					GameRecord currentRobot = playersInfo2[i];
					if (currentRobot.getAvenue() == avenue && currentRobot.getStreet() == street) {
						occupied = true;
						break;
					}
				}
				if (occupied == false) {
					openSpots.add(new Coordinates(street, avenue));
				}
			}
		}
		return openSpots;
	}
}