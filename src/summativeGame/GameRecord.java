package summativeGame;

public  class GameRecord {
	//private Arena arena;
	private int energy;
	private double speed;
	private int x;
	private int y;
	private String name;
	private boolean frozen;
	private int dodgeChance;
	private int catchChance;
	private int id;
	//private Cordinates[] itemInfo;

	public GameRecord(int id,int street, int avenue, int energy, double speed, String type,boolean frozen, int dodgeChance, int catchChance) {
		//super(c, street, avenue, d);
		//this.arena = arena;
		this.energy = energy;
		this.speed = speed;
		this.name=type;
		this.y=street;
		this.x=avenue;
		this.dodgeChance=dodgeChance;
		this.catchChance=catchChance;
		this.frozen=frozen;
		this.id=id;
	}



	public String getName() { 
		return name; 
	}
	
	public int getAvenue() { 
		return this.x; 
	}
	public int getID() { 
		return this.id; 
	}
	
	public int getStreet() { 
		return this.y; 
	}
	
	public boolean isRobotFrozen() { 
		return frozen; 
	}

	public int getEnergy() { 
		return this.energy; 
	}
	
	public double getSpeed() { // check why this is double
		return this.speed; 
	}
	
	public int getDodgeChance() { 
		return this.dodgeChance; 
	}
	
	public int getCatchChance() { 
		return this.catchChance; 
	}
	
	public void setEnergy(int energy) {
		this.energy=energy;
	}
	
	public void setSpeed(double speed) { 
		 this.speed=speed; 
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
	
	public void SetisRobotFrozen(boolean isFrozen) { 
		this.frozen=isFrozen; 
	}
	
	public void setDodgeChance(int dodgeChance) { 
		this.dodgeChance=dodgeChance; 
	}
	
	public void setCatchChance(int catchChance) { 
		this.catchChance=catchChance; 
	}

}
