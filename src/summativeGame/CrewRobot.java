package summativeGame;

import java.util.ArrayList;
import becker.robots.City;
import becker.robots.Direction;

public class CrewRobot extends GameRobot {
	private int dodgingAbility;
	private String type;
	private Coordinates[] itemCoordinates;
	private GameRobot[] playersInfo;
	private Arena arena;

	private int killerIndex;
	private Coordinates currentTarget;

	public CrewRobot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type,
			int dodgingAbility, int catchAbility) {
		super(c, street, avenue, d, arena, energy, speed, type, dodgingAbility, catchAbility);
		this.type = "crew";
		this.dodgingAbility = dodgingAbility;
		catchAbility = 0;
		this.arena = arena;
	}

	public GameRobot[] moveTurn(GameRobot[] robots, int i, Coordinates[] itemCoordinates) {
		System.out.println(this.getName() + " has " + this.getEnergy() + " energy left.");
		this.itemCoordinates = itemCoordinates;
		this.playersInfo = robots;

		int index;
		for (index = 0; index < playersInfo.length; index++) {
			GameRobot player = playersInfo[index];
			if (player.getName().compareTo("killer") == 0) {
				this.killerIndex = index;
			}
		}
		// System.out.println("killerIndex: " + killerIndex);

//		lastXLocation = new int[this.playersInfo.length];
//		lastYLocation = new int[this.playersInfo.length];

//		for (int j = 0; j < this.playersInfo.length; j++) {
//			lastXLocation[j] = this.playersInfo[j].getAvenue();
//			lastYLocation[j] = this.playersInfo[j].getStreet();
//		}

		if (this.getEnergy() <= 0) {
			this.setEnergy(this.getEnergy() + 90);
		} else if (this.getEnergy() >= 50) {
			System.out.println("taking offensive stretegy.");
			this.offensiveStrategy(itemCoordinates, this.playersInfo, arena.getCorners());
			System.out.println("took offensive stretegy.");
		} else {
			System.out.println("taking defensive stretegy.");
			this.defensiveStrategy(arena, this.playersInfo);
			System.out.println("took defensive stretegy.");
		}

		this.playersInfo[i].setAvenue(this.getAvenue());
		this.playersInfo[i].setStreet(this.getStreet());

		robots = playersInfo;
		return robots;
	}

