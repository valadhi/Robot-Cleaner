import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTextArea;


import javaclient3.PlayerClient;
import javaclient3.Position2DInterface;
import javaclient3.PlayerException;
import javaclient3.structures.PlayerConstants;
import javaclient3.RangerInterface;

import java.util.*;
import java.awt.Point;

/**
 * BaseRobot is a base class for the maze runner problem. This class is to be
 * extended with additional methods to allow the mazerunner robot to navigate
 * the given labyrinth.
 */
public class VacuumBot {
	/**
	 * Constant denoting to what precision the robot measures yaw. Higher
	 * numbers indicate higher precision.
	 */
	static VacuumBot vacuumBot;
	protected final static int PRECISION = 200;
	public final static int NORTH = 0;
	public final static int EAST = 1;
	public final static int WEST = 2;
	public final static int SOUTH = 3;
	PlayerClient robot = null;
	Position2DInterface pos2D = null;
	RangerInterface sonar = null;
	int heading;
	double[] sonarValues;
	double[] lastValues;
	Map map;
	double x, y, yaw;
	int i, j;
	drawMap dm;
	EvalGUI evalgui;
	static VisualMap visualmap;
	AStar1 as;
	Vector<Cell> path = new Vector<Cell>();
	Vector<Cell> turningPoints = new Vector<Cell>();
	boolean pathFollowing = false;
	boolean turning = false;
	boolean avoidanceFlag = true;
	boolean recoveryFlag = true;
	boolean moveAlongPathFlag = true;
	boolean turningFlag = false;
	double direction = 1;
	int mapSize = 120;
	boolean turnBack = false;
	boolean c = false;
	boolean turning360 = false;
	boolean collisionFlag = false;
	boolean visited = false;


	Vector<Vector<Cell>> frontier = new Vector<Vector<Cell>>();
	Vector<Cell> tempFrontier = new Vector<Cell>();
	Cell oldTarget = new Cell(); 
	
