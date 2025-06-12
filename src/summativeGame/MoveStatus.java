package summativeGame;

public class MoveStatus {

	
	private String robotId;
	private String targetRobotId;
	private String operation;
	
	public MoveStatus(String robotId, String targetRobotId, String operation) {
		this.robotId = robotId;
		this.targetRobotId = targetRobotId;
		this.operation = operation;
	}

	public String getRobotId() {
		return robotId;
	}

	public String getTargetRobotId() {
		return targetRobotId;
	}

	public String getOperation() {
		return operation;
	}
	
}
