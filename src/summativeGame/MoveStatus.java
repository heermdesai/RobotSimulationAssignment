package summativeGame;

public class MoveStatus {

	private int robotId;         // ID of the robot performing the move
	private int targetRobotId;   // ID of the target robot 
	private String operation;    // Description of the operation

	/**
	 * Constructor to create a MoveStatus object representing a robot's action.
	 *
	 * @param robotId - ID of the robot taking the action
	 * @param targetRobotId - ID of the robot being targeted, or -1 if none
	 * @param operation - the type of action performed e.g., "move", "freeze", "heal", "none"
	 */
	public MoveStatus(int robotId, int targetRobotId, String operation) {
		this.robotId = robotId;
		this.targetRobotId = targetRobotId;
		this.operation = operation;
	}

	/**
	 * @return the ID of the robot that performed the action
	 */
	public int getRobotId() {
		return this.robotId;
	}

	/**
	 * @return the ID of the robot that was targeted, or -1 if none
	 */
	public int getTargetRobotId() {
		return this.targetRobotId;
	}

	/**
	 * @return the operation string describing the action
	 */
	public String getOperation() {
		return this.operation;
	}
}