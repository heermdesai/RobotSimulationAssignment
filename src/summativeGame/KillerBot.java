package summativeGame;

import becker.robots.*;
import java.util.Random;

public class KillerBot extends GameRobot {

	private Arena arena; // The arena where the robot operates
	private String type; // Type of the robot
	private int catchChance; // Chance to successfully freeze a target
	private boolean isInitialized = false; // Flag to check if tracking arrays are initialized

	private Coordinates[] lastPositions; // Last known positions of other robots
	private int[] totalFreezeAttempts; // Total freeze attempts for each robot
	private int[] successfulDodges; // Successful dodges for each robot
	private int[] robotSpeedsTracked; // Speeds of other robots

	private GameRecord[] playersInfo; // Information about all players

	private Coordinates currentTargetCoordinates; // Coordinates of the current target
	private GameRecord currentTargetRobot; // Current target robot

	private static final int AGGRESSIVE_ENERGY_THRESHOLD = 30; // Energy threshold for aggressive strategy
	private static final int FREEZE_ENERGY_COST = 5; // Energy cost to freeze a target
	private static final int RECHARGE_RATE = 15; // Energy recharge rate
	private static final int DODGE_ABANDON_THRESHOLD = 3; // Threshold for abandoning a target
	private static final int PROXIMITY_RADIUS = 3; // Radius to find nearby targets

	private Random rand; // Random

	/**
	 * Constructor for KillerBot
	 * @param id Robot ID
	 * @param c City 
	 * @param street Initial street position
	 * @param avenue Initial avenue position
	 * @param d Initial direction
	 * @param arena Game arena reference
	 * @param energy Initial energy level
	 * @param speed Movement speed
	 * @param type Robot type 
	 * @param dodgeChance Dodge chance 
	 * @param catchChance Chance to catch/freeze a bot
	 */
	public KillerBot(int id, City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type,
			int dodgeChance, int catchChance) {
		super(id, c, street, avenue, d, arena, energy, speed, type, dodgeChance, catchChance);
		this.arena = arena; 
		this.type = "killer"; 
		this.catchChance = catchChance;

		// Initialize arrays to null
		this.lastPositions = null;
		this.totalFreezeAttempts = null;
		this.successfulDodges = null;
		this.robotSpeedsTracked = null;

		this.rand = new Random(); // Initialize random generator
	}

	/**
	 * Determines and executes KillerBot's action on its turn.
	 *
	 * @param robots Array of all robots in the game
	 * @param i Index of this KillerBot in the array
	 * @param itemInfo Array of item locations
	 * @return MoveStatus indicating the bot's action
	 */
	@Override
	public MoveStatus moveTurn(GameRecord[] robots, int i, Coordinates[] itemInfo) {
		this.playersInfo = robots; // Store player info
		int currentKillerEnergy = this.getEnergy(); // Get current energy
		System.out.println("KillerBot (" + this.getID() + ") current energy: " + currentKillerEnergy);

		// Safely initialize arrays only if playersInfo is valid and non-empty
		if (playersInfo != null && playersInfo.length > 0) {
			initializeTrackingArrays(); // Initialize tracking arrays
			updateSpeedData(); // Update speed data
		}

		GameRecord[] validTargets = getValidTargets(); // Get valid targets
		int validTargetCount = validTargets.length; // Count valid targets

		MoveStatus status = null; // Initialize move status

		// Choose strategy based on energy and valid targets
		if (currentKillerEnergy >= AGGRESSIVE_ENERGY_THRESHOLD && validTargetCount > 0) {
			System.out.println("KillerBot (" + this.getID() + ") using aggressive strategy.");
			status = aggressiveStrategy(robots); // Use aggressive strategy
		} else if (validTargetCount == 0 && currentKillerEnergy >= AGGRESSIVE_ENERGY_THRESHOLD) {
			this.move(); // Move if no valid targets
			status = new MoveStatus(this.getID(), -1, "move");
			System.out.println("KillerBot (" + this.getID() + ") moved.");
		} else {
			System.out.println("KillerBot (" + this.getID() + ") using defensive strategy.");
			status = defensiveStrategy(); // Use defensive strategy
		}

		return status; // Return move status
	}

