package summativeGame;
import java.util.*;

import becker.robots.*;

/**
 * Medic Robot class that moves the robot according to its unique properties.
 * @author Samarvir
 * @version June 13th, 2025
 */
public class MedicRobot extends GameRobot{
	
	private Arena arena;
	int turnCount=0;

	/**
	 * Constructor method to initialize the medic robot and its properties
	 * @param id : ID is the unique identity number assigned to the robot
	 * @param c: The city in which the robot is initialized
	 * @param street : The street where the medic robot starts
	 * @param avenue : The avenue where the medic robot starts
	 * @param d: The initial direction of the robot
	 * @param arena: The arena where the robot is contained
	 * @param energy: The energy level of robot
	 * @param speed: Speed is the number of steps the robot can move
	 * @param type: DEtermine the what type of robot is that (Medic is this case)
	 * @param dodgeChance : The dodging ability of the robot
	 * @param catchChance : The catching ability of the robot
	 */
	public MedicRobot(int id,City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, int dodgeChance, int catchChance){
		super(id,c, street, avenue, d, arena, energy, speed,type,dodgeChance,catchChance);
		this.arena=arena;
	}

	/**
	 * Method that allow medic robot to take its turn and move
	 * @param robots: Contains the information of all other robots
	 * @param index: Tells what position in the robots array is the medic Robot that is moving its turn
	 * @param inteInfo : Contains the locations of all the items inside the arena
	 * @return : returns the updated status if the medic has unfreezen some other robot or not.
	 */
	public MoveStatus moveTurn(GameRecord[] robots, int index,Coordinates[] itemInfo) {

		turnCount++;
		MoveStatus status;

		int energy=this.getEnergy();
		double speed = this.getSpeed();
		int steps=determineSteps(speed,energy);
		int[][] killerLocation = findKillerLocation(robots); // finds the killer location
		killerLocation=selectionSortByDistance(killerLocation,this.getStreet(),this.getAvenue()); //sort for closest killer location
		int killerStreet = killerLocation[0][0];
		int killerAvenue = killerLocation[0][1];
		int numRobotsFroxen= checkIsRobotsFrozen(robots); // find the number of frozen robots

		// If medic finds frozen robot, it follows strategy 1
		// Otherwise, medic robot follows strategy 2
		if(numRobotsFroxen>0) {
			int[][] frozenCrewMatesLocation = new int[numRobotsFroxen][2];
			frozenCrewMatesLocation= getFrozenRobotsLocation(robots,numRobotsFroxen); // gets the locations of all frozen robots
			frozenCrewMatesLocation= selectionSortByDistance(frozenCrewMatesLocation,this.getStreet(),this.getAvenue()); // Sort the locations of all frozen robots
			int targetStreet = frozenCrewMatesLocation[0][0];
			int targetAvenue = frozenCrewMatesLocation[0][1];
			status=this.moveStratergy1(robots,steps,energy,targetStreet,targetAvenue,killerStreet,killerAvenue);
		}
		else {
			status=this.moveStratergy2(robots,steps,energy,killerStreet,killerAvenue);
		}

		// After every 5 turns, the robot's energy is increased by 10 units.
		if (turnCount==5) {
			energy+=10;

			// Energy cannot be greater than 100 units.
			if(energy>=100) {
				energy=100;
			}
			turnCount=0;
			this.setEnergy(energy);
		}

		return status;
	}


	/**
	 * Method the determine the number of steps to be moved by the medic robot
	 * @param speed : The speed of medic robot
	 * @param energy : The energy of the medic robot
	 * @return : Returns the number of steps to be moved by the robot
	 */
	private int determineSteps(double speed, int energy) {

		int steps=(int)speed;

		// If energy is less than half, robot moves less steps
		if(energy<50) {
			steps--;
		}

		return steps;
	}

	/**
	 * Method to sort the locations of all frozen robots
	 * @param locations: Contains the location of all frozen robots
	 * @param medicStreet: The street of medic robot at that point
	 * @param medicAvenue: The avenue of medic robot at that point
	 * @return : Returns the sorted array of locations of frozen robot 
	 */
	private int[][] selectionSortByDistance(int[][] locations, int medicStreet, int medicAvenue) {

		int n = locations.length;

		// selection sort algorithm
		for (int i =0;i <n-1; i++) {
			int minIndex = i;
			int minDist = distanceSquared(medicStreet, medicAvenue, locations[i][0], locations[i][1]);

			for (int j = i+1; j< n;j++) {
				int dist = distanceSquared(medicStreet, medicAvenue, locations[j][0], locations[j][1]);
				if (dist < minDist) {
					minIndex = j;
					minDist = dist;
				}
			}

			// Swapping locations[i] and locations[minIndex]
			if (minIndex != i) {
				int tempStreet = locations[i][0];
				int tempAvenue = locations[i][1];

				locations[i][0] = locations[minIndex][0];
				locations[i][1] = locations[minIndex][1];

				locations[minIndex][0] = tempStreet;
				locations[minIndex][1] = tempAvenue;
			}
		}

		return locations;
	}