//	private int findSpeed(GameRobot[] playersInfo, int index) {
//		GameRobot currentPlayer = playersInfo[index];
//		int speed = (lastXLocation[index] - currentPlayer.getAvenue())
//				+ (lastYLocation[index] - currentPlayer.getStreet());
//
//		return speed;
//	}

	private void offensiveStrategy(Coordinates[] itemCoordinates, GameRobot[] playersInfo, Coordinates[] corners) {
		// Coordinates nextItem = this.findItem(itemCoordinates, playersInfo);
		Coordinates[] sortedCornersList = findClosestCorner(corners);

		if (this.countThingsInBackpack() == 0) {
			if (currentTarget == null || currentTarget.getIsPicked() == true) {
				currentTarget = this.findItem(itemCoordinates, playersInfo);
			}
			// this.pickUpItem(nextItem);
			// System.out.println("speed: " + this.getRobotSpeed());
			this.moveTowards(currentTarget);
			if (this.canPickThing()) {
				this.pickThing(1);
				currentTarget.setIsPicked(true);
				currentTarget = null;
			}
			else {
				System.out.println("NOT PICKING ANYTHING");
			}
		} else {
			if (currentTarget == null) {
				this.goToCorner(sortedCornersList);
			}

			if (this.getAvenue() == sortedCornersList[0].getAvenue()
					&& this.getStreet() == sortedCornersList[0].getStreet()) {
				this.putThing();
				currentTarget = null;
			}
		}
	}

	public Coordinates[] findClosestCorner(Coordinates[] corners) {
		for (int i = 0; i < corners.length; i++) {
			int minIndex = i;
			for (int j = i + 1; j < corners.length; j++) {
				if (findDistance(corners[j].getAvenue(), corners[j].getStreet()) < findDistance(
						corners[minIndex].getAvenue(), corners[minIndex].getStreet())) {
					minIndex = j;
				}
			}
			Coordinates temp = corners[i];
			corners[i] = corners[minIndex];
			corners[minIndex] = temp;
		}
		return corners;
	}

	public void moveTowards(Coordinates target) {
		int numSteps = this.getRobotSpeed();
		while (numSteps > 0 && this.getEnergy() > 0) {
			if (this.getAvenue() > target.getAvenue()) {
				this.faceWest();
				this.move();
			} else if (this.getAvenue() < target.getAvenue()) {
				this.faceEast();
				this.move();
			} else if (this.getStreet() > target.getStreet()) {
				this.faceNorth();
				this.move();
			} else if (this.getStreet() < target.getStreet()) {
				this.faceSouth();
				this.move();
			} else {
				break;
			}
			numSteps--;
		}

	}

	// ADD FUNCTIONALITY TO CHECK WHERE OTHER PLAYERS ARE
	private Coordinates findItem(Coordinates[] itemCoordinates, GameRobot[] playersInfo) {
		for (int j = 0; j < itemCoordinates.length - 1; j++) {
			for (int k = 1; k < itemCoordinates.length - j; k++) {
				Coordinates item1 = itemCoordinates[k - 1];
				Coordinates item2 = itemCoordinates[k];

				if (findDistance(item1.getStreet(), item1.getAvenue()) > findDistance(item2.getStreet(),
						item2.getAvenue()) || (item1.getIsPicked() == true && item2.getIsPicked() == false)) {
					// Swap in the actual array
					Coordinates temp = itemCoordinates[k - 1];
					itemCoordinates[k - 1] = itemCoordinates[k];
					itemCoordinates[k] = temp;
				}
			}
		}
//		for (int i = 0; i < itemCoordinates.length; i++) {
//			System.out.print(itemCoordinates[i]);
//		}
//		System.out.println();

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

//	private void pickUpItem(Coordinates item) {
//		this.moveTowards(item);
//		// this.goToItem(item);
//		this.pickItem(item);
//	}

//	private void goToItem(Coordinates item) {
//		this.moveHorizontal(item);
//		this.moveVertical(item);
//	}

//	private void pickItem(Coordinates item) {
//		this.pickThing(1);
//		item.setIsPicked(true);
//	}

//	private void moveVertical(Coordinates item) {
//		int yCoordinate = this.getStreet();
//		int stepsToMove = 0;
//		if (yCoordinate < item.getStreet()) {
//			this.faceSouth();
//			stepsToMove = item.getStreet() - yCoordinate;
//		} else {
//			this.faceNorth();
//			stepsToMove = yCoordinate - item.getStreet();
//		}
//		this.move(stepsToMove);
//	}

//	private void moveHorizontal(Coordinates item) {
//		int xCoordinate = this.getAvenue();
//		int stepsToMove = 0;
//		if (xCoordinate > item.getAvenue()) {
//			this.faceWest();
//			stepsToMove = xCoordinate - item.getAvenue();
//		} else {
//			this.faceEast();
//			stepsToMove = item.getAvenue() - xCoordinate;
//		}
//
//		this.move(stepsToMove);
//	}

	private void faceWest() {
		while (this.isFacingWest() == false) {
			this.turnRight();
		}
	}

	private void faceEast() {
		while (this.isFacingEast() == false) {
			this.turnRight();
		}
	}

	private void faceSouth() {
		while (this.isFacingSouth() == false) {
			this.turnRight();
		}
	}

	private void faceNorth() {
		while (this.isFacingNorth() == false) {
			this.turnRight();
		}
	}

//	private void depositItem(Coordinates[] cornersList) {
//		this.goToCorner(cornersList);
//		this.putThing();
//		
//	}

	private void goToCorner(Coordinates[] cornersList) {
		Coordinates[] sortedCornersList = cornersList;
		Coordinates closestCorner = sortedCornersList[0];
		this.moveTowards(closestCorner);
	}

	private void defensiveStrategy(Arena arena, GameRobot[] playersInfo) {
		Coordinates safeSpot = this.findSafeSpot(arena, playersInfo);
		this.moveTowards(safeSpot);
	}

	private Coordinates findSafeSpot(Arena arena, GameRobot[] playersInfo) {
		GameRobot killer = playersInfo[killerIndex];
		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());

		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);

		for (int i = 1; i < openSpots.size(); i++) {
			Coordinates spot = openSpots.get(i);
			int distanceToSpot = findDistance(spot, killerPosition);
			int j = i - 1;
			// while the element is at index greater than 0, and the account number is
			// greater than the one to the left of it, the two elements swap places. We
			// reduce i by 1 every time to build up the "sorted" section, and go through the
			// "unsorted" section every time.
			while (j >= 0 && (findDistance(openSpots.get(j), killerPosition)) < distanceToSpot) {
				openSpots.set(j + 1, openSpots.get(j));
				j--;
			}
			openSpots.set(j + 1, spot);
		}

		if (openSpots.size() > 0) {
			return openSpots.get(0);
		} else {
			return null;
		}
	}

	private ArrayList<Coordinates> getEmptySpots(Arena arena, GameRobot[] playersInfo) {
		ArrayList<Coordinates> openSpots = new ArrayList<Coordinates>();
		for (int avenue = 0; avenue < arena.getWidth(); avenue++) {
			for (int street = 0; street < arena.getHeight(); street++) {
				boolean occupied = false;
				for (int i = 0; i < playersInfo.length; i++) {
					GameRobot currentRobot = playersInfo[i];
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