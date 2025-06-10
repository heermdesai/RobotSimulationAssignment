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

	private ArrayList<Coordinates> recentPositions = new ArrayList<Coordinates>();
	private int estimatedCatchAbility = 50;
	private int totalEncounters = 0;
	private int successfulDodges = 0;

	public CrewRobot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type,
			int dodgingAbility, int catchAbility) {
		super(c, street, avenue, d, arena, energy, speed, type, dodgingAbility, catchAbility);
		this.type = "crew";
		this.dodgingAbility = dodgingAbility;
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

	public GameRobot[] moveTurn(GameRobot[] robots, int i, Coordinates[] itemCoordinates) {
		// System.out.println(this.getName() + " has " + this.getEnergy() + " energy
		// left.");
		this.itemCoordinates = itemCoordinates;
		this.playersInfo = robots;

		int index;
		for (index = 0; index < playersInfo.length; index++) {
			GameRobot player = playersInfo[index];
			if (player.getName().compareTo("killer") == 0) {
				this.killerIndex = index;
			}
		}

		if (this.getEnergy() <= 0) {
			this.setEnergy(this.getEnergy() + 90);
		} else if (this.getEnergy() >= 20) {
			// System.out.println("taking offensive stretegy.");
			this.offensiveStrategy(itemCoordinates, this.playersInfo, arena.getCorners());
			// System.out.println("took offensive stretegy.");
		} else {
			// System.out.println("taking defensive stretegy.");
			this.defensiveStrategy(arena, this.playersInfo, arena.getCorners());
			// System.out.println("took defensive stretegy.");
		}

		this.playersInfo[i].setAvenue(this.getAvenue());
		this.playersInfo[i].setStreet(this.getStreet());

		robots = playersInfo;
		return robots;
	}

	private void offensiveStrategy(Coordinates[] itemCoordinates, GameRobot[] playersInfo, Coordinates[] corners) {
		Coordinates[] sortedCornersList = findClosestCorner(corners);

//		for (int i = 0; i < sortedCornersList.length; i++) {
//			System.out.println(sortedCornersList[i]);
//		}

		if (this.countThingsInBackpack() == 0) {
			if (currentTarget == null || currentTarget.getIsPicked() == true) {
				currentTarget = this.findItem(itemCoordinates, playersInfo);
			}

			if (this.currentTarget != null) {
				this.moveTowards(currentTarget);
				if (this.canPickThing()) {
					this.pickThing(1);
					currentTarget.setIsPicked(true);
					currentTarget = null;
				}
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
		Coordinates killerCoord = new Coordinates(playersInfo[killerIndex].getStreet(),
				playersInfo[killerIndex].getAvenue());

		for (int i = 0; i < corners.length; i++) {
			int minIndex = i;
			for (int j = i + 1; j < corners.length; j++) {
				int currentKillerDistance = findDistance(killerCoord, corners[j]);
				int bestKillerDistance = findDistance(corners[minIndex].getAvenue(), corners[minIndex].getStreet());

				if (currentKillerDistance > bestKillerDistance) {
					minIndex = j;
				} else if (currentKillerDistance == bestKillerDistance) {
					int currentSelfDistance = findDistance(corners[j].getAvenue(), corners[j].getStreet());
					int bestSelfDistance = findDistance(corners[minIndex].getAvenue(), corners[minIndex].getStreet());

					if (currentSelfDistance < bestSelfDistance) {
						minIndex = j;
					}
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
			Coordinates nextSpot;
			if (this.getAvenue() > target.getAvenue()) {
				this.faceWest();
				nextSpot = new Coordinates(this.getStreet(), this.getAvenue() - 1);
			} else if (this.getAvenue() < target.getAvenue()) {
				this.faceEast();
				nextSpot = new Coordinates(this.getStreet(), this.getAvenue() + 1);
			} else if (this.getStreet() > target.getStreet()) {
				this.faceNorth();
				nextSpot = new Coordinates(this.getStreet() - 1, this.getAvenue());
			} else if (this.getStreet() < target.getStreet()) {
				this.faceSouth();
				nextSpot = new Coordinates(this.getStreet() + 1, this.getAvenue());
			} else {
				break;
			}

			if (recentPositions.contains(nextSpot)) {
				break;
			}

			this.move();
			numSteps--;

			recentPositions.add(new Coordinates(this.getStreet(), this.getAvenue()));
			if (recentPositions.size() > 3) {
				recentPositions.remove(0);
			}
		}

	}

//	        // Only move if this isn't a loop move
//	        if (recentPositions.contains(nextPosition)) {
//	            break;
//	        }
//
//	        this.move();
//	        numSteps--;
//
//	        // Update move history
//	        recentPositions.add(new Coordinates(this.getStreet(), this.getAvenue()));
//	        if (recentPositions.size() > 3) {
//	            recentPositions.remove(0);  // keep recent 3 moves
//	        }
//	    }
//	}

	// ADD FUNCTIONALITY TO CHECK WHERE OTHER PLAYERS ARE
	private Coordinates findItem(Coordinates[] itemCoordinates, GameRobot[] playersInfo) {
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
		System.out.println("estimated catchability = " + estimatedCatchAbility);
	}

	private void goToCorner(Coordinates[] cornersList) {
		Coordinates[] sortedCornersList = cornersList;
		Coordinates closestCorner = sortedCornersList[0];
		this.moveTowards(closestCorner);
	}

	private void defensiveStrategy(Arena arena, GameRobot[] playersInfo, Coordinates[] corners) {
		Coordinates killerCoord = new Coordinates(playersInfo[killerIndex].getStreet(),
				playersInfo[killerIndex].getAvenue());
		Coordinates currentCoord = new Coordinates(this.getStreet(), this.getAvenue());

		// ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);

		if (estimatedCatchAbility > 60 && findDistance(killerCoord, currentCoord) <= 4) {
			Coordinates farthestSpot = this.findFarthestSpot(arena, playersInfo);
			this.moveTowards(farthestSpot);
		} else {
			this.offensiveStrategy(itemCoordinates, playersInfo, corners);
//            Coordinates safeSpot = this.findSafeSpot(arena, playersInfo);
//            this.moveTowards(safeSpot);
		}
	}

	private Coordinates findSafeSpot(Arena arena, GameRobot[] playersInfo) {
		GameRobot killer = playersInfo[killerIndex];
		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());
		Coordinates currentPosition = new Coordinates(this.getStreet(), this.getAvenue());

		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);
		ArrayList<Coordinates> reachableSpots = new ArrayList<Coordinates>();

		for (int i = 0; i < reachableSpots.size(); i++) {
			Coordinates openSpot = reachableSpots.get(i);
			int distance = findDistance(currentPosition, openSpot);
			if (distance <= this.getSpeed()) {
				reachableSpots.add(openSpot);
			}
		}

		for (int i = 1; i < reachableSpots.size(); i++) {
			Coordinates spot = reachableSpots.get(i);
			int distanceToSpot = findDistance(spot, killerPosition);
			int j = i - 1;

			while (j >= 0 && (findDistance(reachableSpots.get(j), killerPosition)) < distanceToSpot) {
				reachableSpots.set(j + 1, openSpots.get(j));
				j--;
			}
			reachableSpots.set(j + 1, spot);
		}

		if (reachableSpots.size() > 0) {
			return reachableSpots.get(0);
		} else {
			return null;
		}
	}

	private Coordinates findFarthestSpot(Arena arena, GameRobot[] playersInfo) {
		GameRobot killer = playersInfo[killerIndex];
		Coordinates killerPosition = new Coordinates(killer.getStreet(), killer.getAvenue());

		ArrayList<Coordinates> openSpots = this.getEmptySpots(arena, playersInfo);

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