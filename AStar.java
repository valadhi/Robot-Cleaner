
import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map.Entry;

public class AStar {

	private static int FOUND = 1;
	private static int NOT_FOUND = 0;
	private Map map;
	private Vector closed;
	//PriorityQueue<Entry<Cell, Double>> open;
	private Vector open;
	
	public boolean contains(PriorityQueue<Entry<Cell, Double>> pq, Cell c) {
		
		Iterator itr = pq.iterator();
		
		while (itr.hasNext()) {
			Entry<Cell, Double> e = (Entry)itr.next();
			if (e.getKey().equals(c))
				return true;
		}
		
		return false;
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
	
	//TO DO: implement the function
	public Cell[] findPath(Map map) {
		this.map = map;
		Cell adj[] = new Cell[4];
		return adj;
	}
	
	public int buildPath() {
		return 0;
	}
	
	//return the Cell from open vector with the minimum f
	public Cell getCell() {
		
		double min = Double.MAX_VALUE;
		double score;
		Cell best = (Cell)open.get(0);
		
		for(int i=0;i<open.size();i++){
			Cell c = (Cell)open.get(i);
            if(!closed.contains(c)) {
             
                score = getFValue(c);
                if(score < min){
                    min = score;
                    best = c;
                }
            }
        }
		
		return best;
	}
	
	public int AStar() {
		
		int tentative_g_score;// = NOT_FOUND;
		boolean tentative_is_better;
		boolean isInOpen = false;
		//open = new PriorityQueue<Entry<Cell, Double>>((Collection<? extends Entry<Cell, Double>>) new MyComparator());
		open = new Vector();
		closed = new Vector();
		
		Cell start = Cell.getStartCell();
		
		start.setGValue(0);
		//Entry<Cell, Double> e = new AbstractMap.SimpleEntry<Cell, Double>(start, getFValue(start));
		//open.add(e);
		open.add(start);
		start.setParent(null);
		
		while (!open.isEmpty()) {
			//Entry<Cell, Double> n = open.element();
			//if (n.getKey().equals(Cell.getFinishCell()))
				//return buildPath();
			
			Cell n = getCell();
			if (n.equals(Cell.getFinishCell()))
				return buildPath();
			//open.remove();
			//closed.addElement(n.getKey());
			open.remove(n);
			closed.addElement(n);
			
			//Cell adj[] = map.getAdjacent(n.getKey());
			Cell adj[] = map.getAdjacent(n);
			
			for (int i=0;i<adj.length;i++) {
				if (adj[i] != null) {
					if (closed.contains(adj[i]))
						continue;
					
					//tentative_g_score = n.getKey().getGValue() + 1;
					tentative_g_score = n.getGValue() + Cell.cost;
					
					//if (!contains(open, adj[i])) {
					if (!open.contains(adj[i])) {
						//e = new AbstractMap.SimpleEntry<Cell, Double>(adj[i], getFValue(adj[i]));
						open.addElement(adj[i]);
						tentative_is_better = true;
						//isInOpen = true;
					} else
					if (tentative_g_score < adj[i].getGValue()) {
						tentative_is_better = true;
					} else {
						tentative_is_better = false;
					}
					
					if (tentative_is_better == true) {
						//adj[i].setParent(n.getKey());
						adj[i].setParent(n);
						adj[i].setGValue(tentative_g_score);
						
					}
					/*if (adj[i] == Cell.getFinishCell())
						state = FOUND;
					
					if (adj[i].getState() != Map.OCCUPIED) {
						adj[i].setGValue(n.getKey().getGValue());
					}
					
					adj[i].setParent(n.getKey());
					
					if (!closed.contains(adj[i]) && contains(open, adj[i]))
						closed.addElement(adj[i]);
					*/
				}
			}
			
		}
		
		return 0;
	}
	
	public Point findTarget() {
		return new Point();
	}
}
