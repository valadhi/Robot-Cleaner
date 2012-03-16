

import java.awt.Point;
import java.io.*;
import java.util.Vector;

public class Cell implements Comparable<Cell> {
	
	public static Vector cells = new Vector();
	private static Cell start;
	private static Cell finish;
	private Cell parent;
	public static int cost = 1;
	private Point position;
	private int state;
	private int gValue = -1;
	
	public Cell() {
		cells.add(this);
	}
	
	public static Cell getStartCell() {
		return start;
	}
	
	public static Cell getFinishCell() {
		return finish;
	}
	
	public static void setStartCell(Cell p){
		if (p.state != Map.OCCUPIED)
			start = p;
	}
	
	public static void setFinishCell(Cell p) {
		if (p.state != Map.OCCUPIED)
			finish = p;
	}
	
	public void setCost(int c) {
		cost = c;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void setParent(Cell p) {
		parent = p;
	}
	
	public Cell getParent() {
		return parent;
	}
	
	public void setPosition(Point p) {
		position = p;
	}
	public int getX(){
		return (int)position.getX();
	}
	public int getY(){
		return (int)position.getY();
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setState(int s) {
		state = s;
	}
	
	public int getState() {
		return state;
	}
	
	public int getGValue() {
		if (Cell.start == this)
			return 0;
		return gValue;
	}
	
	public void setGValue(int x) {
		
		if (x == -1) {
			gValue = -1;
			return;
		}
		
		if(gValue == -1){
            gValue = x + cost;
            return;
        }
		
        if(x + cost < gValue){
            gValue = x + cost;
        }
	}
	
	public static void resetGValues() {
		for (int i=0;i<cells.size();i++)
			((Cell)cells.get(i)).setGValue(-1);
	}
	
	public static void remove() {
		cells.clear();
	}
	
	public int compareTo(Cell obj) {
	
		int c = this.getX() - obj.getX();
		
		if (c == 0)
			c = this.getY() - obj.getY();
		
		return c;
	}
}
