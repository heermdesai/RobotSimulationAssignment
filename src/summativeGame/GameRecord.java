package summativeGame;

/**
 * Player Record class which will contain record of informations for all robots
 * @author Samarvir, Aditya, Heer
 * @version June 13th, 2025
 */
public class GameRecord {
	
	// All attributes that are used 
	private int energy;
	private double speed;
	private int x;
	private int y;
	private String name;
	private boolean frozen;
	private int dodgeChance;
	private int catchChance;
	private int id;

	/**
	 * Constructor method to initial the record of each player
	 * @param id : The unique ID for wach robot
	 * @param street: The street of robot
	 * @param avenue: The avenue of robot
	 * @param energy: The energy of robot
	 * @param speed: The speed of robot
	 * @param type: The type of robot (medic, crew or killer robot)
	 * @param frozen: The state of robot(frozen or unfrozen)
	 * @param dodgeChance: The dodging change of robot
	 * @param catchChance: The catching chance of robot
	 */
	public GameRecord(int id, int street, int avenue, int energy, double speed, String type, boolean frozen, int dodgeChance, int catchChance) {
		this.id = id;
		this.y = street;
		this.x = avenue;
		this.energy = energy;
		this.speed = speed;
		this.name = type;
		this.frozen = frozen;
		this.dodgeChance = dodgeChance;
		this.catchChance = catchChance;
	}

	/**
	 * Method to get the type the robot
	 * @return : Return the type of robot
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method to get the avenue of robot
	 * @return : returns the avenue of robot
	 */
	public int getAvenue() {
		return this.x;
	}

	/**
	 * Method to get the id of robot
	 * @return : returns the id of robot
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Method to get the street of robot 
	 * @return : returns the street of root
	 */
	public int getStreet() {
		return this.y;
	}

	/**
	 * Method to get the status of robot (freezen or unfreezen)
	 * @return : return the status of robot
	 */
	public boolean isRobotFrozen() {
		return frozen;
	}

	/**
	 * Method to get the energy of robot
	 * @return: return the energy of robot
	 */
	public int getEnergy() {
		return this.energy;
	}

	/**
	 * Method to get the speed of robot
	 * @return: return the speed of robot
	 */
	public double getSpeed() {
		return this.speed;
	}

	/**
	 * Method to get the dodging ability of robot
	 * @return: return the dodging ability of robot
	 */
	public int getDodgeChance() {
		return this.dodgeChance;
	}

	/**
	 * Method to get the catching chance of robot
	 * @return: return the catching chance of robot
	 */
	public int getCatchChance() {
		return this.catchChance;
	}

	/**
	 * Method to set the energy of robot
	 * @param energy: Giving energy of robot
	 */
	public void setEnergy(int energy) {
		this.energy = energy;
	}

	/**
	 * Method to set the speed of robot
	 * @param energy: Giving speed of robot
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Method to set the name of robot
	 * @param energy: Giving name of robot
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Method to set the avenue of robot
	 * @param energy: Giving avenue of robot
	 */
	public void setAvenue(int x) {
		this.x = x;
	}

	/**
	 * Method to set the street of robot
	 * @param energy: Giving street of robot
	 */
	public void setStreet(int y) {
		this.y = y;
	}

	/**
	 * Method to set the status of robot (frozen or unfrozen)
	 * @param energy: Giving energy of robot
	 */
	public void SetisRobotFrozen(boolean isFrozen) {
		this.frozen = isFrozen;
	}

	/**
	 * Method to set the dodging chance of robot
	 * @param energy: Giving dodging chance of robot
	 */
	public void setDodgeChance(int dodgeChance) {
		this.dodgeChance = dodgeChance;
	}

	/**
	 * Method to set the catching chance of robot
	 * 
	 * @param energy: Giving catching of robot
	 */
	public void setCatchChance(int catchChance) {
		this.catchChance = catchChance;
	}
}