	/**
	 * Initializes tracking arrays for all robots in the arena.
	 * Tracks last positions, freeze attempts, dodges, and speed data.
	 */
	private void initializeTrackingArrays() {
		if (!isInitialized && playersInfo != null && playersInfo.length > 0) {
			lastPositions = new Coordinates[playersInfo.length]; // Initialize last positions
			totalFreezeAttempts = new int[playersInfo.length]; // Initialize freeze attempts
			successfulDodges = new int[playersInfo.length]; // Initialize successful dodges
			robotSpeedsTracked = new int[playersInfo.length]; // Initialize robot speeds

			for (int j = 0; j < playersInfo.length; j++) {
				if (playersInfo[j] != null) {
					lastPositions[j] = new Coordinates(playersInfo[j].getStreet(), playersInfo[j].getAvenue()); // Set last position
				} else {
					lastPositions[j] = new Coordinates(0, 0); // Default position
				}
				totalFreezeAttempts[j] = 0; // Initialize attempts
				successfulDodges[j] = 0; // Initialize dodges
				robotSpeedsTracked[j] = 0; // Initialize speeds
			}
			isInitialized = true; // Mark as initialized
			System.out.println("KillerBot (" + this.getID() + ") tracking arrays initialized.");
		}
	}

	/**
	 * Updates the estimated speed of each non-killer robot by comparing current and previous positions.
	 */
	private void updateSpeedData() {
		if (playersInfo == null) {
			return; // Exit if no players
		}

		for (int i = 0; i < playersInfo.length; i++) {
			GameRecord bot = playersInfo[i]; // Get bot info
			if (bot != null && !bot.getName().equalsIgnoreCase("killer")) { // Check if not killer
				Coordinates lastPos = lastPositions[i]; // Get last position
				int currentX = bot.getAvenue(); // Current avenue
				int currentY = bot.getStreet(); // Current street
				int lastX = lastPos.getAvenue(); // Last avenue
				int lastY = lastPos.getStreet(); // Last street

				int diffX = Math.abs(currentX - lastX); // Difference in avenue
				int diffY = Math.abs(currentY - lastY); // Difference in street
				int distanceMoved = diffX + diffY; // Total distance moved

				robotSpeedsTracked[i] = distanceMoved; // Track speed

				// Update last position safely
				lastPositions[i].setStreet(currentY); // Update street
				lastPositions[i].setAvenue(currentX); // Update avenue
				System.out.println("KillerBot (" + this.getID() + ") updated speed data for bot ID: " + bot.getID());
			}
		}
	}

	/**
	 * Returns an array of all non-killer, unfrozen robots that can be targeted.
	 *
	 * @return GameRecord[] of valid enemy targets
	 */
	private GameRecord[] getValidTargets() {
		if (playersInfo == null) {
			return new GameRecord[0]; // Return empty if no players
		}

		GameRecord[] tempValidTargets = new GameRecord[playersInfo.length]; // Temp array for valid targets
		int validTargetCount = 0; // Count valid targets
		for (int i = 0; i < playersInfo.length; i++) {
			GameRecord bot = playersInfo[i]; // Get bot info
			if (bot != null && !bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) { // Check if valid
				tempValidTargets[validTargetCount] = bot; // Add to valid targets
				validTargetCount++; // Increment count
			}
		}
		GameRecord[] validTargets = new GameRecord[validTargetCount]; // Final valid targets array
		for (int i = 0; i < validTargetCount; i++) {
			validTargets[i] = tempValidTargets[i]; // Populate final array
		}
		System.out.println("KillerBot (" + this.getID() + ") found " + validTargetCount + " valid targets.");
		return validTargets; // Return valid targets
	}

