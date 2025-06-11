package summativeGame;
import java.util.*;

import becker.robots.*;


public class MedicRobot extends GameRobot{

	public MedicRobot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, int dodgeChance, int catchChance){
		super(c, street, avenue, d, arena, energy, speed,type,dodgeChance,catchChance);
	}

	int turnCount=0;

	public GameRobot[] moveTurn(GameRobot[] robots, int index, Coordinates[] itemInfo) {

		turnCount++;

		int energy=robots[index].getEnergy();
		double speed = robots[index].getSpeed(); // check why double

		//int steps=determineSteps(speed,energy);

		int steps=5;
		int[] killerLocation = findKillerLocation(robots);
		int killerStreet = killerLocation[0];
		int killerAvenue = killerLocation[1];

		int numRobotsFroxen= checkIsRobotsFrozen(robots);
		if(numRobotsFroxen>0) {

			int [][] frozenCrewMatesLocation = new int[numRobotsFroxen][2];
			frozenCrewMatesLocation= getFrozenRobotsLocation(robots,numRobotsFroxen);
			frozenCrewMatesLocation= selectionSortByDistance(frozenCrewMatesLocation,this.getStreet(),this.getAvenue());
			int targetStreet = frozenCrewMatesLocation[0][0];
			int targetAvenue = frozenCrewMatesLocation[0][1];

			//System.out.println(killerX + " " + killerY);
			robots=this.moveStratergy1(robots,index,steps,energy,targetStreet,targetAvenue,killerStreet,killerAvenue);
		}
		else {
			robots=this.moveStratergy2(robots, index,steps,energy,killerStreet,killerAvenue);
		}

		//System.out.println(robots[index].getEnergy());
		//this.turnAround();
		//this.move();
		robots[index].setAvenue(this.getAvenue());
		robots[index].setStreet(this.getStreet());

		if (turnCount==5) {
			energy+=10;
			if(energy>=100) {
				energy=100;
			}
			turnCount=0;
			robots[index].setEnergy(energy);
		}
		return robots;
	}

	private int determineSteps(double speed, int energy) {
		int steps=1;

		if(speed>0 && speed<30) {
			steps=2;
		}
		else if(speed>=30&&speed<50) {
			steps=3;
		}
		else if(speed>=50&&speed<80) {
			steps=4;
		}
		else if(speed>=80&&speed<=100) {
			steps=7;
		}

		if(energy<50) {
			steps--;
		}
		return steps;
	}

	private int[][] selectionSortByDistance(int[][] locations, int medicStreet, int medicAvenue) {
		int n = locations.length;

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

			// Swap locations[i] and locations[minIndex]
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

	private int distanceSquared(int x1, int y1, int x2, int y2) {
		return ((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}

	private int[] findKillerLocation(GameRobot[] robots) {
		int killerY = -1;
		int killerX = -1;
		for (int i = 0; i < robots.length; i++) {
			if (robots[i].getName().equalsIgnoreCase("killer")) {
				killerY = robots[i].getStreet();   // Y
				killerX = robots[i].getAvenue();  // X
				break;
			}
		}
		return new int[]{killerY, killerX};
	}

	private int[][] getFrozenRobotsLocation(GameRobot[] robots, int crewMateFroxen) {

		int[][] cordinates = new int[crewMateFroxen][2];
		int j=0;
		for(int i =0; i<robots.length;i++) {
			if((robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic"))&& robots[i].isRobotFrozen()) {
				cordinates[j][0]=robots[i].getStreet();
				cordinates[j][1]=robots[i].getAvenue();
				j++;
			}
		}
		return cordinates;
	}

	private int checkIsRobotsFrozen(GameRobot[] robots) {

		int numRobotsFroxen=0;
		for(int i=0;i<robots.length;i++) {
			if(robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic")) {
				if(robots[i].isRobotFrozen()) {
					numRobotsFroxen++;
				}
			}
		}

		return numRobotsFroxen;	
	}

	private GameRobot[] moveStratergy1(GameRobot[] robots, int index,int moveSteps,int energy,int targetStreet, int targetAvenue, int killerStreet, int killerAvenue) {

		int steps = 0;
		
		while (steps < moveSteps && (this.getStreet() != targetStreet || this.getAvenue() != targetAvenue)) {
			//int currentStreet = this.getStreet();
			//int currentAvenue = this.getAvenue();

			/*
			 * // Step priority: vertical first, then horizontal if (currentStreet <
			 * targetStreet && !isDangerous(currentStreet + 1, currentAvenue, killerStreet,
			 * killerAvenue)) { this.setDirection(Direction.SOUTH); this.move(); } else if
			 * (currentStreet > targetStreet && !isDangerous(currentStreet - 1,
			 * currentAvenue, killerStreet, killerAvenue)) {
			 * this.setDirection(Direction.NORTH); this.move(); } else if (currentAvenue <
			 * targetAvenue && !isDangerous(currentStreet, currentAvenue + 1, killerStreet,
			 * killerAvenue)) { this.setDirection(Direction.EAST); this.move(); } else if
			 * (currentAvenue > targetAvenue && !isDangerous(currentStreet, currentAvenue -
			 * 1, killerStreet, killerAvenue)) { this.setDirection(Direction.WEST);
			 * this.move(); } else { // All options are dangerous or blocked
			 * System.out.println("Medic: No safe move available this turn."); break; }
			 */

			boolean moved = moveTowardTarget(targetStreet, targetAvenue, killerStreet, killerAvenue);
			/*
			 * boolean moved=false; if (isDangerous(currentStreet, currentAvenue,
			 * killerStreet, killerAvenue)) { // 🛡️ In danger zone → move away from killer
			 * moved = moveAwayFromKiller(killerStreet, killerAvenue); } else { // ✅ Safe →
			 * try to move toward target moved = moveTowardTarget(targetStreet,
			 * targetAvenue, killerStreet, killerAvenue); } if (!moved) {
			 * System.out.println("No move made, exiting loop to prevent infinite turn.");
			 * break; }
			 */

			steps++;

			if(moved) {
				energy-=3;
				if(energy<=0) {
					energy=1;
				}
				robots[index].setEnergy(energy);
			}
			robots=unfreezeRobot(robots);
		}
		return robots;
	}
	
	
	private GameRobot[] moveStratergy2(GameRobot[] robots, int index,int numSteps, int energy,int killerStreet, int killerAvenue) {

		int steps = 0;

		while (steps < numSteps) {
			int currStreet = this.getStreet();
			int currAvenue = this.getAvenue();
			//int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);

			boolean moved = false;
			if (isDangerous(currStreet,currAvenue,killerStreet,killerAvenue)) {
				
				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps);
				// Already have moveAwayFromKiller helper
				//moved =moveAwayFromKiller(killerStreet, killerAvenue,2);
				energy-=3;
				if(energy<=0) {
					energy=1;
				}
				robots[index].setEnergy(energy);

			} else {
				// Random safe move — let's write this to reuse your helpers
				//moved = randomSafeMove(killerStreet, killerAvenue);
				energy+=5;
				if(energy>=100) {
					energy=100;
				}
				robots[index].setEnergy(energy);
			}

			if (!moved) {
				// Cannot move — exit loop
				break;
			}
			robots=unfreezeRobot(robots);
			steps++;
		}
		return robots;

	}

	/*
	 * private boolean randomSafeMove(int killerStreet, int killerAvenue) { int[][]
	 * directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // NORTH, SOUTH, WEST, EAST
	 * ArrayList<int[]> safeMoves = new ArrayList<int[]>();
	 * 
	 * for (int[] dir : directions) { int newStreet = this.getStreet() + dir[0]; int
	 * newAvenue = this.getAvenue() + dir[1];
	 * 
	 * // Reuse isValidMove() and isDangerous() if (isValidMove(newStreet,
	 * newAvenue) && !isDangerous(newStreet, newAvenue, killerStreet, killerAvenue))
	 * { safeMoves.add(dir); } }
	 * 
	 * if (!safeMoves.isEmpty()) { Random rand = new Random(); int[] move =
	 * safeMoves.get(rand.nextInt(safeMoves.size())); // Reuse moveInDirection()
	 * that moves and rotates correctly moveInDirection(move[0], move[1]); return
	 * true; } else { // No safe move, stay put
	 * System.out.println("Medic: No safe moves available, staying put."); return
	 * false; } }
	 */

	private boolean moveAwayFromKiller(int killerStreet, int killerAvenue) {
		int currStreet = this.getStreet();
		int currAvenue = this.getAvenue();

		int[][] directions = {{-1, 0},{1, 0},{0, -1},{0, 1}}; // NORTH, SOUTH, WEST, EAST

		int maxDistance = -1;
		int bestDirIndex = -1;

		for (int i = 0; i < directions.length; i++) {
			int newStreet = currStreet + directions[i][0];
			int newAvenue = currAvenue + directions[i][1];
			int distance = Math.abs(newStreet - killerStreet) + Math.abs(newAvenue - killerAvenue);

			if (distance > maxDistance && isValidMove(newStreet, newAvenue)) {
				maxDistance = distance;
				bestDirIndex = i;
			}
		}

		if (bestDirIndex != -1) {
			int streetChange = directions[bestDirIndex][0];
			int avenueChange = directions[bestDirIndex][1];


			int newStreet = currStreet + streetChange;
			int newAvenue = currAvenue + avenueChange;

			moveInDirection(streetChange, avenueChange);
			return true;
			//return (this.getStreet() != currStreet || this.getAvenue() != currAvenue);
		}

		return false;
	}
	
	private boolean moveAwayFromKiller(int killerStreet, int killerAvenue, int numSteps) {
	    int steps = 0;
	    boolean movedAtLeastOnce = false;

	    while (steps < numSteps) {
	        int currStreet = this.getStreet();
	        int currAvenue = this.getAvenue();

	        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // NORTH, SOUTH, WEST, EAST
	        int maxDistance = -1;
	        int bestDirIndex = -1;

	        for (int i = 0; i < directions.length; i++) {
	            int newStreet = currStreet + directions[i][0];
	            int newAvenue = currAvenue + directions[i][1];
	            int distance = Math.abs(newStreet - killerStreet) + Math.abs(newAvenue - killerAvenue);

	            if (distance > maxDistance && isValidMove(newStreet, newAvenue)) {
	                maxDistance = distance;
	                bestDirIndex = i;
	            }
	        }

	        if (bestDirIndex != -1) {
	            int streetChange = directions[bestDirIndex][0];
	            int avenueChange = directions[bestDirIndex][1];
	            moveInDirection(streetChange, avenueChange);

	            movedAtLeastOnce = true;
	            steps++;
	        } else {
	            // No more moves possible
	            break;
	        }
	    }
	    return movedAtLeastOnce;
	}

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

	private boolean moveTowardTarget(int targetStreet, int targetAvenue, int killerStreet, int killerAvenue) {
		int currStreet = this.getStreet();
		int currAvenue = this.getAvenue();

		Random rand = new Random();
		int randomNumber = rand.nextInt(2) + 1; 
		if(randomNumber==1) {
			if (currStreet < targetStreet && !isDangerous(currStreet + 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.SOUTH);
				this.move(); 
				//return;
				return true;
			} 
			else if (currStreet > targetStreet && !isDangerous(currStreet - 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.NORTH);
				this.move();
				//return;
				return true;
			} 
			else if (currAvenue < targetAvenue && !isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.EAST);
				this.move();
				//return;
				return true;
			} 
			else if (currAvenue > targetAvenue && !isDangerous(currStreet, currAvenue - 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.WEST);
				this.move(); 
				//return;
				return true;
			}
			else {
				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				boolean moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps);
				//boolean moved = moveAwayFromKiller(killerStreet, killerAvenue);	
				//moved = moveAwayFromKiller(killerStreet, killerAvenue);	
				if (!moved) {
					System.out.println("Medic: No move possible, staying put.");
				}
				return moved;
			}
		}
		else {
			if (currAvenue < targetAvenue && !isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue)) {
				if(!isDangerous(currStreet, currAvenue + 1, killerStreet, killerAvenue))
					this.rotateToDirection(Direction.EAST);
				this.move();
				//return;
				return true;
			} 
			else if (currAvenue > targetAvenue && !isDangerous(currStreet, currAvenue - 1, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.WEST);
				this.move(); 
				//return;
				return true;
			}
			else if (currStreet < targetStreet && !isDangerous(currStreet + 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.SOUTH);
				this.move(); 
				//return;
				return true;
			} 
			else if (currStreet > targetStreet && !isDangerous(currStreet - 1, currAvenue, killerStreet, killerAvenue)) {
				this.rotateToDirection(Direction.NORTH);
				this.move();
				//return;
				return true;
			} 
			else {
				int distance = Math.abs(currStreet - killerStreet) + Math.abs(currAvenue - killerAvenue);
				int safeSteps = Math.max(1, 6 - distance);  // Closer killer → more steps
				boolean moved = moveAwayFromKiller(killerStreet, killerAvenue, safeSteps);
				//boolean moved = moveAwayFromKiller(killerStreet, killerAvenue);	
				//moved = moveAwayFromKiller(killerStreet, killerAvenue);	
				if (!moved) {
					System.out.println("Medic: No move possible, staying put.");
				}
				return moved;
			}
		}
		//this.move();
		//return false;
	}

	private boolean isDangerous(int row, int col, int killerRow, int killerCol) {
		int distance = Math.abs(row - killerRow) + Math.abs(col - killerCol);
		return distance <=5;  // Stay at least 4 blocks away
	}

	private void rotateToDirection(Direction targetDirection) {
		while(this.getDirection() != targetDirection) {
			this.turnLeft();
		}
	}

	/*
	 * private boolean isValidMove(int targetStreet, int targetAvenue) { // Check
	 * bounds if (targetStreet < 0 || targetStreet >= 12 || targetAvenue < 0 ||
	 * targetAvenue >= 23) { return false; }
	 * 
	 * // Save current position and direction int currentStreet = this.getStreet();
	 * int currentAvenue = this.getAvenue(); Direction originalDirection =
	 * this.getDirection();
	 * 
	 * // Set direction toward the target if (targetStreet < currentStreet) {
	 * this.rotateToDirection(Direction.NORTH); } else if (targetStreet >
	 * currentStreet) { this.rotateToDirection(Direction.SOUTH); } else if
	 * (targetAvenue < currentAvenue) { this.rotateToDirection(Direction.WEST); }
	 * else if (targetAvenue > currentAvenue) {
	 * this.rotateToDirection(Direction.EAST); }
	 * 
	 * // Check for wall in that direction boolean clear = this.frontIsClear();
	 * 
	 * // Restore original direction this.rotateToDirection(originalDirection);
	 * 
	 * return clear; }
	 */
	
	private boolean isValidMove(int targetStreet, int targetAvenue) {
	    // Check bounds of the grid
	    if (targetStreet < 0 || targetStreet >= 12|| targetAvenue < 0 || targetAvenue >= 11) {
	        return false;
	    }

	    // If target is adjacent
	    int streetDiff = Math.abs(targetStreet - this.getStreet());
	    int avenueDiff = Math.abs(targetAvenue - this.getAvenue());

	    // Only move 1 step N/S/E/W
	    if ((streetDiff == 1 && avenueDiff == 0) || (streetDiff == 0 && avenueDiff == 1)) {
	        return true; // ✅ It's a valid adjacent move, assume no wall
	    }
	    return false;
	}

	private GameRobot[] unfreezeRobot(GameRobot[] robots) {

		for (int i = 0; i < robots.length; i++) {
			if ((robots[i].getName().equalsIgnoreCase("crew")||robots[i].getName().equalsIgnoreCase("medic")) &&
					robots[i].getStreet() == this.getStreet() &&
					robots[i].getAvenue() == this.getAvenue() &&
					robots[i].isRobotFrozen()) {

				robots[i].setIsRobotFrozen(false); // Call your method to unfreeze
				break; // stop after unfreezing one
			}
		}
		return robots;
	}
	
	/*
	 * private boolean frontIsClear(Direction dir) { int currStreet =
	 * this.getStreet(); int currAvenue = this.getAvenue(); City city =
	 * this.getCity();
	 * 
	 * if (dir == Direction.NORTH) { return !city.getWall(new Intersection(city,
	 * currStreet, currAvenue), Direction.NORTH); } else if (dir == Direction.SOUTH)
	 * { return !city.getWall(new Intersection(city, currStreet, currAvenue),
	 * Direction.SOUTH); } else if (dir == Direction.EAST) { return
	 * !city.getWall(new Intersection(city, currStreet, currAvenue),
	 * Direction.EAST); } else if (dir == Direction.WEST) { return !city.getWall(new
	 * Intersection(city, currStreet, currAvenue), Direction.WEST); } return false;
	 * }
	 */
}
