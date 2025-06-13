package summativeGame;

import becker.robots.*;
import java.util.Random;

public class KillerBot extends GameRobot {

	private Arena arena;
	private String type;
	private int catchChance;
	private boolean isInitialized = false;

	private Coordinates[] lastPositions;
	private int[] totalFreezeAttempts;
	private int[] successfulDodges;
	private int[] robotSpeeds;

	private GameRecord[] playersinfo;

	private Coordinates currentTargetCoordinates;
	private GameRecord currentTargetRobot;

	private static final int AGGRESSIVE_ETHRESHOLD = 30;
	private static final int FREEZE_ECOST = 10;
	private static final int RECHARGE_RATE = 5;
	private static final int DODGE_ABANDON_THRESHOLD = 3;

	private Random rand;

	public KillerBot(int id, City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, int dodgeChance, int catchChance){
		super(id, c, street, avenue, d, arena, energy, speed, type, dodgeChance, catchChance);

		this.arena = arena;
		this.type = "killer";
		this.catchChance = catchChance;

		this.lastPositions = null;
		this.totalFreezeAttempts = null;
		this.successfulDodges = null;
		this.robotSpeeds = null;

		this.rand = new Random();
	}

	public MoveStatus moveTurn(GameRecord[] robots, int i, Coordinates[] itemInfo) {

		this.playersinfo = robots;
		int currentKillerEnergy = this.getEnergy();

		initializeTrackingArrays();
		updatingSpeedData();

		GameRecord[] validTargets = getValidTargets();
		int validTargetCount = validTargets.length;

		MoveStatus status = null;

		if(currentKillerEnergy >= AGGRESSIVE_ETHRESHOLD && validTargetCount > 0) {
			status = aggresiveStrategy(validTargets);
		}
		else if(validTargetCount == 0 && currentKillerEnergy >= AGGRESSIVE_ETHRESHOLD) {
			this.move();
			status = new MoveStatus(this.getID(), -1, "move");
		}
		else {
			status = defensiveStrategy();
		}

		return status;
	}

	private void initializeTrackingArrays() {

		if(!isInitialized) {
			lastPositions = new Coordinates[playersinfo.length];
			totalFreezeAttempts = new int[playersinfo.length];
			successfulDodges = new int[playersinfo.length];
			robotSpeeds = new int[playersinfo.length];

			for(int j = 0; j <playersinfo.length; j++) {
				lastPositions[j] = new Coordinates(playersinfo[j].getStreet(), playersinfo[j].getAvenue());
				totalFreezeAttempts[j] = 0;
				successfulDodges[j] = 0;
				robotSpeeds[j] = 0;
			}

			isInitialized = true;
		}
	}

	private void updatingSpeedData() {
		for(int i = 0; i < playersinfo.length; i++) {
			GameRecord bot = playersinfo[i];
			if(!bot.getName().equalsIgnoreCase("killer")) {
				if(lastPositions[i].getAvenue() == -1 && lastPositions[i].getStreet() == -1) {
					lastPositions[i].setStreet(bot.getStreet());
					lastPositions[i].setAvenue(bot.getAvenue());
				}
				else {
					Coordinates lastPos = lastPositions[i];
					int currentX = bot.getAvenue();
					int currentY = bot.getStreet();
					int lastX = lastPos.getAvenue();
					int lastY = lastPos.getStreet();

					int diffX = Math.abs(currentX - lastX);
					int diffY = Math.abs(currentY - lastY);

					int distanceMoved = diffX + diffY;
					robotSpeeds[i] = distanceMoved;

					lastPositions[i].setStreet(currentY);
					lastPositions[i].setAvenue(currentX);
				}
			}
		}
	}

	private GameRecord[] getValidTargets() {
		GameRecord[] tempValidTargets = new GameRecord[playersinfo.length];
		int validTargetCount = 0;
		for(int i = 0; i < playersinfo.length; i++) {
			GameRecord bot = playersinfo[i];
			if(!bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) {
				tempValidTargets[validTargetCount] = bot;
				validTargetCount++;
			}
		}

		GameRecord[] validTargets = new GameRecord[validTargetCount];
		for(int i = 0; i < validTargetCount; i++) {
			validTargets[i] = tempValidTargets[i];
		}
		return validTargets;
	}

