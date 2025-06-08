package summativeGame;



import java.util.ArrayList;

import becker.robots.*;



public class killerBot extends GameRobot {



	public killerBot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, double dodgeChance, double catchChance){

		super(c, street, avenue, d, arena, energy, speed,type,dodgeChance,catchChance);

	}



	public GameRobot[] moveTurn(GameRobot[] robots, int i,Coordinates[] itemInfo) {
		

		this.turnLeft();
		

		return robots;
	}


}