	/**
	 * Aggressive strategy to pursue and attempt to freeze a target robot.
	 * Selects the best target among all non-killer, unfrozen robots.
	 * If the current target dodges too often, switches to simpler targets.
	 * Moves towards the target and tries to freeze if in the same position.
	 * If no valid targets exist, the bot simply moves forward.
	 * 
	 * @param allRobots array of all robots currently in the game
	 * @return MoveStatus representing the action taken (move, freeze, etc.)
	 */
	private MoveStatus aggressiveStrategy(GameRecord[] allRobots) {
		if (allRobots == null) {
			return new MoveStatus(this.getID(), -1, "move"); // Return move status if null
		}

		GameRecord[] validTargets = new GameRecord[allRobots.length]; // Array for valid targets
		int validTargetCount = 0; // Count valid targets

		// Filter valid targets
		for (int i = 0; i < allRobots.length; i++) {
			GameRecord bot = allRobots[i]; // Get bot info
			if (bot != null && !bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) { // Check if valid
				validTargets[validTargetCount] = bot; // Add to valid targets
				validTargetCount++; // Increment count
			}
		}

		if (validTargetCount == 0) { // No valid targets
			this.move();
			System.out.println("KillerBot (" + this.getID() + ") moved due to no valid targets.");
			return new MoveStatus(this.getID(), -1, "move"); // Return move status
		}

		GameRecord[] actualValidTargets = new GameRecord[validTargetCount]; // Final valid targets array
		for (int i = 0; i < validTargetCount; i++) {
			actualValidTargets[i] = validTargets[i]; // Populate final array
		}

		currentTargetRobot = selectTarget(actualValidTargets); // Select target
		if (currentTargetRobot == null) { // No target selected
			this.move(); 
			System.out.println("KillerBot (" + this.getID() + ") moved due to no target selected.");
			return new MoveStatus(this.getID(), -1, "move"); // Return move status
		}

		int targetIndex = -1; // Initialize target index
		for (int j = 0; j < playersInfo.length; j++) {
			if (playersInfo[j] != null && playersInfo[j].getID() == currentTargetRobot.getID()) { // Find target index
				targetIndex = j; // Set index
				break; 
			}
		}

		// Check if target is too dodgy
		if (targetIndex != -1 && successfulDodges[targetIndex] >= DODGE_ABANDON_THRESHOLD && totalFreezeAttempts[targetIndex] > 0) {
			GameRecord[] simplerTargets = getSimplerTargets(actualValidTargets, currentTargetRobot.getID()); // Get simpler targets
			if (simplerTargets.length > 0) { // If simpler targets exist
				System.out.println("KillerBot (" + this.getID() + ") switching to simpler targets.");
				return aggressiveStrategy(simplerTargets); // Recur with simpler targets
			}
		}

		currentTargetCoordinates = new Coordinates(currentTargetRobot.getStreet(), currentTargetRobot.getAvenue()); // Set target coordinates

		moveTo(currentTargetCoordinates, this.getRobotSpeed()); // Move to target

		if (this.getAvenue() == currentTargetRobot.getAvenue() && this.getStreet() == currentTargetRobot.getStreet()) { // Check if at target
			System.out.println("KillerBot (" + this.getID() + ") reached target and attempting to freeze.");
			return freezeTarget(currentTargetRobot); // Freeze target
		} else {
			System.out.println("KillerBot (" + this.getID() + ") moved towards target.");
			return new MoveStatus(this.getID(), -1, "move"); // Return move status
		}
	}

	/**
	 * Filters and returns a simplified list of targets excluding the specified one.
	 * Excludes targets that have dodged too many times to focus on easier targets.
	 * 
	 * @param currentValidTargets current array of valid target robots
	 * @param excludedTargetId the ID of the target to exclude from the list
	 * @return an array of simpler valid targets to pursue
	 */
	private GameRecord[] getSimplerTargets(GameRecord[] currentValidTargets, int excludedTargetId) {
		GameRecord[] tempSimplerTargets = new GameRecord[currentValidTargets.length]; // Temp array for simpler targets
		int simplerTargetCount = 0; // Count simpler targets

		// Filter simpler targets
		for (int i = 0; i < currentValidTargets.length; i++) {
			GameRecord bot = currentValidTargets[i]; // Get bot info
			if (bot != null && bot.getID() != excludedTargetId) { // Check if not excluded
				int botGlobalIndex = -1; // Initialize index
				for (int j = 0; j < playersInfo.length; j++) {
					if (playersInfo[j] != null && playersInfo[j].getID() == bot.getID()) { // Find global index
						botGlobalIndex = j; // Set index
						break; // Exit loop
					}
				}
				if (botGlobalIndex != -1 && successfulDodges[botGlobalIndex] < DODGE_ABANDON_THRESHOLD) { // Check dodges
					tempSimplerTargets[simplerTargetCount] = bot; // Add to simpler targets
					simplerTargetCount++; // Increment count
				}
			}
		}

		GameRecord[] actualSimplerTargets = new GameRecord[simplerTargetCount]; // Final simpler targets array
		for (int i = 0; i < simplerTargetCount; i++) {
			actualSimplerTargets[i] = tempSimplerTargets[i]; // Populate final array
		}
		System.out.println("KillerBot (" + this.getID() + ") found " + simplerTargetCount + " simpler targets.");
		return actualSimplerTargets; // Return simpler targets
	}

