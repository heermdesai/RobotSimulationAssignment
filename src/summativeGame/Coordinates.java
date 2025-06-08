package summativeGame;

public class Coordinates {
	private int avenue;
	private int street;
	private boolean isPicked;
	private boolean isFinalLocation;
	private boolean isFinalPosition;

	public Coordinates(int street, int avenue) {
		this.street = street;
		this.avenue = avenue;
		this.isPicked = false;
		this.isFinalPosition = false;
	}

	public int getAvenue() {
		return avenue;
	}

	public int getStreet() {
		return street;
	}

	public boolean getIsPicked() {
		return isPicked;
	}
	
	public boolean getIsFinalPosition() {
		return isFinalLocation;
	}

	public void setIsFinalPosition(boolean isFinalLocation) {
		this.isFinalLocation = isFinalLocation;
	}

	public void setIsPicked(boolean isPicked) {
		this.isPicked = isPicked;
	}

	
	public void setAvenue(int avenue) {
		this.avenue = avenue;
	}

	public void setStreet(int street) {
		this.street = street;
	}

	public String toString() {
		return "(" + this.getAvenue() + ", " + this.getStreet() + " " + this.getIsPicked() + ")";
	}
}