	private MoveStatus aggresiveStrategy(GameRecord[] validTargets) {
		MoveStatus status = null;

		if(validTargets.length == 0) {
			this.move();
			return new MoveStatus(this.getID(), -1, "move");
		}

		currentTargetRobot = sortedtarget(validTargets);
		if(currentTargetRobot == null) {
			this.move();
			return new MoveStatus(this.getID(), -1, "move");
		}

		int targetIndex = -1;
		for (int j = 0; j < playersinfo.length; j++) {
			if (playersinfo[j] == currentTargetRobot) {
				targetIndex = j;
				break;
			}
		}

		if (targetIndex != -1 && successfulDodges[targetIndex] >= DODGE_ABANDON_THRESHOLD && totalFreezeAttempts[targetIndex] > 0) {
			GameRecord[] simplerTargets = getSimplerTargets(validTargets, targetIndex);
			if (simplerTargets.length > 0) {
				status = aggresiveStrategy(simplerTargets);
				return status;
			}
		}

		currentTargetCoordinates = new Coordinates(currentTargetRobot.getStreet(),currentTargetRobot.getAvenue());

		moveTo(currentTargetCoordinates, this.getRobotSpeed());

		if(this.getAvenue() == currentTargetRobot.getAvenue() && this.getStreet() == currentTargetRobot.getStreet()) {
			status = freezeTarget(currentTargetRobot);
		} else {
			status = new MoveStatus(this.getID(), -1, "move");
		}
		return status;
	}

	private GameRecord[] getSimplerTargets(GameRecord[] currentValidTargets, int index) {
		GameRecord[] tempSimplerTargets = new GameRecord[currentValidTargets.length];
		int simplerTargetCount = 0;

		for (int i = 0; i < currentValidTargets.length; i++) {
			GameRecord bot = currentValidTargets[i];
			int botIndex = -1;

			for (int j = 0; j < playersinfo.length; j++) {
				if (playersinfo[j] == bot) {
					botIndex = j;
					break;
				}
			}

			if (botIndex != -1 && botIndex != index && successfulDodges[botIndex] < DODGE_ABANDON_THRESHOLD) {
				tempSimplerTargets[simplerTargetCount++] = bot;
			}
		}

		GameRecord[] simplerTargets = new GameRecord[simplerTargetCount];
	    for (int i = 0; i < simplerTargetCount; i++) {
	        simplerTargets[i] = tempSimplerTargets[i];
	    }
	    return simplerTargets;
	}

	private MoveStatus defensiveStrategy() {
		MoveStatus status = null;

		int currentKillerEnergy = this.getEnergy();
		currentKillerEnergy += RECHARGE_RATE;
		this.setEnergy(currentKillerEnergy);

		Coordinates[] corners = arena.getCorners();

		if(corners == null || corners.length == 0) {
			this.turnRight();
			return new MoveStatus(this.getID(), -1, "cornerNotFound");
		}

		Coordinates nearestCorner = findClosetCorner(corners);

		if(this.getAvenue() != nearestCorner.getAvenue() || this.getStreet() != nearestCorner.getStreet()) {
			moveTo(nearestCorner, this.getRobotSpeed());
			currentTargetCoordinates = nearestCorner;
			currentTargetRobot = null;
			status = new MoveStatus(this.getID(), -1, "moveToCorner");
		}
		else {
			GameRecord nearbyBot = findNearbyTarget();
			if(nearbyBot != null && !nearbyBot.isRobotFrozen() && !nearbyBot.getName().equalsIgnoreCase("killer")) {
				status = freezeTarget(nearbyBot);
			}
			else {
				this.turnRight();
				status = new MoveStatus(this.getID(), -1, "recharge&Turn");
			}
		}
		return status;
	}