	/**
	 * Defensive strategy where the bot moves to a corner to recharge energy.
	 * Attempts to freeze nearby targets within a radius.
	 * If no nearby targets, the bot turns right and waits.
	 * 
	 * @return MoveStatus representing the defensive action taken (move, freeze, turn)
	 */
	private MoveStatus defensiveStrategy() {
		Coordinates[] corners = arena.getCorners(); // Get corners

		if (corners == null || corners.length == 0) { // No corners found
			this.turnRight(); 
			System.out.println("KillerBot (" + this.getID() + ") could not find corners. Turning right.");
			return new MoveStatus(this.getID(), -1, "cornerNotFound"); // Return status
		}

		Coordinates nearestCorner = findClosestCorner(corners); // Find nearest corner

		if (this.getAvenue() != nearestCorner.getAvenue() || this.getStreet() != nearestCorner.getStreet()) { // Not at corner
			moveTo(nearestCorner, this.getRobotSpeed()); // Move to corner
			currentTargetCoordinates = nearestCorner; // Set target coordinates
			currentTargetRobot = null; // Clear target robot
			System.out.println("KillerBot (" + this.getID() + ") moving to nearest corner at (" + nearestCorner.getAvenue() + ", "
					+ nearestCorner.getStreet() + ").");
			return new MoveStatus(this.getID(), -1, "moveToCorner"); // Return status
		} else {
			// Recharge only when on corner
			int currentKillerEnergy = this.getEnergy(); // Get current energy
			System.out.println("KillerBot (" + this.getID() + ") current energy before recharge: " + currentKillerEnergy);

			currentKillerEnergy += RECHARGE_RATE; // Recharge energy
			if (currentKillerEnergy > 100) // Cap energy
				currentKillerEnergy = 100;
			this.setEnergy(currentKillerEnergy); // Set new energy

			System.out.println("KillerBot (" + this.getID() + ") energy after recharge: " + currentKillerEnergy);

			// Try freeze nearby bots in proximity
			GameRecord nearbyBot = findNearbyTarget(PROXIMITY_RADIUS); // Find nearby bot

			if (nearbyBot != null && !nearbyBot.isRobotFrozen() && !nearbyBot.getName().equalsIgnoreCase("killer")) { // Valid nearby bot
				System.out.println("KillerBot (" + this.getID() + ") found nearby bot " + nearbyBot.getName() + " (ID: "
						+ nearbyBot.getID() + "). Attempting to freeze.");
				return freezeTarget(nearbyBot); // Freeze nearby bot
			} else {
				this.turnRight();
				System.out.println("KillerBot (" + this.getID() + ") no nearby targets. Skipping turn and turning right.");
				return new MoveStatus(this.getID(), -1, "recharge&Turn"); // Return status
			}
		}
	}

