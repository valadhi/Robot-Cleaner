
import java.io.*;
import java.util.*;
import java.io.File;

public class Parser {
	
	public static String getCfgFile() {
		
		//current directory
		File f = new File(".");

		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".cfg")) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] files = f.listFiles(textFilter);
		
		return files[0].getName();
		
	}
	
	public static String parseCfgFile() {
		
		String name = getCfgFile();
		String mapWorld = null;
		String mapPicture = null;
		
		File file = new File(name);
		
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    
	    String line = "";
	    String delims = "[ ]+";
	    boolean b = false;
	    
	    try {
		      fis = new FileInputStream(file);
	
		      // Here BufferedInputStream is added for fast reading.
		      bis = new BufferedInputStream(fis);
		      dis = new DataInputStream(bis);
	
		      while (dis.available() != 0) {
		    	  line = dis.readLine();
		    	  String[] tokens = line.split(delims);
		    	  
		    	  for (int i=0;i<tokens.length;i++) {
		    		  if (tokens[i].equals("worldfile")) {
		    			  mapWorld = tokens[i+1].substring(1, tokens[i+1].length() - 1);
		    			  b = true;
		    			  break;
		    		  }
		    	  }
		    	  
		    	  if (b == true)
		    		  break;
		    
		      }
		      
		      fis.close();
		      bis.close();
		      dis.close();
		      
	      } catch (IOException e) {
	    	  e.printStackTrace();
	      }

	      
	      
	      b = false;
	      
	      try {
		      fis = new FileInputStream(mapWorld);

		      // Here BufferedInputStream is added for fast reading.
		      bis = new BufferedInputStream(fis);
		      dis = new DataInputStream(bis);

		      while (dis.available() != 0) {
		    	  line = dis.readLine();
		    	  String[] tokens = line.split(delims);
		    	  
		    	  for (int i=0;i<tokens.length;i++) {
		    		  if (tokens[i].equals("bitmap")) {
		    			  mapPicture = tokens[i+1].substring(1, tokens[i+1].length() - 1);
		    			  b = true;
		    			  break;
		    		  }
		    	  }
		    	  
		    	  if (b == true)
		    		  break;
		    
		      }
		      
		      fis.close();
		      bis.close();
		      dis.close();

	      } catch (IOException e) {
	    	  e.printStackTrace();
	      }
	      
	      return mapPicture;
	}
	
	public static void main(String[] args) {
		System.out.println(parseCfgFile());
	}
	
}