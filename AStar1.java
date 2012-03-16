


import java.io.*;
import java.util.*;
import java.awt.*;

public class AStar1 {

	private static int FOUND = 1;
	private static int NOT_FOUND = 0;
	private Map map;
	private Vector<Cell> closed;
	private Vector<Cell> open;
	private Vector<Cell> path;
	
	
	public AStar1(Map map) {
		this.map = map;
	}
	
	public double getFValue(Cell c) {
		return c.getGValue() + getHValue(c.getPosition(), Cell.getFinishCell().getPosition(), (double)Cell.cost);
	}
	
	public double getHValue(Point now, Point finish, double D){
        
		Point start = Cell.getStartCell().getPosition();
        
		int dx1 = now.x - finish.x;
        int dy1 = now.y - finish.y;
        int dx2 = start.x - finish.x;
        int dy2 = start.y - finish.y;
        int cross = Math.abs(dx1*dy2 - dx2*dy1);

        return  D * (Math.abs(dx1) + Math.abs(dy1) + cross*0.0002);
    }
	
	//return the Cell from open vector with the minimum f
	public Cell getCell() {
			
		double min = Double.MAX_VALUE;
		double score;
		Cell best = open.get(0);
			
		for(int i=0;i<open.size();i++){
			Cell c = open.get(i);
	        if(!closed.contains(c)) {
	        	score = getFValue(c);
	            if(score < min) {
	            	min = score;
	            	best = c;
	            }
	        }
		}
		
		return best;
	}
		
	public Vector<Cell> findPath(Cell start, Cell finish) {
		Cell.setStartCell(start);
		Cell.setFinishCell(finish);
		
		int state = AStar();
		if (state == FOUND) {
			buildPath();
			return path;
		}	
		
		return null;
	}
	
	private void buildPath() {
		
		path = new Vector<Cell>();
		Cell c = Cell.getFinishCell();
			
		while(!c.getPosition().equals(Cell.getStartCell().getPosition())) {
			path.add(0, c);
			c = c.getParent();
		}		
	}
	
	
	private int AStar() {
		
		int tentative_g_score;
		boolean tentative_is_better;

		open = new Vector<Cell>();
		closed = new Vector<Cell>();
		
		Cell start = Cell.getStartCell();
		start.setGValue(0);
		start.setParent(null);
		open.add(start);
		
		while (!open.isEmpty()) {
			
			Cell n = getCell();
			
			if (n.equals(Cell.getFinishCell()))
				return FOUND;
			
			open.remove(n);
			closed.addElement(n);

			Cell adj[] = map.getAdjacent(n);
			
			if (n.getPosition().equals(Cell.getStartCell().getPosition())) {
				int j;
				for (j=0;j<4;j++)
					if (adj[j] != null)
						break;
				
				if (j == 4)
					System.out.println("celula de inceput nu are vecini valizi");
				
			}
			
			for (int i=0;i<adj.length;i++) {
				if (adj[i] != null) {
					if (closed.contains(adj[i]))
						continue;
		
					tentative_g_score = n.getGValue() + Cell.cost;
					
					if (!open.contains(adj[i])) {
						open.addElement(adj[i]);
						tentative_is_better = true;
					} else
					if (tentative_g_score < adj[i].getGValue()) {
						tentative_is_better = true;
					} else {
						tentative_is_better = false;
					}
					
					if (tentative_is_better == true) {
						adj[i].setParent(n);
						adj[i].setGValue(tentative_g_score);
						//adj[i].setState(4);
					}
				}
			}
			
		}
		
		return NOT_FOUND;
	}
}
