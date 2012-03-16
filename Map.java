


import java.awt.Point;
import java.io.*;


public class Map implements Serializable{
	
	private int length = 0;
	public static final int OCCUPIED = 0;
	public static final int FREE = 1;
	public static final int UNKNOWN = 2;
	public static final int ROBOT = 3;
	public static final int FRONTIER = 4;
	Cell map[][];
	
	public Map(int tempLength){
		length = tempLength;
		map = new Cell[length][length];
		for(int i=0;i<length;i++) {
            for(int j=0;j<length;j++) {
               map[i][j] = new Cell();
               map[i][j].setPosition(new Point(i, j));
               map[i][j].setState(UNKNOWN);
            }
        }
	}
	
	
	public Cell[][] getMap() {
		return map;
	}
	
	
	public Cell[][] extendMap(int newLength) {
		
		Cell[][] newMap = new Cell[newLength][newLength];
		Cell.remove();
		
		for (int i=0;i<newLength;i++){
			for (int j=0;j<newLength;j++){
				newMap[i][j] = new Cell();
				newMap[i][j].setPosition(new Point(i, j));
	            
				if (i >= (newLength - length)/2 && i < newLength - (newLength - length)/2) {
					if (j >= (newLength - length)/2 && j < newLength - (newLength - length)/2) {
						newMap[i][j].setState(map[i][j].getState());
					} else {
						newMap[i][j].setState(UNKNOWN);
					}
				} else {
					newMap[i][j].setState(UNKNOWN);
				}
			}
		}
		
		map = newMap;
		length = newLength;
		
		return map;
	}
	
	public boolean checkArea(int x, int y) {
		
		for (int i=x-2;i<=x+2;i++) {
			for (int j=y-2;j<=y+2;j++) {
				if (i < 0 || i >= length || j < 0 || j >= length) {
					return false;
				}else {	
					if (map[i][j].getState() == OCCUPIED) {
						return false;
					}	
				}	
			}
		}
		
		return true;
	}
	
	//in adj I store the values in the next order:north, east, south, west
	public Cell[] getAdjacent(Cell x){
        
		Cell adj[] = new Cell[4];
        Point p = x.getPosition();
        
        if(p.y != 0) {
        	if (checkArea(p.x, p.y-1))
        		adj[0] = map[p.x][p.y-1];
        }
        if(p.x != length-1) {
        	if (checkArea(p.x+1, p.y))
        		adj[1] = map[p.x+1][p.y];
        }
        if(p.y != length-1) {
        	if (checkArea(p.x, p.y+1))
        		adj[2] = map[p.x][p.y+1];
        }
        if(p.x != 0) {
        	if (checkArea(p.x-1, p.y))
        		adj[3] = map[p.x-1][p.y];
        }
        
        return adj;
    }
	
	//returns the state of a cell in map, used in drawCell to paint correct image - Alo
		public int getState(int x,int y){
			return map[x][y].getState();
		}
		
		//returns the size of the map, used in drawCell to paint the correct amount of images - Alo
		public int getLength() {
			return length;
		}
		
		public Cell getCell(int x, int y) {
			return map[x][y];
		}
	
}