	/**
	 * Constructor for the robot, which sets up the proxy objects and a thread
	 * to keep the sensor fields up to date.
	 */
	public VacuumBot() {

		vacuumBot = this;
		// Set up service proxies
		try {
			robot = new PlayerClient("localhost", 6665);
			pos2D = robot.requestInterfacePosition2D(0,
					PlayerConstants.PLAYER_OPEN_MODE);
			sonar = robot.requestInterfaceRanger(0,
					PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) {
			System.err.println("VacuumBot: Error connecting to Player!\n>>>"
					+ e.toString());
			System.exit(1);
		}
		map = new Map(mapSize);
		// System.out.println("avem "+Cell.cells.size()+" celule");

		robot.runThreaded(-1, -1);

		// pos2D.setSpeed(0.5,0);
		while (!sonar.isDataReady())
			;

		double[] sonarValues = sonar.getData().getRanges();
		for (double a : sonarValues) {
			System.out.println(a);
		}

		lastValues = new double[15];
		for (int i = 0; i < lastValues.length; i++) {
			lastValues[i] = sonarValues[i];
		}

		x = (Math.round(pos2D.getX() * 100)) / 100.0;
		y = (Math.round(pos2D.getY() * 100)) / 100.0;
		yaw = (Math.round(pos2D.getYaw() * 100)) / 100.0;

		dm = new drawMap(map);
		evalgui = new EvalGUI();
		evalgui.add(dm.getPanel());
		
		//for (int k=0;k<37;k++)
			//path.add(map.map[103-k][90]);
		
		//pathFollowing = true;
		
		positionChecker();
		sensorChecker();
		mapping();
		moveAlongPath();
		avoidance();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		checkTurningCollision();
		//recovery();

	}

	private void getFrontier() {

		tempFrontier.clear();

		for (int i = 1; i < map.map.length - 1; i++) {
			for (int j = 1; j < map.map[i].length - 1; j++) {
				if (map.map[i][j].getState() == 2 || 
					map.map[i][j].getState() == 4 ||
					map.map[i][j].getState() == 3) {
					if (map.map[i + 1][j].getState() == 1
							|| map.map[i - 1][j].getState() == 1
							|| map.map[i][j + 1].getState() == 1
							|| map.map[i][j - 1].getState() == 1) {

						tempFrontier.add(map.map[i][j]);
					}

				}
			}
		}
	}

	private void separateFrontier() {

		frontier.clear();
		int i = 0;
		while (!tempFrontier.isEmpty()) {

			Vector<Cell> temp = new Vector<Cell>();
			temp.add(tempFrontier.remove(0));
			frontier.add(temp);
			temp = null;

			for (int j = 0; j < frontier.get(i).size(); j++) {

				Cell a = frontier.get(i).get(j);

				for (int k = 0; k < tempFrontier.size(); k++) {
					Cell b = tempFrontier.get(k);
					if ((b.getX() + 1 == a.getX() && b.getY() == a.getY())
							|| (b.getX() - 1 == a.getX() && b.getY() == a
									.getY())
							|| (b.getX() + 1 == a.getX() && b.getY() + 1 == a
									.getY())
							|| (b.getX() + 1 == a.getX() && b.getY() - 1 == a
									.getY())
							|| (b.getX() == a.getX() && b.getY() + 1 == a
									.getY())
							|| (b.getX() == a.getX() && b.getY() - 1 == a
									.getY())
							|| (b.getX() - 1 == a.getX() && b.getY() + 1 == a
									.getY())
							|| (b.getX() - 1 == a.getX() && b.getY() - 1 == a
									.getY())) {
						tempFrontier.remove(b);
						frontier.get(i).add(b);
						b = null;
						k--;

					}
				}
				a = null;

			}
			// System.out.println("Area:" +i);
			i++;
		}

		for (int g = 0; g < frontier.size(); g++) {
			if (frontier.get(g).size() < 5) {
				frontier.remove(g);
				g--;
			}
		}
		
		for (int g = 0; g < frontier.size(); g++) {
			for (i=0;i<frontier.get(g).size();i++)
				frontier.get(g).get(i).setState(4);
		}

		//checkFrontier();

	}

	public void checkFrontier() {

		System.out.println("Intru sa reduc");
		int minX, maxX, minY, maxY;
		System.out.println("Inainte de reducere sunt " + frontier.size()
				+ " frontiere");

		if (frontier.size() == 1)
			return;

		for (int i = 0; i < frontier.size(); i++) {
			minX = Integer.MAX_VALUE;
			maxX = Integer.MIN_VALUE;
			minY = Integer.MAX_VALUE;
			maxY = Integer.MIN_VALUE;

			for (int j = 0; j < frontier.get(i).size(); j++) {
				if (minY > frontier.get(i).get(j).getY())
					minY = frontier.get(i).get(j).getY();

				if (maxY < frontier.get(i).get(j).getY())
					maxY = frontier.get(i).get(j).getY();

				if (minX > frontier.get(i).get(j).getX())
					minX = frontier.get(i).get(j).getX();

				if (maxX < frontier.get(i).get(j).getX())
					maxX = frontier.get(i).get(j).getX();
			}

			if (frontier.size() == 2) {
				if (frontier.get(0).size() > frontier.get(1).size())
					frontier.remove(1);
				else
					frontier.remove(0);
			}

			if (frontier.size() > 2) {
				if (Math.abs(minY - maxY) < 4) {
					System.out.println("AM ELIMAT PE Y");
					//for (int k = 0; k < frontier.get(i).size(); k++) {
					//	frontier.get(i).get(k).setState(5);
					//}
					frontier.remove(i);

					i--;
				} else if (Math.abs(minX - maxX) < 4) {
					System.out.println("AM ELIMINAT PE X");
					//for (int k = 0; k < frontier.get(i).size(); k++) {
					//	frontier.get(i).get(k).setState(5);
					//}
					frontier.remove(i);

					i--;
				}
			}

		}

		for (int i = 0; i < frontier.size(); i++) {
			for (int j = 0; j < frontier.get(i).size(); j++) {
				frontier.get(i).get(j).setState(4);
			}
		}
	}

	public boolean checkArea(int x, int y) {

		for (int i = x - 3; i <= x + 3; i++) {
			for (int j = y - 3; j <= y + 3; j++) {
				if (i < 0 || i >= map.getLength() || j < 0
						|| j >= map.getLength()) {
					return false;
				} else {
					if (map.map[i][j].getState() == Map.OCCUPIED) {
						return false;
					}
				}
			}
		}

		return true;
	}

	
	public void getNextTarget() {

		as = new AStar1(map);
		Cell target = new Cell();
		int min = Integer.MAX_VALUE;
		int min2 = Integer.MAX_VALUE;
		int k, p;
		int a, b;

		path.clear();
		Vector<Cell> tempPath = new Vector<Cell>();
		Vector<Cell> path2 = new Vector<Cell>();
		
		System.out.println("Incep sa caut target");
		
		for (k = 0; k < frontier.size(); k++) {
			Cell[] l = new Cell[frontier.get(k).size()];
	
			for (p = 0; p < frontier.get(k).size(); p++) {
				l[p] = frontier.get(k).get(p);
			}
			
			Arrays.sort(l);
			a = b = frontier.get(k).size()/2;
			target = null;
			
			while(true) {
				if (a > 0) {
					target = l[a];
					if (checkArea(target.getX(), target.getY()))
						break;
					a--;
				}
				
				if (b < frontier.get(k).size() - 1) {
					target = l[b];
					if (checkArea(target.getX(), target.getY()))
						break;
					b++;
				}
				
				if (a == 0 && b == frontier.get(k).size()-1) {
					target = null;
					break;
				}	
				
			}
			
			if (target != null)
				target.setState(3);
			
			if (target == null)
				continue;
			
			Cell.resetGValues();
			
			tempPath = as.findPath(map.map[i][j], target);
			
			if (tempPath == null)
				continue;
			
			tempPath.add(0, map.map[i][j]);

			System.out.println("cost to the frontier " + k + " is "	+ target.getGValue());

			if (min > target.getGValue()) {
				min2 = min;
				path2.clear();
				path2.addAll(path);
				min = target.getGValue();
				path.clear();
				path.addAll(tempPath);	
				
			}
			
		}

		if (path.get(path.size()-1).getPosition().equals(oldTarget.getPosition())) {
			path.clear();
			path.addAll(path2);
		}
		
		oldTarget = path.get(path.size()-1);
		
		System.out.println("min is " + min);
		System.out.println("min2 is " + min2);
	}

	
	private void sensorChecker() {
		Thread sensorChecker = new Thread() {
			public void run() {
				double oldError = 0;

				while (true) {
					while (!sonar.isDataReady())
						;
					sonarValues = sonar.getData().getRanges();

					for (int i = 0; i < lastValues.length; i++) {
						lastValues[i] = sonarValues[i];
					}
					
					try {
						sleep(5);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		sensorChecker.start();
	}

	private void positionChecker() {
		Thread positionChecker = new Thread() {
			public void run() {

				while (true) {
					while (!pos2D.isDataReady());

					x = pos2D.getX();
					y = pos2D.getY();
					yaw = pos2D.getYaw();

					yaw = Math.round(yaw * 100) / 100.0;

					if (yaw == 3.14) {
						heading = WEST;
					}
					if (yaw == 0) {
						heading = EAST;
					}
					if (yaw == 1.57) {
						heading = NORTH;
					}
					if (yaw == -1.57) {
						heading = SOUTH;
					}

					try {
						sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};
		positionChecker.start();
	}

	private void sensorOutput() {
		Thread sensorOutput = new Thread() {
			public void run() {

				while (true) {
					/*
					 * System.out.printf("front: %1.2f", lastValues[3]);
					 * System.out.printf(" front: %1.2f", lastValues[4]);
					 * System.out.printf(" front: %1.2f", lastValues[9]);
					 * System.out.printf(" front: %1.2f", lastValues[10]);
					 * System.out.printf(" front: %1.2f", lastValues[11]);
					 * System.out.printf(" front: %1.2f", lastValues[12]);
					 * 
					 * System.out.printf(" back: %1.2f", lastValues[0]);
					 * System.out.printf(" back: %1.2f", lastValues[1]);
					 * System.out.printf(" back: %1.2f", lastValues[2]);
					 * 
					 * System.out.printf(" left: %1.2f", lastValues[5]);
					 * System.out.printf(" left: %1.2f", lastValues[6]);
					 * System.out.printf(" left: %1.2f", lastValues[13]);
					 * 
					 * System.out.printf(" right: %1.2f", lastValues[7]);
					 * System.out.printf(" right: %1.2f", lastValues[8]);
					 * System.out.printf(" right: %1.2f", lastValues[14]);
					 * System.out.println();
					 */

					System.out.printf(" front: %1.2f", lastValues[12]);
					System.out.printf(" back: %1.2f", lastValues[1]);
					System.out.printf(" left: %1.2f", lastValues[13]);
					System.out.printf(" right: %1.2f", lastValues[14]);

					System.out.printf(" X: %1.2f", x);
					System.out.printf(" Y: %1.2f", y);
					System.out.printf(" Yaw: %1.2f", yaw);
					System.out.println();
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		sensorOutput.start();
	}

	private void map(double yaw, double value, int lasti, int lastj, int pos) {

		int sign, yN, xN;
		int l = 0, i;

		if (yaw < 3 * Math.PI / 4 && yaw >= Math.PI / 4) {
			if (yaw < Math.PI / 2)
				sign = 1;
			else
				sign = -1;

			yN = (int) Math.round(Math.cos(sign * (Math.PI / 2 - yaw)) * value);

			for (i = 0; i < yN; i++) {
				l = (int) Math.round(i * Math.tan(sign * (Math.PI / 2 - yaw)));
				
				if (!(lasti + sign * l > mapSize - 1 || lastj - i > mapSize - 1 || lastj
						- i < 0)) {

					if (map.map[lasti + sign * l][lastj - i].getState() == Map.OCCUPIED)
						break;
					map.map[lasti + sign * l][lastj - i].setState(1);
				}
			}

			l = (int) Math.round(i * Math.tan(sign * (Math.PI / 2 - yaw)));

			if (i == yN) {
				if (!(lasti + sign * l > mapSize - 1 || lastj - i > mapSize - 1 || lastj
						- i < 0)) {
					if (pos != SOUTH) {

						if (value < 100) {
							if (map.map[lasti + sign * l][lastj - i].getState() != Map.FREE) {
								map.map[lasti + sign * l][lastj - i].setState(0);
							}else {
								if (map.map[lasti + sign * l - 1][lastj - i].getState() == Map.UNKNOWN ||
										map.map[lasti + sign * l - 1][lastj - i].getState() == Map.FRONTIER)
									map.map[lasti + sign * l][lastj - i].setState(0);
								else
								if (map.map[lasti + sign * l + 1][lastj - i].getState() == Map.UNKNOWN ||
										map.map[lasti + sign * l + 1][lastj - i].getState() == Map.FRONTIER)
									map.map[lasti + sign * l][lastj - i].setState(0);
								else
								if (map.map[lasti + sign * l][lastj - i - 1].getState() == Map.UNKNOWN ||
									map.map[lasti + sign * l][lastj - i - 1].getState() == Map.FRONTIER)
									map.map[lasti + sign * l][lastj - i].setState(0);
								else
								if (map.map[lasti + sign * l][lastj - i + 1].getState() == Map.UNKNOWN ||
										map.map[lasti + sign * l][lastj - i + 1].getState() == Map.FRONTIER)
									map.map[lasti + sign * l][lastj - i].setState(0);
							}
						}
					} else {
						map.map[lasti + sign * l][lastj - i].setState(1);
					}
				}
			}
		} else if (yaw < Math.PI / 4 && yaw >= -Math.PI / 4) {
			if (yaw > 0)
				sign = 1;
			else
				sign = -1;

			xN = (int) Math.round(Math.cos(sign * yaw) * value);

			for (i = 0; i < xN; i++) {
				if (!(lasti + i > mapSize - 1 || lastj - sign * l > mapSize - 1 || lastj
						- sign * l < 0)) {
					l = (int) Math.round(i * Math.tan(sign * yaw));
					
					if (map.map[lasti + i][lastj - sign * l].getState() == Map.OCCUPIED)
						break;
					map.map[lasti + i][lastj - sign * l].setState(1);
				}
			
			}

			l = (int) Math.round(i * Math.tan(sign * yaw));

			if (i == xN) {
				if (!(lasti + i > mapSize - 1
						|| lastj - sign * l > mapSize - 1 || lastj - sign
						* l < 0)) {
					if (pos != SOUTH) {
					
						if (map.map[lasti + i][lastj - sign * l].getState() != Map.FREE) {
							map.map[lasti + i][lastj - sign * l].setState(0);
						}else {
							if (map.map[lasti + i - 1][lastj - sign * l].getState() == Map.UNKNOWN ||
									map.map[lasti + i - 1][lastj - sign * l].getState() == Map.FRONTIER) 
								map.map[lasti + i][lastj - sign * l].setState(0);
							else
							if (map.map[lasti + i + 1][lastj - sign * l].getState() == Map.UNKNOWN ||
									map.map[lasti + i + 1][lastj - sign * l].getState() == Map.FRONTIER) 
								map.map[lasti + i][lastj - sign * l].setState(0);
							else
							if (map.map[lasti + i][lastj - sign * l - 1].getState() == Map.UNKNOWN ||
									map.map[lasti + i][lastj - sign * l - 1].getState() == Map.FRONTIER) 
								map.map[lasti + i][lastj - sign * l].setState(0);	
							else
							if (map.map[lasti + i][lastj - sign * l + 1].getState() == Map.UNKNOWN ||
									map.map[lasti + i][lastj - sign * l + 1].getState() == Map.FRONTIER) 
								map.map[lasti + i][lastj - sign * l].setState(0);	
						}
					} else {
						map.map[lasti + i][lastj - sign * l].setState(1);
					}
				}
			}
		} else if (yaw < -Math.PI / 4 && yaw >= -3 * Math.PI / 4) {
			if (yaw > -Math.PI / 2)
				sign = 1;
			else
				sign = -1;

			yN = (int) Math.round(Math.cos(sign * (Math.PI / 2 + yaw)) * value);

			for (i = 0; i < yN; i++) {
				if (!(lasti + sign * l > mapSize - 1 || lastj + i > mapSize - 1)) {
					l = (int) Math.round(i
							* Math.tan(sign * (Math.PI / 2 + yaw)));
					
					if (map.map[lasti + sign * l][lastj + i].getState() == Map.OCCUPIED)
						break;
					map.map[lasti + sign * l][lastj + i].setState(1);
				}

			}

			l = (int) Math.round(i * Math.tan(sign * (Math.PI / 2 + yaw)));

			if (i == yN) {
				if (!(lasti + sign * l > mapSize - 1 || lastj + i > mapSize - 1)) {
					if (pos != SOUTH) {
					
						if (map.map[lasti + sign * l][lastj + i].getState() != Map.FREE) {
							map.map[lasti + sign * l][lastj + i].setState(0);
						}else {
							if (map.map[lasti + sign * l - 1][lastj + i].getState() == Map.UNKNOWN ||
									map.map[lasti + sign * l - 1][lastj + i].getState() == Map.FRONTIER) 
								map.map[lasti + sign * l][lastj + i].setState(0);
							else
							if (map.map[lasti + sign * l + 1][lastj + i].getState() == Map.UNKNOWN ||
									map.map[lasti + sign * l + 1][lastj + i].getState() == Map.FRONTIER) 
								map.map[lasti + sign * l][lastj + i].setState(0);	
							else
							if (map.map[lasti + sign * l][lastj + i - 1].getState() == Map.UNKNOWN ||
									map.map[lasti + sign * l][lastj + i - 1].getState() == Map.FRONTIER) 
								map.map[lasti + sign * l][lastj + i].setState(0);	
							else
							if (map.map[lasti + sign * l][lastj + i + 1].getState() == Map.UNKNOWN ||
									map.map[lasti + sign * l][lastj + i + 1].getState() == Map.FRONTIER) 
								map.map[lasti + sign * l][lastj + i].setState(0);	
						}
					} else {
						map.map[lasti + sign * l][lastj + i].setState(1);
					}
				}
			}
		} else if (yaw < -3 * Math.PI / 4 || yaw > 3 * Math.PI / 4) {
			if (yaw > 0)
				sign = -1;
			else
				sign = 1;

			xN = (int) Math.round(Math.cos(Math.PI + sign * yaw) * value);

			for (i = 0; i < xN; i++) {
				if (!(lasti - i > mapSize - 1 || lastj + sign * l > mapSize - 1 || lasti
						- i < 0)) {
					l = (int) Math.round(i * Math.tan(Math.PI + sign * yaw));
					
					if (map.map[lasti - i][lastj + sign * l].getState() == Map.OCCUPIED)
						break;
					map.map[lasti - i][lastj + sign * l].setState(1);
				}
			}

			l = (int) Math.round(i * Math.tan(Math.PI + sign * yaw));

			if (i == xN) {
				if (!(lasti - i > mapSize - 1
						|| lastj + sign * l > mapSize - 1 || lasti - i < 0)) {
					if (pos != SOUTH) {
					
						if (map.map[lasti - i][lastj + sign * l].getState() != Map.FREE) {
							map.map[lasti - i][lastj + sign * l].setState(0);
						}else {
							if (map.map[lasti - i - 1][lastj + sign * l].getState() == Map.UNKNOWN ||
									map.map[lasti - i - 1][lastj + sign * l].getState() == Map.FRONTIER) 
								map.map[lasti - i][lastj + sign * l].setState(0);
							else
							if (map.map[lasti - i + 1][lastj + sign * l].getState() == Map.UNKNOWN ||
									map.map[lasti - i + 1][lastj + sign * l].getState() == Map.FRONTIER) 
								map.map[lasti - i][lastj + sign * l].setState(0);	
							else
							if (map.map[lasti - i][lastj + sign * l - 1].getState() == Map.UNKNOWN ||
									map.map[lasti - i][lastj + sign * l - 1].getState() == Map.FRONTIER) 
								map.map[lasti - i][lastj + sign * l].setState(0);
							else
							if (map.map[lasti - i ][lastj + sign * l + 1].getState() == Map.UNKNOWN ||
									map.map[lasti - i ][lastj + sign * l + 1].getState() == Map.FRONTIER) 
								map.map[lasti - i][lastj + sign * l].setState(0);
						}
					} else {
						map.map[lasti - i][lastj + sign * l].setState(1);
					}
				}
			}
		}

	}

	/*
	 * 0 = Occupied; 1 = not occupied; 2 = unknown; 3 = robot;
	 */
	private void mapping() {

		Thread mapping = new Thread() {
			double left, right, top, down;
			int yN, xN;

			public void run() {

				int lasti, lastj;
				i = (int) Math.round(x * 10) + 60;
				j = map.map.length - ((int) Math.round(y * 10) + 60);
				lasti = i;
				lastj = j;

				System.out.println(i);
				System.out.println(j);
				map.map[i][j].setState(3);

				while (true) {
					i = (int) Math.round(x * 10) + 60;
					j = map.map.length - ((int) Math.round(y * 10) + 60);

					map.map[i][j].setState(3);
					lasti = i;
					lastj = j;

					left = sonarValues[13] * 10;
					down = sonarValues[1] * 10;
					right = sonarValues[14] * 10;
					top = sonarValues[12] * 10;

					double l, r, t, d;
					l = r = t = d = 0;

					if (!turning) {
						if (yaw > 0 && yaw <= Math.PI / 2) {
							t = yaw;
							d = -Math.PI + yaw;
							l = Math.PI / 2 + yaw;
							r = -Math.PI / 2 + yaw;
						} else if (yaw > Math.PI / 2 && yaw <= Math.PI) {
							t = yaw;
							d = yaw - Math.PI;
							l = yaw - 3 * Math.PI / 2;
							r = yaw - Math.PI / 2;
						} else if (yaw > -Math.PI && yaw <= -Math.PI / 2) {
							t = yaw;
							d = yaw + Math.PI;
							l = yaw + Math.PI / 2;
							r = yaw + 3 * Math.PI / 2;
						} else if (yaw > -Math.PI / 2 && yaw <= 0) {
							t = yaw;
							d = yaw + Math.PI;
							l = yaw + Math.PI / 2;
							r = yaw - Math.PI / 2;
						}
	
						map(t, top, lasti, lastj, NORTH);
						map(d, down, lasti, lastj, SOUTH);
						map(l, left, lasti, lastj, WEST);
						map(r, right, lasti, lastj, EAST);
	
						dm.updateMap();
					 }

					try {
						sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		mapping.start();
	}

	/**
	 * Method to cause the robot to turn in a given direction.
	 * 
	 * @param direction
	 *            the given direction.
	 */

	private void getTurningPoints() {

		turningPoints.clear();
		
		for (int i = 0; i < path.size() - 2; i++) {
			if (path.get(i).getX() != path.get(i + 2).getX()
					&& path.get(i).getY() != path.get(i + 2).getY()) {
				turningPoints.add(path.get(i + 1));
			}
		}

		for (int i = 0; i < turningPoints.size(); i++)
			System.out.println("punct de intoarcere: "
					+ turningPoints.get(i).getX() + "   "
					+ turningPoints.get(i).getY());
		
	}

	public void makeFirstRotation() {

		if (path.get(0).getX() == path.get(1).getX()) {
			if (path.get(0).getY() < path.get(1).getY()) {
				turnSouth();
			} else {
				turnNorth();
			}
		} else if (path.get(0).getY() == path.get(1).getY()) {
			if (path.get(0).getX() < path.get(1).getX()) {
				turnEast();
			} else {
				turnWest();
			}
		}
	}

	private void moveAlongPath() {
		Thread moveAlongPath = new Thread() {

			public void run() {

				while (true) {

					while (moveAlongPathFlag) {

						i = (int) Math.round(x * 10) + 60;
						j = map.map.length - ((int) Math.round(y * 10) + 60);

						if (pathFollowing) {
						
							//avoidanceFlag = true;
							if (i == path.get(path.size() - 1).getX()
									&& j == path.get(path.size() - 1).getY()) {
								stop1();
								System.out.println("path traveled");
								System.out.println("robotX:  " + i
										+ "    robotY    " + j);
								pathFollowing = false;
							} else {
								if (turningPoints.size() > 0) {
									if (turningPoints.get(0).getX() == i
											&& turningPoints.get(0).getY() == j) {

										Point p = path.get(
												path.indexOf(turningPoints
														.get(0)) + 1)
												.getPosition();
										stop1();
										turning = true;
										
										if (i > p.x)
											turnWest();
										else if (i < p.x)
											turnEast();
										else if (j > p.y)
											turnNorth();
										else if (j < p.y)
											turnSouth();

										System.out.println("am intors la:  "
												+ turningPoints.get(0).getX()
												+ "   "
												+ turningPoints.get(0).getY());
										turning = false;
										turningPoints.remove(0);

									}
								}

								if (turningPoints.size() > 0) {
									if (Math.abs(i
											- turningPoints.get(0).getX()) > 1
											|| Math.abs(j
													- turningPoints.get(0)
															.getY()) > 1) {
										
										if (!turnBack)
											forward();
										else
											backward();
									} else {
										if (!turnBack)
											slowForward();
										else
											slowBackward();
										
									}
								} else {
									if (Math.abs(i
											- path.get(path.size() - 1).getX()) > 1
											|| Math.abs(j
													- path.get(path.size() - 1)
															.getY()) > 1) {
										if (!turnBack)
											forward();
										else
											backward();
									} else {
										if (!turnBack)
											slowForward();
										else
											slowBackward();
									}
								}
							}
						} else {
							stop1();
							System.out.println("again");
							
							if (c)
								turn360(0.15);
							else if (!turnBack)
								turn360(0.15);
							
							
							c = false;
							turnBack = false;
							
							// Storage1.saveMap("name.txt", map);

							getFrontier();
							separateFrontier();

							System.out.println("there are " + frontier.size()
									+ " frontiers");

							

							if (frontier.size() == 0) {
								System.out.println("Mapping done!!!!");
								break;
							}

							getNextTarget();

							System.out.println("TARGET is: "
									+ path.get(path.size() - 1).getX() + "   "
									+ path.get(path.size() - 1).getY());
							getTurningPoints();
							System.out.println("there are "
									+ turningPoints.size() + " turning points");
							turning = true;
							makeFirstRotation();
							turning = false;
							pathFollowing = true;

						}

						try {
							sleep(1);
						} catch (InterruptedException e) {
						}
					}
					
					try {
						sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		moveAlongPath.start();
	}

	private void getFrontierAndGo() {
		i = (int) Math.round(x * 10) + 60;
		j = map.map.length - ((int) Math.round(y * 10) + 60);
		getFrontier();
		separateFrontier();
		System.out.println("there are " + frontier.size() + " frontiers");


		getNextTarget();

		System.out.println("TARGET is: " + path.get(path.size() - 1).getX()
				+ "   " + path.get(path.size() - 1).getY());
		getTurningPoints();
		System.out.println("there are " + turningPoints.size()
				+ " turning points");
		turning = true;
		makeFirstRotation();
		turning = false;
		pathFollowing = true;
		moveAlongPathFlag = true;

	}
	
	public void renewPath() {
		as = new AStar1(map);
		Cell target = new Cell();
		
		if (checkArea(map.map[i][j].getX(), map.map[i][j].getY())) {
			
			Cell.resetGValues();	
			target = path.get(path.size()-1);
			path.clear();
			path = as.findPath(map.map[i][j], target);
			path.add(0, map.map[i][j]);
			oldTarget = target;
			
			getTurningPoints();
			turning = true;
			//turning360 = true;
			makeFirstRotation();
			//turning360 = false;
			turning = false;
			pathFollowing = true;
			moveAlongPathFlag = true;

		}else {
			//TO DO: find a valid position to run AStar
		}
	}
	
	public void checkTurningCollision() {
		Thread collision = new Thread() {
			public void run() {
				double[] temp = new double[15];
				int i;
				double oldYaw = yaw;
				while (true) {
					
					if(turning) {
						
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("oldYaw:yaw    "+oldYaw+":"+yaw);
						if (Math.abs(oldYaw - yaw) <= 0.01)
							collisionFlag = true;
						else
							collisionFlag = false;
						
						
						oldYaw = yaw;
						/*for (i=0;i<15;i++) {
							if (Math.abs(temp[i] - sonarValues[i]) > 0.01){
								collisionFlag = false;
								break;
							}
						}
						
						if (i == 15)
							collisionFlag = true;
						
						if (!collisionFlag) {
							for (i=0;i<15;i++) {
								temp[i] = sonarValues[i];
							}
						}*/
					}else {
						oldYaw = yaw;
						collisionFlag = false;
					}
					
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		collision.start();
	}
	
	public void avoidance() {
		Thread avoidance = new Thread() {
			
			public void run() {
				double back;
				double front, oldFront;
				int xDist, yDist;
				
				while (true) {
					
					while (avoidanceFlag) {
						i = (int) Math.round(x * 10) + 60;
						j = map.map.length - ((int) Math.round(y * 10) + 60);
						
						
						if (!turning) {
							if (path.size() > 0) {
								if (turningPoints.size() == 0) {
									xDist = Math.abs(i - path.get(path.size()-1).getX());
									yDist = Math.abs(j - path.get(path.size()-1).getY());
								}else {
									xDist = Math.abs(i - turningPoints.get(0).getX());
									yDist = Math.abs(j - turningPoints.get(0).getY());
								}
									
								int temp = 0;
									
								if (xDist != 0) {
									temp = xDist;
								}else if (yDist != 0) {
									temp = yDist;
								}	
									
								int a = (int) Math.round(sonarValues[12] * 10);
										
								if (temp - a > 2) {
									moveAlongPathFlag = false;
									oldFront = a;
									stop1();
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
												
									front = (int) Math.round(sonarValues[12] * 10);
												
									if (front > 2 + oldFront) { 
										pathFollowing = true;
										moveAlongPathFlag = true;
										//avoidanceFlag = false;
											
										//b = false;
									}else if (front - oldFront <= 2 || oldFront - front <= 2) {
										
										if (sonarValues[13] < sonarValues[14]) {
											turning = true;
												
											if (heading == NORTH)
												turnEast();
											else if (heading == SOUTH)
												turnWest();
											else if (heading == WEST)
												turnNorth();
											else if (heading == EAST)
												turnSouth();
												
											turning = false;
													
										}else {
											turning = true;
												
											if (heading == NORTH)
												turnWest();
											else if (heading == SOUTH)
												turnEast();
											else if (heading == WEST)
												turnSouth();
											else if (heading == EAST)
												turnNorth();
												
											turning = false;
										}
												
											
										forward();
											
										try {
											sleep(2000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
											
										turnBack = false;
										renewPath();
										//getFrontierAndGo();
										//pathFollowing = false;
										//moveAlongPathFlag = true;
											
									}
								}
							}
							
							if (sonarValues[9] < 0.15 || sonarValues[10] < 0.15 || sonarValues[11] < 0.15 || 
								sonarValues[8] < 0.15 || sonarValues[7] < 0.15 || sonarValues[6] < 0.15 ||
								sonarValues[5] < 0.15) {
								stop1();
								moveAlongPathFlag = false;
								turnBack = true;
								pathFollowing = true;
								back = sonarValues[1] * 10;
								
								if (back > 5) {
									path.clear();
									path.add(map.map[i][j]);
									
									switch (heading) {
										case NORTH:
											path.add(map.map[i][j+1]);
											path.add(map.map[i][j+2]);
											//path.add(map.map[i][j+3]);
											break;
										case SOUTH:
											path.add(map.map[i][j-1]);
											path.add(map.map[i][j-2]);
											//path.add(map.map[i][j-3]);
											break;
										case WEST:
											path.add(map.map[i+1][j]);
											path.add(map.map[i+2][j]);
											//path.add(map.map[i+3][j]);
											break;	
										case EAST:
											path.add(map.map[i-1][j]);
											path.add(map.map[i-2][j]);
											//path.add(map.map[i-3][j]);
											break;	
										default:
											break;
									}
									
									if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
										Math.abs(j - path.get(path.size() - 1).getY()) < 5)
										c = true;
										
								}
								moveAlongPathFlag = true;
							}else if (sonarValues[0] < 0.05 || sonarValues[1] < 0.05 || sonarValues[2] < 0.05) {
								stop1();
								moveAlongPathFlag = false;
								turnBack = false;
								pathFollowing = true;
								front = sonarValues[12] * 10;
								
								if (front > 5) {
									path.clear();
									path.add(map.map[i][j]);
									
									switch (heading) {
										case NORTH:
											path.add(map.map[i][j-1]);
											path.add(map.map[i][j-2]);
											//path.add(map.map[i][j+3]);
											break;
										case SOUTH:
											path.add(map.map[i][j+1]);
											path.add(map.map[i][j+2]);
											//path.add(map.map[i][j-3]);
											break;
										case WEST:
											path.add(map.map[i-1][j]);
											path.add(map.map[i-2][j]);
											//path.add(map.map[i+3][j]);
											break;	
										case EAST:
											path.add(map.map[i+1][j]);
											path.add(map.map[i+2][j]);
											//path.add(map.map[i-3][j]);
											break;	
										default:
											break;
									}
									
									if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
										Math.abs(j - path.get(path.size() - 1).getY()) < 5)
										c = true;
										
								}
								moveAlongPathFlag = true;
								
							}
						}else {
							//System.out.println("se roteste");
							if (collisionFlag) {
								//for (double a : sonarValues) {
									//System.out.println(a);
								//}
								System.out.println("COLIZIUNE!!!");
								//front collision
								if ((sonarValues[4] < sonarValues[0] &&
									 sonarValues[4] < sonarValues[1] &&
									 sonarValues[4] < sonarValues[2] &&
									 sonarValues[4] < sonarValues[5] &&
									 sonarValues[4] < sonarValues[6] &&
									 sonarValues[4] < sonarValues[7] &&
									 sonarValues[4] < sonarValues[8]) || 
									(sonarValues[3] < sonarValues[0] &&
									 sonarValues[3] < sonarValues[1] &&
									 sonarValues[3] < sonarValues[2] &&
									 sonarValues[3] < sonarValues[5] &&
									 sonarValues[3] < sonarValues[6] &&
									 sonarValues[3] < sonarValues[7] &&
									 sonarValues[3] < sonarValues[8])
								    ) {
									moveAlongPathFlag = false;
									turningFlag = false;
									turning = false;
									pathFollowing = false;
									turnBack = false;
									collisionFlag = false;
									stop1();
									System.out.println("s-a produs coliziune in fata");
									pos2D.setSpeed(-0.3, 0);
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									
									if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
										Math.abs(j - path.get(path.size() - 1).getY()) < 5)
											c = true;
									
									moveAlongPathFlag = true;
								}else if ((sonarValues[5] < sonarValues[0] &&
										   sonarValues[5] < sonarValues[1] &&
										   sonarValues[5] < sonarValues[2] &&
										   sonarValues[5] < sonarValues[3] &&
										   sonarValues[5] < sonarValues[4] &&
										   sonarValues[5] < sonarValues[7] &&
										   sonarValues[5] < sonarValues[8]) || 
										  (sonarValues[6] < sonarValues[0] &&
										   sonarValues[6] < sonarValues[1] &&
										   sonarValues[6] < sonarValues[2] &&
										   sonarValues[6] < sonarValues[3] &&
										   sonarValues[6] < sonarValues[4] &&
										   sonarValues[6] < sonarValues[7] &&
										   sonarValues[6] < sonarValues[8])
									      ) {
											moveAlongPathFlag = false;
											turningFlag = false;
											turning = false;
											pathFollowing = false;
											turnBack = false;
											collisionFlag = false;
											stop1();
											System.out.println("s-a produs coliziune in stanga");
											
											
											turning = true;
											pos2D.setSpeed(0, 0.3);
											try {
												sleep(1000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											stop1();
											turning = false;
											
											//move front
											if (sonarValues[12] > sonarValues[1]) {
												
												pos2D.setSpeed(0.3, 0);
												try {
													sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}else {
												//move back	
												pos2D.setSpeed(-0.3, 0);
												try {
													sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											
											
											if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
												Math.abs(j - path.get(path.size() - 1).getY()) < 5)
													c = true;
											
											moveAlongPathFlag = true;
								}else if ((sonarValues[7] < sonarValues[0] &&
										   sonarValues[7] < sonarValues[1] &&
										   sonarValues[7] < sonarValues[2] &&
										   sonarValues[7] < sonarValues[3] &&
										   sonarValues[7] < sonarValues[4] &&
										   sonarValues[7] < sonarValues[5] &&
										   sonarValues[7] < sonarValues[6]) || 
										  (sonarValues[8] < sonarValues[0] &&
										   sonarValues[8] < sonarValues[1] &&
										   sonarValues[8] < sonarValues[2] &&
										   sonarValues[8] < sonarValues[3] &&
										   sonarValues[8] < sonarValues[4] &&
										   sonarValues[8] < sonarValues[5] &&
										   sonarValues[8] < sonarValues[6])
									      ) {
											moveAlongPathFlag = false;
											turningFlag = false;
											turning = false;
											pathFollowing = false;
											turnBack = false;
											collisionFlag = false;
											stop1();
											System.out.println("s-a produs coliziune in dreapta");
											
											//turningFlag = true;
											turning = true;
											pos2D.setSpeed(0, -0.3);
											try {
												sleep(1000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											stop1();
											turning = false;
											//turningFlag = false;
											
											//move front
											if (sonarValues[12] > sonarValues[1]) {
												System.out.println("ma duc in fata");
												pos2D.setSpeed(0.3, 0);
												try {
													sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}else {
												//move back	
												System.out.println("ma duc in spate");
												pos2D.setSpeed(-0.3, 0);
												try {
													sleep(1000);
												} catch (InterruptedException e) {
													e.printStackTrace();
												}
											}
											stop1();
											
											if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
												Math.abs(j - path.get(path.size() - 1).getY()) < 5)
													c = true;
											
											moveAlongPathFlag = true;
								} else if ((sonarValues[0] < sonarValues[6] &&
										   sonarValues[0] < sonarValues[7] &&
										   sonarValues[0] < sonarValues[8] &&
										   sonarValues[0] < sonarValues[3] &&
										   sonarValues[0] < sonarValues[4] &&
										   sonarValues[0] < sonarValues[5]) || 
										  (sonarValues[1] < sonarValues[6] &&
										   sonarValues[1] < sonarValues[7] &&
										   sonarValues[1] < sonarValues[8] &&
										   sonarValues[1] < sonarValues[3] &&
										   sonarValues[1] < sonarValues[4] &&
										   sonarValues[1] < sonarValues[5]) ||
										  (sonarValues[2] < sonarValues[6] &&
										   sonarValues[2] < sonarValues[7] &&
										   sonarValues[2] < sonarValues[8] &&
										   sonarValues[2] < sonarValues[3] &&
										   sonarValues[2] < sonarValues[4] &&
										   sonarValues[2] < sonarValues[5])
									      ) {
											moveAlongPathFlag = false;
											turningFlag = false;
											turning = false;
											pathFollowing = false;
											turnBack = false;
											collisionFlag = false;
											stop1();
											System.out.println("s-a produs coliziune in spate");
											
											System.out.println("ma duc in fata");
											//move front
											pos2D.setSpeed(0.3, 0);
											try {
												sleep(1000);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											
											if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
												Math.abs(j - path.get(path.size() - 1).getY()) < 5)
													c = true;
											
											moveAlongPathFlag = true;
								}
							}
							
							/*System.out.println("am detectat coliziune: "+collisionFlag);
							//System.out.println("valorile de la spate 0 1 2 sunt: "+sonarValues[0]+"  "+sonarValues[1]+"  "+sonarValues[2]);
							if (sonarValues[3] < 0.05 || sonarValues[4] < 0.05) {
								moveAlongPathFlag = false;
								stop1();
								System.out.println("intru in caz de recovery");
								back = sonarValues[1] * 10;
								if (back > 5) {
									System.out.println("recovery");
									pos2D.setSpeed(-0.3, 0);
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}else { 
									int direction = 0;
									
									if (sonarValues[6] < sonarValues[8] || sonarValues[5] < sonarValues[7]) 
										direction = 1;
									else
										direction = -1;
										
									back = sonarValues[1] * 10;
									
									turning = true;
									while (back < 5) {
										pos2D.setSpeed(0, direction*0.1);
										back = sonarValues[1] * 10;
											
										try {
											sleep(10);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									turning = false;
									
									pos2D.setSpeed(-0.3, 0);
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									stop1();
									
								}
								
								if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
										Math.abs(j - path.get(path.size() - 1).getY()) < 5)
										c = true;
								
								pathFollowing = false;
								moveAlongPathFlag = true;
								turnBack = false;
							}else if (sonarValues[0] < 0.05 || sonarValues[1] < 0.05 || sonarValues[2] < 0.05) {
								moveAlongPathFlag = false;
								stop1();
								
								front = sonarValues[12] * 10;
								if (front > 5) {
									System.out.println("recovery");
									pos2D.setSpeed(0.3, 0);
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}else { 
									int direction = 0;
									
									if (sonarValues[6] < sonarValues[8] || sonarValues[5] < sonarValues[7]) 
										direction = 1;
									else
										direction = -1;
										
									front = sonarValues[12] * 10;
									
									turning = true;
									while (front < 5) {
										pos2D.setSpeed(0, direction*0.1);
										front = sonarValues[12] * 10;
											
										try {
											sleep(10);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									turning = false;
									
									pos2D.setSpeed(0.3, 0);
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									stop1();
									
								}
								
								if (Math.abs(i - path.get(path.size() - 1).getX()) < 5 &&
										Math.abs(j - path.get(path.size() - 1).getY()) < 5)
										c = true;
								
								pathFollowing = false;
								moveAlongPathFlag = true;
								turnBack = false;
							}*/
						}

						
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		avoidance.start();
	}

	/*public void avoidance() {

		Thread avoidance = new Thread() {
			public void run() {

				while (true) {

					while (avoidanceFlag) {

						if (sonarValues[9] < 0.15 || sonarValues[10] < 0.15
								|| sonarValues[11] < 0.15) {
							
							System.out.println("Avoidance1 fata");
							moveAlongPathFlag = false;
							turningFlag = false;
							stop1();
							if (path != null) {
								if (Math.abs(i
										- path.get(path.size() - 1).getX()) < 5
										&& Math.abs(path.get(path.size() - 1)
												.getY()) < 5) {

									pathFollowing = false;
									pos2D.setSpeed(-0.2, 0);
									try {
										sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									stop1();
									getFrontierAndGo();
								}else {
									pos2D.setSpeed(-0.2, 0);

									try {
										sleep(500);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									stop1();
									getFrontierAndGo();
								}
								
							} else {
								pos2D.setSpeed(-0.2, 0);

								try {
									sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								stop1();
								getFrontierAndGo();
							}

						}

						else if (sonarValues[0] < 0.05 || sonarValues[1] < 0.05
								|| sonarValues[2] < 0.05) {
							
							System.out.println("Avoidance2 spate");
							moveAlongPathFlag = false;
							turningFlag = false;
							stop1();
							pos2D.setSpeed(0.1, 0);
							try {
								sleep(400);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							stop1();
							getFrontierAndGo();
						}

						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		avoidance.start();
	}*/

	public void recovery() {

		Thread recovery = new Thread() {
			public void run() {

				while (true) {

					while (recoveryFlag) {

						if (turningFlag) {

							if (sonarValues[4] < 0.03 || sonarValues[3] < 0.03
									|| (sonarValues[5] < 0.03)
									|| (sonarValues[6] < 0.03)
									|| (sonarValues[7] < 0.03)
									|| (sonarValues[8] < 0.03)
									|| (sonarValues[0] < 0.03)
									|| (sonarValues[1] < 0.03)
									|| (sonarValues[2] < 0.03)) {
								System.out.println("Recovery fata");
								moveAlongPathFlag = false;
								pathFollowing = false;
								avoidanceFlag = false;
								turningFlag = false;

								pos2D.setSpeed(0, (0 - direction) * 0.2);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								pos2D.setSpeed(-0.1, 0);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								avoidanceFlag = true;
								getFrontierAndGo();

							}

							/*
							 * if (sonarValues[4] < 0.02 || sonarValues[3] <
							 * 0.02 || (sonarValues[5] < 0.02 && sonarValues[0]
							 * > 0.5 && sonarValues[1] > 0.5 && sonarValues[2] >
							 * 0.5) || (sonarValues[6] < 0.02 && sonarValues[0]
							 * > 0.5 && sonarValues[1] > 0.5 && sonarValues[2] >
							 * 0.5) || (sonarValues[7] < 0.02 && sonarValues[0]
							 * > 0.5 && sonarValues[1] > 0.5 && sonarValues[2] >
							 * 0.5) || (sonarValues[8] < 0.02 && sonarValues[0]
							 * > 0.5 && sonarValues[1] > 0.5 && sonarValues[2] >
							 * 0.5)) {
							 * System.out.println("Start recover from crash");
							 * moveAlongPathFlag = false; avoidanceFlag = false;
							 * turningFlag = false; pos2D.setSpeed(-0.2, 0); try
							 * { sleep(500); } catch (InterruptedException e) {
							 * // TODO Auto-generated catch block
							 * e.printStackTrace(); } stop1(); avoidanceFlag =
							 * true; pathFollowing = false; moveAlongPathFlag =
							 * true;
							 * 
							 * System.out.println("recovery done");
							 * 
							 * } else if (sonarValues[0] < 0.02 ||
							 * sonarValues[1] < 0.02 || sonarValues[2] < 0.02 ||
							 * (sonarValues[5] < 0.02 && sonarValues[3] > 0.5 &&
							 * sonarValues[4] > 0.5 && sonarValues[9] > 0.5) ||
							 * (sonarValues[6] < 0.02 && sonarValues[3] > 0.5 &&
							 * sonarValues[4] > 0.5 && sonarValues[9] > 0.5) ||
							 * (sonarValues[7] < 0.02 && sonarValues[3] > 0.5 &&
							 * sonarValues[4] > 0.5 && sonarValues[9] > 0.5) ||
							 * (sonarValues[8] < 0.02 && sonarValues[3] > 0.5 &&
							 * sonarValues[4] > 0.5 && sonarValues[9] > 0.5)) {
							 * 
							 * System.out.println("Start recover from crash");
							 * moveAlongPathFlag = false; avoidanceFlag = false;
							 * turningFlag = false; pos2D.setSpeed(0.2, 0);
							 * 
							 * try { sleep(500); } catch (InterruptedException
							 * e) {
							 * 
							 * e.printStackTrace(); } stop1(); avoidanceFlag =
							 * true; pathFollowing = false; moveAlongPathFlag =
							 * true; System.out.println("recovery done");
							 */

						} else {
							if (sonarValues[7] < 0.15 || sonarValues[8] < 0.15) {
								moveAlongPathFlag = false;
								pathFollowing = false;
								avoidanceFlag = false;
								turningFlag = false;
								System.out.println("Recovery dreapta");
								pos2D.setSpeed(0.1, 0);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								turnNorth();
								pos2D.setSpeed(0.3, 0);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								avoidanceFlag = true;
								getFrontierAndGo();

							} else if (sonarValues[5] < 0.15
									|| sonarValues[6] < 0.15) {
								moveAlongPathFlag = false;
								pathFollowing = false;
								avoidanceFlag = false;
								turningFlag = false;
								System.out.println("Recovery stanga");
								
								pos2D.setSpeed(-0.3, 0);
								try {
									sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								turnSouth();
								pos2D.setSpeed(0.3, 0);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								avoidanceFlag = true;
								getFrontierAndGo();
							}
						}

						try {
							sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};
		recovery.start();
	}

	public void turn(int angle, double speed) {

		double targetYaw = cauculateYaw(angle);
		turn(targetYaw, speed);

	}

	public void turn(double target, double speed) {

		turningFlag = true;
		long targetYaw = roundedYaw(target);

		System.out.println(targetYaw + "");

		while (!pos2D.isDataReady()) {
		}
		long position = roundedYaw(pos2D.getYaw());

		direction = position - targetYaw > 0 ? -1 : 1;
		long lastDifference;
		long pi = roundedYaw(Math.PI);
		long temp = 0;

		long difference = Math.abs(roundedYaw(pos2D.getYaw()) - targetYaw);
		// correctDirection = (pos2D.getYaw() - targetYaw) > 0 ? true : false;

		if (position < -(pi - roundedYaw(0.3)) / 2 && targetYaw > pi / 2) {
			direction = -1;

			// System.out.println("Turn right");
			while (difference > 0 && turningFlag) {

				if (pos2D.isDataReady()) {
					lastDifference = difference;
					position = roundedYaw(pos2D.getYaw());
					difference = Math.abs(position - targetYaw);

					if (position < -(pi - roundedYaw(0.3)) / 2) {
						direction = -1;

						temp = Math.abs(position + pi) + 1;

						if (temp < PRECISION / 100) {
							pos2D.setSpeed(0, direction * temp / PRECISION / 2);
						}

						else if (temp < PRECISION / 5) {
							pos2D.setSpeed(0, direction * temp / PRECISION);
						} else {
							pos2D.setSpeed(0, direction * speed);
						}

					}

					else if (position > pi / 2) {

						if (lastDifference < difference) {
							direction = 0 - direction;
						}

						if (difference < PRECISION / 100) {
							pos2D.setSpeed(0, direction * difference
									/ PRECISION / 2);
						}

						else if (difference < PRECISION / 5) {
							pos2D.setSpeed(0, direction * difference
									/ PRECISION);
						} else {
							pos2D.setSpeed(0, direction * speed);
						}

					}

					difference = Math.abs(roundedYaw(pos2D.getYaw())
							- targetYaw);
				}
			}
			pos2D.setSpeed(0, 0);
			return;

		}

		else if (position > (pi - roundedYaw(0.3)) / 2 && targetYaw < -pi / 2) {

			// System.out.println("Turn left");
			while (difference > 0 && turningFlag) {

				if (pos2D.isDataReady()) {
					lastDifference = difference;
					position = roundedYaw(pos2D.getYaw());
					difference = Math.abs(position - targetYaw);

					if (position > pi - roundedYaw(0.3) / 2) {
						direction = 1;

						temp = Math.abs(position - pi);

						if (temp < PRECISION / 100) {
							pos2D.setSpeed(0, direction * temp / PRECISION / 2);
						}

						else if (temp < PRECISION / 5) {
							pos2D.setSpeed(0, direction * temp / PRECISION);
						} else {
							pos2D.setSpeed(0, direction * speed);
						}

					}

					else if (position < -pi / 2) {

						if (lastDifference < difference) {
							direction = 0 - direction;
						}

						if (difference < PRECISION / 100) {
							pos2D.setSpeed(0, direction * difference
									/ PRECISION / 2);
						}

						else if (difference < PRECISION / 5) {
							pos2D.setSpeed(0, direction * difference
									/ PRECISION);
						} else {
							pos2D.setSpeed(0, direction * speed);
						}

					}

					difference = Math.abs(roundedYaw(pos2D.getYaw())
							- targetYaw);
				}
			}
			pos2D.setSpeed(0, 0);
			return;

		}

		while (difference > 0 && turningFlag == true) {
			// System.out.println(direction+ "   " + difference );
			if (pos2D.isDataReady()) {
				lastDifference = difference;
				difference = Math.abs(roundedYaw(pos2D.getYaw()) - targetYaw);

				if (lastDifference < difference) {
					direction = 0 - direction;
				}
				if (difference >= PRECISION / 10) {
					pos2D.setSpeed(0, direction * speed);
				}

				else if (difference < PRECISION / 100) {
					pos2D.setSpeed(0, direction * difference / PRECISION / 2);
				}

				else if (difference < PRECISION / 5) {
					pos2D.setSpeed(0, direction * difference / PRECISION);
				}

				difference = Math.abs(roundedYaw(pos2D.getYaw()) - targetYaw);
			}

		}

		pos2D.setSpeed(0, 0);
		turningFlag = false;

	}

	private void turn360(final double speed) {
		turningFlag = true;
		direction = 1;

		while (!pos2D.isDataReady())
			;
		double target = pos2D.getYaw();
		Thread turn360 = new Thread() {

			public void run() {
				while (!pos2D.isDataReady())
					;
				long targetYaw = roundedYaw(pos2D.getYaw());

				pos2D.setSpeed(0, direction * speed);
				int count = 0;
				while (turningFlag && count < 10) {
					count++;
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while (!pos2D.isDataReady()) {
				}

				long difference = Math.abs(roundedYaw(pos2D.getYaw())
						- targetYaw);

				while (difference > 100 && turningFlag) {

					while (!pos2D.isDataReady()) {

					}

					difference = Math.abs(roundedYaw(pos2D.getYaw())
							- targetYaw);

					try {
						sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		};
		turn360.start();
		try {
			turn360.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("difference < 100");
		turn(target, speed);

	}

	private double cauculateYaw(int angle) {
		double targetYaw = 0;
		double a = Math.PI / 180;
		while (!pos2D.isDataReady())
			;

		double lastangle = pos2D.getYaw();
		if (angle < 0) {
			targetYaw = lastangle - a * angle;
		}
		if (angle > 0) {
			targetYaw = lastangle - a * angle;
		}

		if (targetYaw > Math.PI) {
			targetYaw = -Math.PI + (targetYaw - Math.PI);
		}
		if (targetYaw < -Math.PI) {
			targetYaw = Math.PI + (targetYaw + Math.PI);
		}

		return targetYaw;
	}

	private long roundedYaw(double yaw) {
		return (Math.round(yaw * PRECISION));
	}

	public void slowForward() {
		pos2D.setSpeed(0.05, 0);
	}
	
	public void slowBackward() {
		pos2D.setSpeed(-0.05, 0);
	}

	public void forward() {
		pos2D.setSpeed(0.3, 0);
	}

	public void backward() {
		pos2D.setSpeed(-0.3, 0);
	}

	public void turnLeft() {
		pos2D.setSpeed(0, 0.2);
	}

	public void turnRight() {
		pos2D.setSpeed(0, -0.2);
	}

	public void stop1() {
		pos2D.setSpeed(0, 0);
	}

	public void turnNorth() {
		turn(Math.PI / 2, 0.3);
	}

	public void turnSouth() {
		turn(-Math.PI / 2, 0.3);
	}

	public void turnEast() {
		turn(0, 0.3);
	}

	public void turnWest() {
		turn(Math.PI, 0.3);
	}

	public static void main(String args[]) {

		VacuumBot a = new VacuumBot();
		// new controlPanel(vacuumBot);
		visualmap = a.new VisualMap();
		// Screenshot s = new Screenshot("a", "b", drawMap.frame);
		// s.savePicture("aaaaaaa");
	}

	class VisualMap extends JFrame {

		JTextArea jta;

		public VisualMap() {
			Container main = getContentPane();
			jta = new JTextArea();
			jta.setEditable(false);
			main.setLayout(new BorderLayout());
			main.add(jta, BorderLayout.CENTER);

			for (int i = 0; i < map.map.length; i++) {
				for (int j = 0; j < map.map[i].length; j++) {
					jta.append("" + map.map[j][i].getState());
				}
				jta.append("\n");
			}

			pack();
			// setVisible(true);
		}

		public void update() {
			jta.setText("");
			for (int i = 0; i < map.map.length; i++) {
				for (int j = 0; j < map.map[i].length; j++) {
					jta.append("" + map.map[j][i].getState());
				}
				jta.append("\n");

			}
			jta.repaint();
		}

		/*
		 * JLabel [][]labels; public VisualMap() {
		 * 
		 * Container main = getContentPane(); main.setLayout(new GridLayout(50,
		 * 50));
		 * 
		 * labels = new JLabel[110][130]; for(int i =0;i<labels.length;i++){
		 * for(int j=0;j<labels[i].length;j++){ labels[i][j] = new JLabel("2");
		 * } }
		 * 
		 * for (int i = 0; i < map.length; i++) { for (int j = 0; j <
		 * map[i].length; j++) {
		 * 
		 * main.add(labels[i][j]); } }
		 * 
		 * pack(); setVisible(true); }
		 * 
		 * public void changeMap(int x,int y,int value){
		 * 
		 * labels[x][y].setText(""+value); labels[x][y].repaint();
		 * 
		 * 
		 * }
		 */
	}

	private void setCurrentPosttion(int i, int j) {
		map.map[i][j].setState(3);
		for (int k = 1; k <= 2; k++) {
			map.map[i - k][j].setState(3);
			map.map[i - k][j + k].setState(3);
			map.map[i - k][j - k].setState(3);
			map.map[i + k][j].setState(3);
			map.map[i + k][j + k].setState(3);
			map.map[i + k][j - k].setState(3);
			map.map[i][j + k].setState(3);
			map.map[i][j - k].setState(3);
		}
		map.map[i + 2][j - 1].setState(3);
		map.map[i + 2][j + 1].setState(3);
		map.map[i + 1][j + 2].setState(3);
		map.map[i - 1][j + 2].setState(3);
		map.map[i - 2][j - 1].setState(3);
		map.map[i - 2][j + 1].setState(3);
		map.map[i + 1][j - 2].setState(3);
		map.map[i - 1][j - 2].setState(3);

		// north
		if (yaw > 0.78 && yaw < 2.33) {
			map.map[i + 2][j - 1].setState(1);
			map.map[i + 2][j].setState(1);
			map.map[i + 2][j + 1].setState(1);
			map.map[i + 2][j - 2].setState(1);
			map.map[i - 2][j - 1].setState(1);
			map.map[i - 2][j].setState(1);
			map.map[i - 2][j + 1].setState(1);
			map.map[i - 2][j - 2].setState(1);
			map.map[i + 1][j - 2].setState(1);
			map.map[i - 1][j - 2].setState(1);
		}
		// south
		else if (yaw < -0.78 && yaw > -2.33) {
			map.map[i + 2][j - 1].setState(1);
			map.map[i + 2][j].setState(1);
			map.map[i + 2][j + 1].setState(1);
			map.map[i + 2][j + 2].setState(1);
			map.map[i - 2][j - 1].setState(1);
			map.map[i - 2][j].setState(1);
			map.map[i - 2][j + 1].setState(1);
			map.map[i - 2][j + 2].setState(1);
			map.map[i + 1][j + 2].setState(1);
			map.map[i - 1][j + 2].setState(1);
		}
		// east
		else if (yaw > -0.78 && yaw < 0.78) {
			map.map[i + 2][j + 2].setState(1);
			map.map[i - 1][j + 2].setState(1);
			map.map[i][j + 2].setState(1);
			map.map[i + 1][j + 2].setState(1);
			map.map[i + 2][j - 2].setState(1);
			map.map[i - 1][j - 2].setState(1);
			map.map[i][j - 2].setState(1);
			map.map[i + 1][j - 2].setState(1);
			map.map[i + 2][j - 1].setState(1);
			map.map[i + 2][j + 1].setState(1);
		}
		// west
		else if (yaw > 2.33 || yaw < -2.33) {
			map.map[i - 2][j + 2].setState(1);
			map.map[i - 1][j + 2].setState(1);
			map.map[i][j + 2].setState(1);
			map.map[i + 1][j + 2].setState(1);
			map.map[i - 2][j - 2].setState(1);
			map.map[i - 1][j - 2].setState(1);
			map.map[i][j - 2].setState(1);
			map.map[i + 1][j - 2].setState(1);
			map.map[i - 2][j - 1].setState(1);
			map.map[i - 2][j + 1].setState(1);
		}

	}

	private void clearLastPosttion(int i, int j) {
		map.map[i][j].setState(3);
		for (int k = 1; k <= 2; k++) {
			map.map[i - k][j].setState(1);
			map.map[i - k][j + k].setState(1);
			map.map[i - k][j - k].setState(1);
			map.map[i + k][j].setState(1);
			map.map[i + k][j + k].setState(1);
			map.map[i + k][j - k].setState(1);
			map.map[i][j + k].setState(1);
			map.map[i][j - k].setState(1);
		}
	}
}
