
import java.io.*;
import java.util.*;
import java.io.File;

public class Storage1 {
	
	Map map;
	
	public static void saveMap(String name, Map map) {
		
		String s;
		s = "mkdir " + name.substring(0, name.lastIndexOf('.')) + " 2>/dev/null";
		
		/* for each map, create a folder to store the map
		   if the folder is already created, it won't be created
		   another folder
		*/   
		try {
			Process p = Runtime.getRuntime().exec(s);
		} catch (IOException e) {
            	e.printStackTrace();
		}
		
		s = null;
		s = "./"+name.substring(0, name.lastIndexOf('.'))+"/"+name;
		
		try {
		    // Create file	
		    FileWriter fstream = new FileWriter(s);
		    BufferedWriter out = new BufferedWriter(fstream);
		    
		    out.write(map.getLength() + "\n");
		    
		    //writes the matrix values into a file
		    for (int i=0;i<map.getLength();i++) {
		    	for (int j=0;j<map.getLength();j++) {
		    		out.write(map.map[i][j].getState()+ " ");
		    	}
		    	out.write("\n");
		    }	
		    
		    //Close the output stream
		    out.close();
		} catch (Exception e) {
		      System.err.println("Error: " + e.getMessage());
		}
		
	}
	
	public static Map loadMap(String name) {
		
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    Map map = null;
	    String line = null;
	    String delims = "[ ]+";
	    String[] tokens;
	    int i = 0;
	    
	    try {
	    	int index = name.lastIndexOf('.');
	    	String path = "./"+name.substring(0, index)+"/"+name; 
	    	
	    	fis = new FileInputStream(path);
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);
	    	
	    	line = dis.readLine();
	    	tokens = line.split(delims);
	    	
	    	map = new Map(Integer.parseInt(tokens[0]));
	    	
	    	//read the matrix values into our Map object
	    	while (dis.available() != 0) {
	    		line = dis.readLine();
	    		tokens = line.split(delims);
	    		
	    		for (int j=0;j<tokens.length;j++) {
	    			map.map[i][j].setState(Integer.parseInt(tokens[j]));
	    		}
	    		i++;
	    	}
	    	
	    	
	    	fis.close();
		    bis.close();
		    dis.close();
		      
	    } catch (IOException e) {
	    	  e.printStackTrace();
	    }	
	    
	    return map;
	}
	
}