	/**
	 * Method to determine the distance between 2 points
	 * @param x1 : first x point
	 * @param y1 : first y point
	 * @param x2 : second x point
	 * @param y2 : second y point
	 * @return : return the square of distance between the 2 points
	 */
	private int distanceSquared(int x1, int y1, int x2, int y2) {
		return ((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}

	/**
	 * Method to kind the locations of killers
	 * @param robots: Contains the information of all robots
	 * @return : Returns the array of locations of all killer robots
	 */
	private int[][] findKillerLocation(GameRecord[] robots) {

		int count=0;

		// Counts the number of killer robots
		for(int i=0;i<robots.length;i++) {
			if(robots[i].getName().equalsIgnoreCase("killer")) {
				count++;
			}
		}

		int [][] killerLocations = new int[count][2];
		int j=0;

		// gets the locations of all killer robots
		for(int i =0; i<robots.length;i++) {
			if(robots[i].getName().equalsIgnoreCase("killer")) {
				killerLocations[j][0]=robots[i].getStreet();
				killerLocations[j][1]=robots[i].getAvenue();
				j++;
			}
		}

		return killerLocations;
	}

	/**
	 * Method to get location of all frozen robots
	 * @param robots : Contains the information of all robots
	 * @param crewMateFroxen : Count of frozen robots
	 * @return : Returns the array of locations of frozen robots
	 */
	private int[][] getFrozenRobotsLocation(GameRecord[] robots, int crewMateFroxen) {

		int[][] cordinates = new int[crewMateFroxen][2];
		int j=0;

		// gets the location of frozen medic or crew robots
		for(int i =0; i<robots.length;i++) {
			if((robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic"))&& robots[i].isRobotFrozen()) {
				cordinates[j][0]=robots[i].getStreet();
				cordinates[j][1]=robots[i].getAvenue();
				j++;
			}
		}
		return cordinates;
	}

	/**
	 * Method to count the number of frozen crew or medic robots
	 * @param robots : Contains the information of all robots
	 * @return : return the count of all froze medic or crew robots
	 */
	private int checkIsRobotsFrozen(GameRecord[] robots) {

		int numRobotsFroxen=0;

		// determines the count of all frozen robots
		for(int i=0;i<robots.length;i++) {
			if(robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic")) {
				if(robots[i].isRobotFrozen()) {
					numRobotsFroxen++;
				}
			}
		}

		return numRobotsFroxen;	
	}

	/**
	 * Strategy 1 : If the killer is away and the medic find any frozen robot, its goes to that location and unfreeze the robots
	 * @param robots : Contains the information of all robots
	 * @param moveSteps : The number of steps that medic robot can move
	 * @param energy : The energy of medic robot
	 * @param targetStreet : The target street where medic wants to go
	 * @param targetAvenue : The target avenue where medic wants to go
	 * @param killerStreet : The current street of killer robot
	 * @param killerAvenue : The current avenue of killer robot
	 * @return : Returns whether medic has unfreeze the robot or not 
	 */
	private MoveStatus moveStratergy1(GameRecord[] robots,int moveSteps,int energy,int targetStreet, int targetAvenue, int killerStreet, int killerAvenue) {

		int steps = 0;
		MoveStatus status= new MoveStatus(-1,-1,"");

		// moves until medic reaches the desired target
		while (steps < moveSteps && (this.getStreet() != targetStreet || this.getAvenue() != targetAvenue)) {

			boolean moved = moveTowardTarget(targetStreet, targetAvenue, killerStreet, killerAvenue); // moves toward target
			steps++;

			// if the robot moved, decrement the energy
			if(moved) {
				energy-=1;

				// Energy cannot be zero
				if(energy<=0) {
					energy=1;
				}
				this.setEnergy(energy);
			}

			status=unfreezeRobot(robots);
		}
		return status;
	}

	/**
	 * Strategy 2 : if the killer is close, moves away from killer
	 * @param robots : Contains the information of all robots
	 * @param numSteps : Number of steps the medic robot can move
	 * @param energy : The energy of medic robot
	 * @param killerStreet : The current street of the killer robot
	 * @param killerAvenue : The current avenue of the killer robot
	 * @return : Returns the status if the medic robot on his way away from killer unfreezes some frozen robot.
	 */
	private MoveStatus moveStratergy2(GameRecord[] robots,int numSteps, int energy,int killerStreet, int killerAvenue) {

		int steps = 0;
		MoveStatus status= new MoveStatus(-1,-1,"");

		// Medic robot moves numSteps steps
		while (steps < numSteps) {
			int currStreet = this.getStreet();
			int currAvenue = this.getAvenue();
			boolean moved = false;

			// Checks if killer is away and medic robot is safe
			if (isDangerous(currStreet,currAvenue,killerStreet,killerAvenue)) {

				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps); // moves away from killer
				status=unfreezeRobot(robots);
				
				// decrement energy after every move
				energy-=1;
				if(energy<=0) {
					energy=1;
				}
				this.setEnergy(energy);
			} 
			else {
				
				// if the Robot does not move, it rested. So energy gets increases
				energy+=5;
				if(energy>=100) {
					energy=100;
				}
				this.setEnergy(energy);
			}

			// Cannot move — exit loop
			if (!moved) {
				break;
			}
			steps++;
		}
		
		return status;
	}

	/**
	 * Allows the medic to move to a safe distance from the killer if it is around
	 * @param killerStreet: The current street of killer
	 * @param killerAvenue: The current avenue of killer
	 * @param numSteps : The number of steps the medic robot can move
	 * @return : Returns if the medic robot has moved or not
	 */
	private boolean moveAwayFromKiller(int killerStreet, int killerAvenue, int numSteps) {
		
		int steps = 0;
		boolean movedAtLeastOnce = false;

		// Medic robot moves numSteps times until it gets to a safe spot
		while (steps < numSteps) {
			int currStreet = this.getStreet();
			int currAvenue = this.getAvenue();
			int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // NORTH, SOUTH, WEST, EAST
			int maxDistance = -1;
			ArrayList<Integer> bestDirIndex = new ArrayList<Integer>();

			// Checks all the possible safe locations to move
			for (int i = 0; i < directions.length; i++) {
				int newStreet = currStreet + directions[i][0];
				int newAvenue = currAvenue + directions[i][1];

				// checks if the new location is is valid location
				if (isValidMove(newStreet, newAvenue)) {
					int distance = Math.abs(newStreet - killerStreet) + Math.abs(newAvenue - killerAvenue);

					// Checks if the new valid safe spot is at max distance from killer 
					if (distance > maxDistance) {
						maxDistance = distance;
						bestDirIndex.clear();
						bestDirIndex.add(i);
					} 
					else if (distance == maxDistance) {
						bestDirIndex.add(i);
					}
				}
			}
			
			// Picks a random location for the medic robot to move
			if (bestDirIndex.size() != 0) {
				Random rand = new Random();
				int randomIndex = rand.nextInt(bestDirIndex.size());
				int chosenDirection = bestDirIndex.get(randomIndex);
				int streetChange = directions[chosenDirection][0];
				int avenueChange = directions[chosenDirection][1];
				moveInDirection(streetChange, avenueChange);
				movedAtLeastOnce = true;
				steps++;
			} 
			else {
				// No more moves possible
				break;
			}
		}

		return movedAtLeastOnce;
	}

	/**
	 * Method to change medic robots direction and move it
	 * @param streetChange : The street the medic robot wants to move
	 * @param avenueChange : The avenue the medic robot wants to move
	 */
	private void moveInDirection(int streetChange, int avenueChange) {
		
		if (streetChange == -1) {
			this.rotateToDirection(Direction.NORTH);
		}
		else if (streetChange == 1) {
			this.rotateToDirection(Direction.SOUTH);
		}
		else if (avenueChange == -1) {
			this.rotateToDirection(Direction.WEST);
		}
		else if (avenueChange == 1) {
			this.rotateToDirection(Direction.EAST);
		}

		this.move();
	}

	/**
	 * Method that moves the medic robot towards the target robot that is frozen through random path
	 * @param targetStreet : The target street that medic robot wants to go
	 * @param targetAvenue : The target avenue that medic robot wants to go
	 * @param killerStreet : The current street of the killer robot
	 * @param killerAvenue : The current avenue of the killer robot
	 * @return : Returns if the medic robot has moved or not
	 */
	private boolean moveTowardTarget(int targetStreet, int targetAvenue, int killerStreet, int killerAvenue) {
		int currStreet = this.getStreet();
		int currAvenue = this.getAvenue();
		Random rand = new Random();
		int randomNumber = rand.nextInt(2) + 1; 
		
		// Path option 1 (move through street first and then avenue)
		if(randomNumber==1) {
			
			if (currStreet < targetStreet && !isDangerous(currStreet + 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.SOUTH);
				this.move(); 
				return true;
			} 
			else if (currStreet > targetStreet && !isDangerous(currStreet - 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.NORTH);
				this.move();
				return true;
			} 
			else if (currAvenue < targetAvenue && !isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.EAST);
				this.move();
				return true;
			} 
			else if (currAvenue > targetAvenue && !isDangerous(currStreet, currAvenue - 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.WEST);
				this.move(); 
				return true;
			}
			else { // If killer is close, medic moves away from killer
				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				boolean moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps);
				
				if (!moved) {
					System.out.println("Medic: No move possible, staying put.");
				}
				return moved;
			}
		}
		else { // Path option 2 (move through avenue first and then street)
			if (currAvenue < targetAvenue && !isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue)) {
				if(!isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue))
					this.rotateToDirection(Direction.EAST);
				this.move();
				return true;
			} 
			else if (currAvenue > targetAvenue && !isDangerous(currStreet, currAvenue - 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.WEST);
				this.move(); 
				return true;
			}
			else if (currStreet < targetStreet && !isDangerous(currStreet + 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.SOUTH);
				this.move(); 
				return true;
			} 
			else if (currStreet > targetStreet && !isDangerous(currStreet - 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.NORTH);
				this.move();
				return true;
			} 
			else {
				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				boolean moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps);
				