	/**
	 * Attempts to freeze the specified target robot.
	 * Checks validity of the target, performs a catch vs dodge roll,
	 * updates freeze attempts and dodges counters, adjusts energy on success,
	 * and returns the outcome as a MoveStatus.
	 * 
	 * @param target the robot to attempt freezing
	 * @return MoveStatus representing the result of the freeze attempt
	 */
	private MoveStatus freezeTarget(GameRecord target) {
		int targetId = target.getID(); // Get target ID
		String actionType = "miss"; // Default action type

		int targetIndex = -1; // Initialize target index
		for (int i = 0; i < playersInfo.length; i++) {
			if (playersInfo[i] != null && playersInfo[i].getID() == target.getID()) { // Find target index
				targetIndex = i; // Set index
				break; // Exit loop
			}
		}
		if (targetIndex == -1 || target.getName().equalsIgnoreCase("killer")) { // Invalid target
			System.out.println("KillerBot (" + this.getID() + ") invalid target for freezing.");
			return new MoveStatus(this.getID(), -1, "invalidTarget"); // Return status
		}

		totalFreezeAttempts[targetIndex]++; // Increment freeze attempts

		int catchRoll = rand.nextInt(100); // Random catch roll
		int dodgeRoll = rand.nextInt(100); // Random dodge roll

		boolean killerSucceeded = (catchRoll < this.catchChance); // Check if freeze succeeded
		boolean targetDodged = (dodgeRoll < target.getDodgeChance()); // Check if target dodged

		System.out.println("KillerBot (" + this.getID() + ") trying to freeze " + target.getName() + " (ID: " + target.getID()
		+ "). Catch roll: " + catchRoll + ", Dodge roll: " + dodgeRoll + ".");

		if (killerSucceeded && !targetDodged) { // Freeze successful
			System.out.println("KillerBot (" + this.getID() + ") successfully froze " + target.getName() + " (ID: "
					+ target.getID() + ")!");
			actionType = "freeze"; // Set action type to freeze
			int currentKillerEnergy = this.getEnergy(); // Get current energy
			currentKillerEnergy -= FREEZE_ENERGY_COST; // Deduct freeze cost
			if (currentKillerEnergy < 0) { // Prevent negative energy
				currentKillerEnergy = 0;
			}
			this.setEnergy(currentKillerEnergy); // Set new energy
			successfulDodges[targetIndex] = 0; // Reset dodges
		} else { // Freeze failed
			System.out.println("KillerBot (" + this.getID() + ") attempted to freeze " + target.getName() + " (ID: "
					+ target.getID() + ") but it avoided!");
			successfulDodges[targetIndex]++; // Increment dodges
			actionType = "miss"; // Set action type to miss
		}

		return new MoveStatus(this.getID(),		targetId, actionType); // Return the result of the freeze attempt
	}

	/**
	 * Moves the KillerBot towards the specified target coordinates,
	 * up to the maximum steps allowed by the killer's speed.
	 * Attempts horizontal moves first, then vertical, and finally fallback moves
	 * if blocked, adjusting direction accordingly.
	 * 
	 * @param target the coordinates to move towards
	 * @param killerSpeed maximum number of steps to move this turn
	 */
	private void moveTo(Coordinates target, int killerSpeed) {
		int stepsTaken = 0; // Initialize steps taken
		int targetAvenue = target.getAvenue(); // Target avenue
		int targetStreet = target.getStreet(); // Target street

		// Move towards the target until speed limit is reached or target is reached
		while (stepsTaken < killerSpeed
				&& (this.getAvenue() != targetAvenue || this.getStreet() != targetStreet)) {

			int currentAvenue = this.getAvenue(); // Current avenue
			int currentStreet = this.getStreet(); // Current street
			boolean movedThisStep = false; // Track if moved

			int deltaAvenue = targetAvenue - currentAvenue; // Difference in avenue
			int deltaStreet = targetStreet - currentStreet; // Difference in street

			// Try horizontal move first if needed
			if (Math.abs(deltaAvenue) >= Math.abs(deltaStreet)) {
				if (deltaAvenue > 0) {
					faceEast(); 
				} else if (deltaAvenue < 0) {
					faceWest(); 
				}
				if (frontIsClear()) {
					move(); 
					movedThisStep = true; 
					System.out.println("KillerBot (" + this.getID() + ") moved horizontally.");
				}
			}

			// If not moved yet, try vertical move
			if (!movedThisStep && deltaStreet != 0) {
				if (deltaStreet > 0) {
					faceSouth(); 
				} else {
					faceNorth(); 
				}
				if (frontIsClear()) {
					move(); 
					movedThisStep = true;
					System.out.println("KillerBot (" + this.getID() + ") moved vertically.");
				}
			}

			// try alternate directions
			if (!movedThisStep) {
				this.turnRight(); 
				if (frontIsClear()) {
					move(); 
					movedThisStep = true; // Mark as moved
					System.out.println("KillerBot (" + this.getID() + ") moved after turning right.");
				} else {
					this.turnRight(); 
					if (frontIsClear()) {
						move(); 
						movedThisStep = true; // Mark as moved
						System.out.println("KillerBot (" + this.getID() + ") moved after turning 180°.");
					} else {
						this.turnLeft(); 
						if (frontIsClear()) {
							move(); 
							movedThisStep = true; // Mark as moved
							System.out.println("KillerBot (" + this.getID() + ") moved after turning left.");
						}
					}
				}
			}

			if (movedThisStep) {
				stepsTaken++; // Increment steps taken
			} else {
				System.out.println("KillerBot (" + this.getID() + ") is stuck.");
				break; // Robot is stuck
			}
		}
	}

