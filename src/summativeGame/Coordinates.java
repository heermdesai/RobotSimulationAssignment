package summativeGame;

/**
 * Gets coordinates for an object based on what street and avenue it is on
 * 
 * @author heerm, aditya, samarvir
 * @version June 13, 2025
 */
public class Coordinates {
	// Declaring private variables for the coordinates
	private int avenue;
	private int street;

	// Declaring attributes of the items at coordinates
	private boolean isPicked;
	private boolean isFinalPosition;

	/**
	 * Constructor method that initializes the avenue (x) and street (y)
	 * coordinates, as well as sets the isPicked and isFinalPosition to false
	 * 
	 * @param street - the y coordinate where the object is located
	 * @param avenue - the x coordinate where the object is located
	 */
	public Coordinates(int street, int avenue) {
		this.street = street;
		this.avenue = avenue;
		this.isPicked = false;
		this.isFinalPosition = false;
	}

	/**
	 * Accessor method for the x coordinate, or the avenue
	 * 
	 * @return - the avenue an object is on (x coordinate)
	 */
	public int getAvenue() {
		return avenue;
	}

	/**
	 * Accessor method for the y coordinate, or the street
	 * 
	 * @return - the street an object is on (y coordinate)
	 */
	public int getStreet() {
		return street;
	}

	/**
	 * Accessor method for the isPicked value
	 * 
	 * @return - if the item with this coordinate has been picked or not
	 */
	public boolean getIsPicked() {
		return isPicked;
	}

	/**
	 * Setter method for the isPicked value
	 * 
	 * @param - sets the item with this coordinate to picked or not
	 */
	public void setIsPicked(boolean isPicked) {
		this.isPicked = isPicked;
	}

	/**
	 * Accessor method for the isFinalPosition value
	 * 
	 * @return - if the item with this coordinate is at its final position or not
	 */
	public boolean getIsFinalPosition() {
		return isFinalPosition;
	}

	/**
	 * Setter method for the isFinalPosition value
	 * 
	 * @param - sets the item with this coordinate to true if it is at final
	 *          position, fakse if its not
	 */
	public void setIsFinalPosition(boolean isFinalPosition) {
		this.isFinalPosition = isFinalPosition;
	}

	/**
	 * Setter method to update x coordinate/avenue
	 * 
	 * @param avenue - the new x coordinate
	 */
	public void setAvenue(int avenue) {
		this.avenue = avenue;
	}

	/**
	 * Setter method to update y coordinate/street
	 * 
	 * @param street - the new y coordinate
	 */
	public void setStreet(int street) {
		this.street = street;
	}

	/**
	 * toString method that prints the values if called
	 */
	public String toString() {
		// Consistent with (street, avenue) or (y, x) convention
		return "(" + this.getStreet() + ", " + this.getAvenue() + ", Picked: " + this.getIsPicked() + ", Final: "
				+ this.getIsFinalPosition() + ")";
	}
}