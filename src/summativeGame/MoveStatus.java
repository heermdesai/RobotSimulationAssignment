package summativeGame;

public class MoveStatus {

	
	private int robotId;
	private int targetRobotId;
	private String operation;
	
	public MoveStatus(int robotId, int targetRobotId, String operation) {
		this.robotId = robotId;
		this.targetRobotId = targetRobotId;
		this.operation = operation;
	}

	public int getRobotId() {
		return robotId;
	}

	public int getTargetRobotId() {
		return targetRobotId;
	}

	public String getOperation() {
		return operation;
	}
	
}