	private MoveStatus freezeTarget(GameRecord target) {
		int targetId = -1;
		String actionType = "miss";

		int targetIndex = -1;
		for (int i = 0; i < playersinfo.length; i++) {
			if (playersinfo[i] == target) {
				targetIndex = i;
				break;
			}
		}
		if (targetIndex == -1 || target.getName().equalsIgnoreCase("killer")) {
			return new MoveStatus(this.getID(), -1, "invalidTarget");
		}

		totalFreezeAttempts[targetIndex]++;

		if (rand.nextInt(100) < this.catchChance) {
			System.out.println("KillerBot froze " + target.getName() + "!");
			targetId = target.getID();
			target.SetisRobotFrozen(true);

			int currentKillerEnergy = this.getEnergy();
			currentKillerEnergy -= FREEZE_ECOST;
			this.setEnergy(currentKillerEnergy);
			successfulDodges[targetIndex] = 0;
			actionType = "freeze";
		} else {
			System.out.println("KillerBot attempted to freeze " + target.getName() + " but it avoided!");
			successfulDodges[targetIndex]++;
		}

		return new MoveStatus(this.getID(), targetId, actionType);
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
						if(frontIsClear()){
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

	private GameRecord sortedtarget(GameRecord[] validTargets) {

		if (validTargets.length == 0) {
			return null;
		}
		GameRecord bestTarget = null;
		double lowestScore = 99999.0;

		for (int i = 0; i < validTargets.length; i++) {
			GameRecord bot = validTargets[i];

			int botGlobalIndex = -1;
			for (int j = 0; j < playersinfo.length; j++) {
				if (playersinfo[j] == bot) {
					botGlobalIndex = j;
					break;
				}
			}

			if (botGlobalIndex != -1) {
				int distance = findDistance(bot.getAvenue(), bot.getStreet());
				int botSpeed = robotSpeeds[botGlobalIndex];

				double observedDodgeProb = 0.0;
				if (totalFreezeAttempts[botGlobalIndex] > 0) {
					observedDodgeProb = (double)successfulDodges[botGlobalIndex] / totalFreezeAttempts[botGlobalIndex];
				}

				double score = (observedDodgeProb * 10) + (double)botSpeed / 2.0 + distance;

				if (score < lowestScore) {
					lowestScore = score;
					bestTarget = bot;
				}
			}
		}
		return bestTarget;
	}

	private GameRecord findNearbyTarget() {

		int proximityRad = 3;
		for(int i = 0; i <playersinfo.length; i++) {
			GameRecord bot = playersinfo[i];
			if(!bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) {
				int distance = findDistance(bot.getAvenue(),bot.getStreet());
				if(distance <= proximityRad) {
					return bot;
				}
			}
		}
		return null;
	}

	private Coordinates findClosetCorner(Coordinates[] corners) {

		if(corners == null || corners.length == 0) {
			return null;
		}

		Coordinates closest = corners[0];
		int minDistance = findDistance(closest.getAvenue(),closest.getStreet());

		for(int i = 1; i <corners.length; i++) {
			Coordinates corner = corners[i];
			int distance = findDistance(corner.getAvenue(), corner.getStreet());
			if(distance < minDistance) {
				minDistance = distance;
				closest = corner;
			}
		}
		return closest;
	}

	private int findDistance(int avenue, int street) {
		int diffAvenue = Math.abs(this.getAvenue() - avenue);
		int diffStreet = Math.abs(this.getStreet() - street);
		return diffAvenue + diffStreet;
	}

	private void faceNorth() {
		while (this.getDirection() != Direction.NORTH) {
			if (this.getDirection() == Direction.EAST) {
				this.turnLeft();
			} else {
				this.turnRight();
			}
		}
	}

	private void faceSouth() {
		while (this.getDirection() != Direction.SOUTH) {
			if (this.getDirection() == Direction.EAST) {
				this.turnRight();
			} else {
				this.turnLeft();
			}
			}
	}

	private void faceEast() {
		while (this.getDirection() != Direction.EAST) {
			if (this.getDirection() == Direction.NORTH) {
				this.turnRight();
			} else {
				this.turnLeft();
			}
		}
	}

	private void faceWest() {
		while (this.getDirection() != Direction.WEST) {
			if (this.getDirection() == Direction.SOUTH) {
				this.turnRight();
			} else {
				this.turnLeft();
			}
		}
	}
}