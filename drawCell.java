

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * A class that paints an image composed of 3 kinds of buffered tile images
 */
public class drawCell extends JPanel {

	Map map;
	public drawCell(Map map){
		this.map = map;
	}
	
	

	/**
	 * A method that draws adjacent tiles based on values in the map.
	 */
	public void paint(Graphics g) {

		/**
		 * Buffer all three tiles from files into memory for quicker painting
		 */
		BufferedImage img0 = null;
		try {
			img0 = ImageIO.read(new File("cell0-small.png"));
		} catch (IOException e) {
		}

		BufferedImage img1 = null;
		try {
			img1 = ImageIO.read(new File("cell1-small.png"));
		} catch (IOException e) {
		}

		BufferedImage img2 = null;
		try {
			img2 = ImageIO.read(new File("cell2-small.png"));
		} catch (IOException e) {
		}

		BufferedImage img3 = null;
		try {
			img3 = ImageIO.read(new File("cell3-small.png"));
		} catch (IOException e) {
		}
		
		BufferedImage img4 = null;
		try {
			img4 = ImageIO.read(new File("cell4-small.png"));
		} catch (IOException e) {
		}
		
		/**
		 * Get the number of cells to be drawn.
		 */
		int size = map.getLength();

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {

				/**
				 * Check for the state of the current cell via .getState() and
				 * paint an according tile image placed 25 (the size of all
				 * tiles) pixels right from the previous tile and 25 pixels
				 * lower from the previous level of tiles.
				 */
				int state = map.getState(x, y);
				switch (state) {
				case 0:
					g.drawImage(img0, x * 5, y * 5, this);
					break;
				case 1:
					g.drawImage(img1, x * 5, y * 5, this);
					break;
				case 2:
					g.drawImage(img2, x * 5, y * 5, this);
					break;
				case 3:
					g.drawImage(img3, x * 5, y * 5, this);
					break;
				case 4:
					g.drawImage(img4, x * 5, y * 5, this);
					break;
				default:
					g.drawImage(img2, x * 5, y * 5, this);
					break;

				}
			}
		}
	}
}