	/**
	 * Selects the best target from an array of valid targets,
	 * using a score based on distance, number of dodges, and speed.
	 * Lower scores indicate higher priority.
	 * 
	 * @param validTargets array of valid target robots to evaluate
	 * @return the best GameRecord target based on priority score, or null if none
	 */
	private GameRecord selectTarget(GameRecord[] validTargets) {
		if (validTargets.length == 0) {
			System.out.println("KillerBot (" + this.getID() + ") no valid targets to select.");
			return null; // No valid targets
		}
		GameRecord bestTarget = null; // Best target
		double lowestScore = 99999.0; // Initialize lowest score

		// Evaluate each valid target
		for (int i = 0; i < validTargets.length; i++) {
			GameRecord bot = validTargets[i]; // Get bot info
			int botGlobalIndex = -1; // Initialize index
			for (int j = 0; j < playersInfo.length; j++) {
				if (playersInfo[j] != null && playersInfo[j].getID() == bot.getID()) { // Find global index
					botGlobalIndex = j; // Set index
					break; // Exit loop
				}
			}

			if (botGlobalIndex != -1) {
				int distance = findDistance(bot.getAvenue(), bot.getStreet()); // Calculate distance
				int dodges = successfulDodges[botGlobalIndex]; // Get dodges
				int botSpeed = robotSpeedsTracked[botGlobalIndex]; // Get speed

				// Calculate a priority score
				double score = (double) dodges / 2.0 + (double) botSpeed / 2.0 + distance;

				if (score < lowestScore) { // Check for best target
					lowestScore = score; // Update lowest score
					bestTarget = bot; // Set best target
				}
			}
		}
		return bestTarget; // Return best target
	}

	/**
	 * Finds a nearby target within a given radius who is not frozen and not a KillerBot.
	 * Returns the first such target found.
	 * 
	 * @param radius the maximum distance to consider
	 * @return a nearby target robot within radius, or null if none found
	 */
	private GameRecord findNearbyTarget(int radius) {
		for (int i = 0; i < playersInfo.length; i++) {
			GameRecord bot = playersInfo[i]; // Get bot info
			if (bot != null && !bot.getName().equalsIgnoreCase("killer") && !bot.isRobotFrozen()) { // Check if valid
				int distance = findDistance(bot.getAvenue(), bot.getStreet()); // Calculate distance
				if (distance <= radius) {
					return bot; // Return nearby bot
				}
			}
		}
		return null; // No nearby target found
	}

	/**
	 * Finds the closest corner from an array of corner coordinates,
	 * based on distance from the KillerBot's current position.
	 * 
	 * @param corners array of corner Coordinates to evaluate
	 * @return the Coordinates of the closest corner, or null if input is empty or null
	 */
	private Coordinates findClosestCorner(Coordinates[] corners) {
		if (corners == null || corners.length == 0) {
			return null; // No corners found
		}

		Coordinates closest = corners[0]; // Start with the first corner
		int minDistance = findDistance(closest.getAvenue(), closest.getStreet()); // Calculate distance

		// Evaluate each corner
		for (int i = 1; i < corners.length; i++) {
			Coordinates corner = corners[i]; // Get corner
			int distance = findDistance(corner.getAvenue(), corner.getStreet()); // Calculate distance
			if (distance < minDistance) { // Check for closest corner
				minDistance = distance; // Update minimum distance
				closest = corner; // Set closest corner
			}
		}
		return closest; // Return closest corner
	}

	/**
	 * Calculates the distance between the KillerBot's current position
	 * and the specified avenue and street.
	 * 
	 * @param avenue the avenue coordinate to measure to
	 * @param street the street coordinate to measure to
	 * @return the sum of the absolute differences in avenue and street
	 */
	private int findDistance(int avenue, int street) {
		int diffAvenue = Math.abs(this.getAvenue() - avenue); // Difference in avenue
		int diffStreet = Math.abs(this.getStreet() - street); // Difference in street
		return diffAvenue + diffStreet; // Return total distance
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