				if (!moved) {
					System.out.println("Medic: No move possible, staying put.");
				}
				return moved;
			}
		}
	}

	/**
	 * Method to check the robot is within safe distance fom the killer robot
	 * @param row : Current row of the medic robot
	 * @param col : Current column of the medic robot
	 * @param killerRow : Current row of killer robot
	 * @param killerCol : Current column of killer robot
	 * @return : Returns if the robot is in danger or not
	 */
	private boolean isDangerous(int row, int col, int killerRow, int killerCol) {
		int distance = Math.abs(row - killerRow) + Math.abs(col - killerCol);
		return distance <=5;  // Stay at least 5 blocks away
	}

	/**
	 * Method to turn robot to the desired direction
	 * @param targetDirection : the direction robots wants to be turned
	 */
	private void rotateToDirection(Direction targetDirection) {
		while(this.getDirection() != targetDirection) {
			this.turnLeft();
		}
	}

	/**
	 * Method to check if the new locations is valid to move or not
	 * @param targetStreet : the street the robot wants to move
	 * @param targetAvenue : the avenue the robot wants to move
	 * @return : returns if the next move is safe or not
	 */
	private boolean isValidMove(int targetStreet, int targetAvenue) {
		
		// Check bounds of the grid
		if (targetStreet < 0 || targetStreet >= (arena.getHeight()-1) || targetAvenue < 0 || targetAvenue >= (arena.getWidth()-1)) {
			return false;
		}

		// If target is adjacent
		int streetDiff = Math.abs(targetStreet - this.getStreet());
		int avenueDiff = Math.abs(targetAvenue - this.getAvenue());

		// Only move 1 step N/S/E/W
		if ((streetDiff == 1 && avenueDiff == 0) || (streetDiff == 0 && avenueDiff == 1)) {
			return true; // It's a valid adjacent move, assume no wall
		}
		return false;
	}

	/**
	 * Method to unfreeze the frozen robots
	 * @param robots : contains the informations of all robots
	 * @return : returns the status whether the medic robot has unfreeze any robot
	 */
	private MoveStatus unfreezeRobot(GameRecord[] robots) {

		MoveStatus status= new MoveStatus(-1,-1,"");
		
		// checks for frozen robot and unfreeze them
		for (int i = 0; i < robots.length; i++) {
			if ((robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic")) &&
					robots[i].getStreet() == this.getStreet() &&
					robots[i].getAvenue() == this.getAvenue() &&
					robots[i].isRobotFrozen()) {

				status = new MoveStatus(this.getID(),robots[i].getID(),"unfreeze");				
				break; // stop after unfreezing one
			}
		}
		return status;
	}

}