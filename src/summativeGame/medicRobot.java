package summativeGame;
import becker.robots.*;


public class medicRobot extends GameRobot{
	
	public medicRobot(City c, int street, int avenue, Direction d, Arena arena, int energy, int speed, String type, double dodgeChance, double catchChance){
		super(c, street, avenue, d, arena, energy, speed,type,dodgeChance,catchChance);
		}

	public void move() {
		this.turnAround();
//		int a=playerRecord[0].getSpeed();
//		int b= playerRecord[0].getX();
//		System.out.println(a+ " " +b);
//		
//		
//		playerRecord[0].setX(4);
//		playerRecord[0].setCatchChance(4);
//		
//		a=playerRecord[0].getX();
//		double c= playerRecord[0].getCatchChance();
//		System.out.println(a+ " " +c);

		
		

	}

	@Override
	public GameRobot[] moveTurn(GameRobot[] playersInfo, int i, Coordinates[] itemCoordinates) {
		// TODO Auto-generated method stub
		return null;
	}
}
