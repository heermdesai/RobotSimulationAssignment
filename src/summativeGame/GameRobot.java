package summativeGame;

import java.util.ArrayList;

import becker.robots.*;
import becker.robots.Direction;
import becker.robots.RobotSE;

public abstract class GameRobot extends RobotSE {
	private Arena arena;
	private int energy;
	private int speed;
	private int x;
	private int y;
	private String name;
	private boolean frozen;
	private Coordinates[] itemInfo;

	public GameRobot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, double dodgeChance, double catchChance) {
		super(c, street, avenue, d);
		this.arena = arena;
		this.energy = energy;
		this.speed = speed;
		this.name=type;
		this.x=street;
		this.y=avenue;
		this.frozen=false;
	}
	
	public String getName() { 
		return name; 
	}
	
	public int getAvenue() { 
		return super.getAvenue(); 
	}
	
	public int getStreet() { 
		return super.getStreet(); 
	}
	
	public boolean isRobotFrozen() { 
		return frozen; 
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public int getRobotSpeed() {
		return this.speed;
	}
	
	public Coordinates[] getItemInfo() {

		// Handle expection please
		/*
		 * if(this.itemInfo.) { Cordinates[] empty = {}; return empty; }
		 */
		return this.itemInfo;
	}
	
	public void pickThing(int num) {
		for(int i = 0; i < num; i++) {
			this.pickThing();
		}
	}
	
	public void setItemInfo(Coordinates[] itemInfo) {
		this.itemInfo=itemInfo;
	}
	
	public void setName(String name) { 
		this.name=name; 
	}
	
	public void setAvenue(int x) { 
		this.x=x; 
	}
	
	public void setStreet(int y) { 
		this.y=y; 
	}
	
	public void setEnergy(int energy) {
		this.energy = energy;
	}
	
	public void setIsRobotFrozen(boolean isFrozen) { 
		this.frozen=isFrozen; 
	}
	
	public void move() {
		//System.out.println(this.getName() + " moving to (" + this.getStreet() + ", " + this.getAvenue() + ")");

		if(this.getEnergy() <= 0) {
			return;
		}
		super.move();
		this.setEnergy(this.getEnergy() - 1);
	}
	
	public abstract GameRobot[] moveTurn(GameRobot[] playersInfo, int i, Coordinates[] itemCoordinates);

}
