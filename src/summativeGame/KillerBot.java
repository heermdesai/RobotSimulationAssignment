package summativeGame;

import java.util.ArrayList;
import becker.robots.*;
import java.util.Random;

public class KillerBot extends GameRobot {

	private Arena arena;
	private String type;
	private int catchChance;

	private Coordinates[] lastPositions;
	private int[] dodgeMemory;
	private int[] robotSpeeds;

	private GameRobot[] playersinfo;

	private Coordinates currentTargetCoordinates;
	private GameRobot currentTargetRobot;

	private static final int AGGRESSIVE_ETHRESHOLD = 30;
	private static final int FREEZE_ECOST = 10;
	private static final int RECHARGE_RATE = 5;

	private Random rand;

	public KillerBot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type,
			int dodgeChance, int catchChance) {
		super(c, street, avenue, d, arena, energy, speed, type, dodgeChance, catchChance);

		this.arena = arena;
		this.type = "killer";
		this.catchChance = catchChance;

		this.lastPositions = new Coordinates[0];
		this.dodgeMemory = new int[0];
		this.robotSpeeds = new int[0];

		this.rand = new Random();
	}

	public GameRobot[] moveTurn(GameRobot[] robots, int i, Coordinates[] itemInfo) {

		this.playersinfo = robots;
		int currentKillerEnergy = this.getEnergy();
		int currentKillerSpeed = this.getRobotSpeed();

		initializeTrackingArrays();

		updatingSpeedData();

		GameRobot[] validTargets = getValidTargets();
		int validTargetCount = validTargets.length;

		if (currentKillerEnergy >= AGGRESSIVE_ETHRESHOLD && validTargetCount > 0) {
			aggresiveStrategy(validTargets);
		} else if (validTargetCount == 0 && currentKillerEnergy >= AGGRESSIVE_ETHRESHOLD) {
			this.move();
		} else {
			defensiveStrategy();
		}

		robots[i].setAvenue(this.getAvenue());
		robots[i].setStreet(this.getStreet());
		robots[i].setEnergy(this.getEnergy());
		return robots;
	}

	private void initializeTrackingArrays() {

		boolean isInitialized = false;

		if (!isInitialized) {
			lastPositions = new Coordinates[playersinfo.length];
			dodgeMemory = new int[playersinfo.length];
			robotSpeeds = new int[playersinfo.length];

			for (int j = 0; j < playersinfo.length; j++) {
				lastPositions[j] = new Coordinates(-1, -1);
				dodgeMemory[j] = 0;
				robotSpeeds[j] = 0;
			}

			isInitialized = true;
		}
	}

	private void updatingSpeedData() {
		for (int i = 0; i < playersinfo.length; i++) {
			GameRobot bot = playersinfo[i];
			if (!bot.getName().equalsIgnoreCase("killer")) {
				if (lastPositions[i].getAvenue() == -1 && lastPositions[i].getStreet() == -1) {
					lastPositions[i].setStreet(bot.getStreet());
					lastPositions[i].setAvenue(bot.getAvenue());
				} else {
					Coordinates lastPos = lastPositions[i];
					int currentX = bot.getAvenue();
					int currentY = bot.getStreet();
					int lastX = lastPos.getAvenue();
					int lastY = lastPos.getStreet();

					int diffX = currentX - lastX;
					if (diffX < 0) {
						diffX = diffX * -1;
					}

					int diffY = currentY - lastY;
					if (diffY < 0) {
						diffY = diffY * -1;
					}

					int distanceMoved = diffX + diffY;
					robotSpeeds[i] = distanceMoved;

					lastPositions[i].setStreet(currentY);
					lastPositions[i].setAvenue(currentX);
				}
			}
		}
	}

	private GameRobot[] getValidTargets() {
		GameRobot[] tempValidTargets = new GameRobot[playersinfo.length];
		int validTargetCount = 0;
		for (int i = 0; i < playersinfo.length; i++) {
			GameRobot bot = playersinfo[i];
			if (!bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) {
				tempValidTargets[validTargetCount] = bot;
				validTargetCount++;
			}
		}

		GameRobot[] validTargets = new GameRobot[validTargetCount];
		for (int i = 0; i < validTargetCount; i++) {
			validTargets[i] = tempValidTargets[i];
		}

		return validTargets;
	}

	private void aggresiveStrategy(GameRobot[] validTargets) {
		if (validTargets.length == 0) {
			this.move();
			return;
		}

		currentTargetRobot = sortedtarget(validTargets);
		if (currentTargetRobot == null) {
			this.move();
			return;
		}

		currentTargetCoordinates = new Coordinates(currentTargetRobot.getStreet(), currentTargetRobot.getAvenue());

		moveTo(currentTargetCoordinates, this.getRobotSpeed());

		if (this.getAvenue() == currentTargetRobot.getAvenue() && this.getStreet() == currentTargetRobot.getStreet()) {
			freezeTarget(currentTargetRobot);
		}
	}

	private void defensiveStrategy() {

		int currentKillerEnergy = this.getEnergy();
		currentKillerEnergy += RECHARGE_RATE;
		this.setEnergy(currentKillerEnergy);

		Coordinates[] corners = arena.getCorners();
		Coordinates nearestCorner = findClosetCorner(corners);

		if (this.getAvenue() != nearestCorner.getAvenue() || this.getStreet() != nearestCorner.getStreet()) {
			moveTo(nearestCorner, this.getRobotSpeed());
			currentTargetCoordinates = nearestCorner;
			currentTargetRobot = null;
		} else {
			GameRobot nearbyBot = findNearbyTarget();
			if (nearbyBot != null && !nearbyBot.isRobotFrozen() && !nearbyBot.getName().equalsIgnoreCase("killer")) {
				freezeTarget(nearbyBot);
			} else {
				this.turnRight();
			}
		}
	}

	private void freezeTarget(GameRobot target) {
		int targetIndex = -1;
		for (int i = 0; i < playersinfo.length; i++) {
			if (playersinfo[i] == target) {
				targetIndex = i;
				break;
			}
		}

		if (targetIndex == -1 || target.getName().equalsIgnoreCase("killer")) {
			return;
		}
		
		boolean dodged = false;
		
		dodged = target.tryToDodge();             
        target.recordGotFrozen(dodged);
        
        if(dodged == true) {
        	 System.out.println(target.getName() + " dodged the freeze!");
             return;
        }

		// Catch chance as integer percentage (e.g., 70 means 70%)
		int catchChancePercent = (int) (this.getCatchChance() * 100); // assuming getCatchChance() returns 0.0-1.0
		int roll = rand.nextInt(100); // generates 0-99

		if (roll < catchChancePercent) {
			target.setIsRobotFrozen(true);
			this.setEnergy(this.getEnergy() - FREEZE_ECOST);
			System.out.println("Freeze successful on " + target.getName() + "!");
		} else {
			System.out.println("Freeze attempt failed on " + target.getName() + "!");
		}
	}

	private void moveTo(Coordinates target, int killerSpeed) {
		int stepsTaken = 0;
		int targetAvenue = target.getAvenue();
		int targetStreet = target.getStreet();

		while (stepsTaken < killerSpeed && (this.getAvenue() != targetAvenue || this.getStreet() != targetStreet)) {

			int currentAvenue = this.getAvenue();
			int currentStreet = this.getStreet();
			boolean movedThisTurn = false;

			if (currentAvenue != targetAvenue) {
				if (currentAvenue < targetAvenue) {
					faceEast();
				} else {
					faceWest();
				}
				if (frontIsClear()) {
					move();
					movedThisTurn = true;
				}
			}

			if (!movedThisTurn && currentStreet != targetStreet) {
				if (currentStreet < targetStreet) {
					faceSouth();
				} else {
					faceNorth();
				}
				if (frontIsClear()) {
					move();
					movedThisTurn = true;
				}
			}

			if (!movedThisTurn) {
				turnRight();
				if (frontIsClear()) {
					move();
					movedThisTurn = true;
				} else {
					turnRight();
					if (frontIsClear()) {
						move();
						movedThisTurn = true;
					} else {
						turnLeft();
						if (frontIsClear()) {
							move();
							movedThisTurn = true;
						}
					}
				}
			}
			if (movedThisTurn) {
				stepsTaken++;
			} else {
				break;
			}
		}

	}

	private GameRobot sortedtarget(GameRobot[] validTargets) {
		if (validTargets.length == 0) {
			return null;
		}

		GameRobot bestTarget = null;
		double lowestScore = 999999.0;

		for (int i = 0; i < validTargets.length; i++) {
			GameRobot bot = validTargets[i];

			int botGlobalIndex = -1;
			for (int j = 0; j < playersinfo.length; j++) {
				if (playersinfo[j] == bot) {
					botGlobalIndex = j;
					break;
				}
			}

			if (botGlobalIndex == -1) {
				continue;
			}

			int distance = findDistance(bot.getAvenue(), bot.getStreet());

			int dodges = dodgeMemory[botGlobalIndex];
			int botSpeed = robotSpeeds[botGlobalIndex];

			double score = (double) dodges / 2.0 + (double) botSpeed / 2.0 + distance;

			if (score < lowestScore) {
				lowestScore = score;
				bestTarget = bot;
			}
		}
		return bestTarget;
	}

	private GameRobot findNearbyTarget() {

		int proximityRad = 3;
		for (int i = 0; i < playersinfo.length; i++) {
			GameRobot bot = playersinfo[i];
			if (!bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) {
				int distance = findDistance(bot.getAvenue(), bot.getStreet());
				if (distance <= proximityRad) {
					return bot;
				}
			}
		}
		return null;
	}

	private Coordinates findClosetCorner(Coordinates[] corners) {

		if (corners.length == 0) {
			return null;
		}

		Coordinates closest = corners[0];
		int minDistance = findDistance(closest.getAvenue(), closest.getStreet());

		for (int i = 1; i < corners.length; i++) {
			Coordinates corner = corners[i];
			int distance = findDistance(corner.getAvenue(), corner.getAvenue());
			if (distance < minDistance) {
				minDistance = distance;
				closest = corner;
			}
		}
		return closest;
	}

	private int findDistance(int avenue, int street) {

		int diffAvenue = this.getAvenue() - avenue;
		if (diffAvenue < 0) {
			diffAvenue = diffAvenue * -1;
		}

		int diffStreet = this.getStreet() - street;
		if (diffStreet < 0) {
			diffStreet = diffStreet * -1;
		}

		return diffAvenue + diffStreet;
	}

	/**
	 * helper methods to turn the robot in the specific direction
	 */
	private void faceNorth() {
		while (this.getDirection() != Direction.NORTH) {
			// if it is not north then turn right
			if (this.getDirection() == Direction.EAST) {
				// just turn left
				this.turnLeft();
			} else {
				this.turnRight();
			}
		}
	}

	/**
	 * helper methods to turn the robot in the specific direction
	 */
	private void faceSouth() {
		while (this.getDirection() != Direction.SOUTH) {
			// if it is not south then turn left
			if (this.getDirection() == Direction.EAST) {
				// just turn right
				this.turnRight();
			} else {
				this.turnLeft();
			}
		}
	}

	/**
	 * helper methods to turn the robot in the specific direction
	 */
	private void faceEast() {
		while (this.getDirection() != Direction.EAST) {
			// if it is not east then turn left
			if (this.getDirection() == Direction.NORTH) {
				// just turn right
				this.turnRight();
			} else {
				this.turnLeft();
			}
		}
	}

	/**
	 * helper methods to turn the robot in the specific direction
	 */
	private void faceWest() {
		while (this.getDirection() != Direction.WEST) {
			// if it is not west then turn left
			if (this.getDirection() == Direction.SOUTH) {
				// just turn right
				this.turnRight();
			} else {
				this.turnLeft();
			}
		}
	}
}