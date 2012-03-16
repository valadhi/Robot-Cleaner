
import javax.swing.*;

/**
 * A class that creates a JFrame and places the drawCell composite image inside
 * it.
 */
public class drawMap{

	private  static drawCell d;
	Map map;
	public static JPanel map_panel;
	
	public JPanel getPanel(){
		
		return d;
		
	}
	
	public drawMap(Map map) {
		this.map = map;
		map_panel = new JPanel();


		d = new drawCell(map);

		map_panel.add(d);

	}
	/**
	 * Calling this method will update the image inside the JFrame according to values in Map
	 * class.
	 */	
	public void updateMap(){
		d.repaint();
	}

}
