import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;


public class EvalGUI extends JFrame {
	
	//Statistics and Map Panel
	public JPanel Map_Panel = new JPanel(new BorderLayout());
	JPanel Right_Panel = new JPanel(new BorderLayout());
	JPanel Statistics_Panel = new JPanel(new GridLayout(16,1));
	JMenuBar menubar = new JMenuBar();
	JMenu file = new JMenu("File");
	JMenu options = new JMenu("Options");
	JMenu exit = new JMenu("Exit");
	JMenuItem newmap = new JMenuItem("Open map file in new window"); 
	JMenuItem measure = new JMenuItem("Change measurement units");
	JMenuItem speed = new JMenuItem("Change speed");		
	JMenuItem exit_ = new JMenuItem("Exit program");
	
	//Statistics Labels

	JLabel dist_title = new JLabel("<html><h2>Distance Statistics<h2></html>");
	JLabel totaldistance = new JLabel("Total distance traveled : 30m");
	JLabel dist_curr_obj = new JLabel("Distance to current objective : 25m");
	JLabel dist_charge = new JLabel("Distance to charging station : 53m");
	JLabel clean_title = new JLabel("<html><h2>Cleaning Statistics<h2></html>");
	JLabel space_covered = new JLabel("30% of total space has been covered");
	JLabel room_covered = new JLabel("76% of current room has been covered");
	JLabel charge_visits = new JLabel("Number of visits to charge station needed : 2");
	JLabel battery_title = new JLabel("<html><h2>Battery Statistics<h2></html>");
	JLabel batt_remaining = new JLabel("30% of battery remaining");
	JLabel coverage_title = new JLabel("<html><h2>Coverage Statistics<h2></html>");
	JLabel nr_coverage_divisions = new JLabel("Number of coverage divisions : 6");
	JLabel vert_hor_div = new JLabel("Number of vertical/horizontal divisions : 3/5");
	JLabel rob_coord = new JLabel(" Robot is at coordinates (x,y)");
	JLabel time_elapsed = new JLabel("Time elapsed : 2:03:05");
	JLabel time_completion = new JLabel("Time remaining : 1:01:00");

	/*
	 * Insert map JPanel
	 */
	public JPanel getMap_Panel(){
		return Map_Panel;
	}
	
	public void setTotalDist(int dist){
		totaldistance.setText("Total distance traveled: " + Integer.toString(dist) + "m");
	}
	/*
	 * Frame Constructor
	 */
	public EvalGUI(){


		 
		//map image
		
		Image img = null;
		try {
			img = ImageIO.read(new File("map2.png"));
		} catch (IOException e) {
		}
		
		img = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
		
		JLabel small_img = new JLabel(new ImageIcon(img));
		
		
		getContentPane().setLayout(new BorderLayout());
		
		Right_Panel.add(Statistics_Panel, BorderLayout.CENTER);

		Statistics_Panel.add(dist_title);
		Statistics_Panel.add(totaldistance);
		Statistics_Panel.add(dist_curr_obj);
		Statistics_Panel.add(dist_charge);
		Statistics_Panel.add(clean_title);
		Statistics_Panel.add(space_covered);
		Statistics_Panel.add(room_covered);	
		Statistics_Panel.add(charge_visits);
		Statistics_Panel.add(battery_title);
		Statistics_Panel.add(batt_remaining);		
		Statistics_Panel.add(coverage_title);
		Statistics_Panel.add(nr_coverage_divisions);
		Statistics_Panel.add(vert_hor_div);
		Statistics_Panel.add(rob_coord);
		Statistics_Panel.add(time_elapsed);
		Statistics_Panel.add(time_completion);
		Right_Panel.add(small_img,BorderLayout.SOUTH);
		
		add(Right_Panel,BorderLayout.EAST);
		add(Map_Panel,BorderLayout.WEST);
		
		//menu items
		menubar.add(file);
		menubar.add(options);
		menubar.add(exit);
		file.add(newmap);
		options.add(measure);
		options.add(speed);
		exit.add(exit_);
		setJMenuBar(menubar);
		
		//frame operations
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(860,650);
		setTitle("EvaluationGUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//pack();
		
	}

}
