

import javaclient3.PlayerClient;
import javaclient3.Position2DInterface;
import javaclient3.PlayerException;
import javaclient3.structures.PlayerConstants;
import javaclient3.RangerInterface;

/*
 A class controlling the moving obstacle.
 */
public class Obstaclebot {
    public final static double MINDISTANCE = 1;
    public final static double SPEED = -0.3;
    PlayerClient robot=null;
    Position2DInterface pos2D=null;
    RangerInterface sonar=null;

    public Obstaclebot(int index) {
	try {
            robot = new PlayerClient("localhost", 6665);
            pos2D = robot.requestInterfacePosition2D(index,PlayerConstants.PLAYER_OPEN_MODE);  
            sonar = robot.requestInterfaceRanger(index,PlayerConstants.PLAYER_OPEN_MODE);
        } catch (PlayerException e) {
            System.err.println("Obstaclebot: Error connecting to Player!\n>>>" + e.toString());
            System.exit(1);
        }

        robot.runThreaded(-1,-1);
    }

    boolean forwardObstacle() {
	while (!sonar.isDataReady()) {
            try {Thread.sleep(25);} catch (InterruptedException e) {} 
        }
	double[] sonarvalues = sonar.getData().getRanges();
	return((sonarvalues[0]<MINDISTANCE) || (sonarvalues[1]<MINDISTANCE) || (sonarvalues[2]<MINDISTANCE));
    }

    boolean backwardObstacle() {
	while (!sonar.isDataReady()) {
            try {Thread.sleep(25);} catch (InterruptedException e) {} 
        }
	double[] sonarvalues = sonar.getData().getRanges();
	return((sonarvalues[3]<MINDISTANCE) || (sonarvalues[4]<MINDISTANCE) || (sonarvalues[5]<MINDISTANCE));
    }

    public void run() {
        while(true) {
	    pos2D.setSpeed(SPEED,0);
            while (!forwardObstacle()) {}
	    pos2D.setSpeed(-SPEED,0);
	    while (!backwardObstacle()) {}
	    
        }
    }

    public static void main(String[] args) {
	Obstaclebot bot = new Obstaclebot(1);
	bot.run();
    }
}
