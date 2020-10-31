import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author chunchang
 * The Circle class is responsible for drawing the circle, 
 * calculate the radius and store the centroid of the circle.
 * 
 * The Circle instance is designed as a state machine: Circle only calculates and repaints the surrounded two circles 
 * after the user selects a desired circle.
 * 
 */
public class Circle extends JComponent {
	// centroid
	Point p; 
	
	// radius
	int r; 
	// state machine 1.draw background 2.draw circle when dragging 3.draw the calculated surrounded circles 
	int state = 1;
	
	// store the indices of the grids inside the surrounded region
	int[][] board = new int[20][20];
	
	// radius of the surrounded circles( one >= radius, another <= radius)
	int maxr = Integer.MIN_VALUE;
	int minr = Integer.MAX_VALUE;

	// loop direction
	int[] h = {-1, 1, 0, 0};
	int[] v = {0, 0, 1, -1};


	{
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				p = e.getPoint(); 
				r = 0; 
				maxr = Integer.MIN_VALUE;
				minr = Integer.MAX_VALUE;
				state = 2;
				repaint();
			}
			public void mouseReleased(MouseEvent e) {
				r = (int) Math.round(e.getPoint().distance(p));
				state = 3;
				repaint();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				r = (int) Math.round(e.getPoint().distance(p));
				repaint();
			}
		});
		setPreferredSize(new Dimension(500, 500));
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//1.paint grid
		//2.paint circle when dragging
		//3.paint selected region 
		if(state == 1)
		{
			drawbackground(g);
		}
		else if(state == 2)
		{
			drawbackground(g);
			drawCircle(Color.RED, p.x, p.y, 2, g);
			drawCircle(Color.BLUE, p.x, p.y, r, g);
		}
		else if(state == 3)
		{
			drawSelectedGrid(g);
			drawCircle(Color.RED, p.x, p.y, 2, g);
			drawCircle(Color.BLUE, p.x, p.y, r, g);
			drawCircle(Color.RED, p.x, p.y, maxr, g);
			drawCircle(Color.RED, p.x, p.y, minr, g);
		}
	}
	
	/**
	 * @param g : The Graphics instance used for drawing
	 * iterate through the board to draw the grid
	 */
	private void drawbackground(Graphics g)
	{
		for (int i = 0; i < 20; i++)
		{
			for (int j = 0; j < 20; j++)
			{
				g.setColor(Color.GRAY);
				g.fillRect(25*i ,25*j, 10,10);
			}
		}
	}
	
	/**
	 * @param c : color for the circle
	 * @param x : x of top left points of the circle
	 * @param y : y of top left points of the circle
	 * @param r : radius of the circle
	 * @param g : The Graphics instance used for drawing
	 */
	private void drawCircle(Color c, int x, int y, int r, Graphics g)
	{
		g.setColor(c);
		g.drawOval(x - r, y - r, 2 * r, 2 * r); 
	}
	
	/**
	 * the function draw the grid or its adjacent grid as blue if one lays outside the circle and one lays on the other side
	 * 
	 * @param g : The Graphics instance used for drawing
	 */
	private void drawSelectedGrid(Graphics g)
	{
		for (int i = 0; i < 20; i++)
			{
				for (int j = 0; j < 20; j++)
				{
					//determine the grid and its surrounding grids cross the edge of circle or not
					//get the index of cross direction
					int[] crossid = crossEdge(i, j);
					
					if(crossid[0] >= 0 && crossid[1] >= 0)
					{
						g.setColor(Color.BLUE);
						g.fillRect(25*crossid[0], 25*crossid[1], 10, 10);
						int r_propos = (int)distance(crossid[0], crossid[1]) + 10;
						board[crossid[0]][crossid[1]] = 1;
						//update the potential radius of surrounding circles
						maxr = Math.max(maxr, r_propos);
						minr = Math.min(minr, r_propos - 20);
					}
					// the grid and its adjacencies do not cross the edge of the circle, draw gray color
					if(crossid[0] != i || crossid[1] != j)
					{
						g.setColor(Color.GRAY);
						g.fillRect(25*i, 25*j, 10, 10);
						board[i][j] = 0;
					}
				}
			}
		// 
		drawGap(g);
	}
	
	/**
	 * The function draws the gap between two points to fulfill the circle
	 * @param g: The Graphics instance used for drawing
	 */
	private void drawGap(Graphics g)
	{
		for (int i = 0; i < 20; i++)
		{
			for (int j = 0; j < 20; j++)
			{
				boolean check = Gap(i, j);
				double dist = distance(i, j);
				// if the grid is inside the surrounded area, and missed by the drawSelectedGrid method
				if(board[i][j] == 0 && check && dist >= minr && dist<= maxr)
				{
					board[i][j] = 1;
					//update the board
					g.setColor(Color.BLUE);
					g.fillRect(25*i, 25*j, 10, 10);
				}
			}
		}
	}
	
	/**
	 * The function 
	 * @param i : index of board
	 * @param j : index of board
	 * @return whether the grid is the gap of the circle
	 */
	private boolean Gap(int i, int j)
	{
		boolean g = false;
		
		//border condition
		int left = (int)Math.max(0, i -1);
		int right = (int)Math.min(19, i + 1);
		
		// check horizontal direction
		// return false if connected
		for(int k = left; k <=right; k++)
		{
			if(board[k][j] == 1)
			{
				return false;
			}
		}
		
		// check vertical direction
		// both up and down should have grids then we can make sure the grid(i, j) is a gap
		if(j > 0)
		{
			g = false;
			for(int k = left; k <=right; k++)
			{
				if(board[k][j - 1] == 1)
				{
					g = true;
					break;
				}
			}
			if(g == false)
			{
				return g;
			}
			
		}
		// check higher row
		if(j < 19)
		{
			g = false;
			for(int k = left; k <=right; k++)
			{
				if(board[k][j + 1] == 1)
				{
					g = true;
					break;
				}
			}
		}
		
		return g;
	}

	/**
	 * check if the grid and its adjacencies cross the edge of the circle
	 * @param i : horizontal index of the grid
	 * @param j : vertical index of the grid
	 * @return the indices cross the circle with the closest distance
	 */
	private int[] crossEdge(int i, int j)
	{
		int[] id = {-1,-1};
		double dist_o = distance(i, j) - r;

		for(int k = 0; k < 4; k++)
		{
			// check if the adjacent point out of bound
			if(i + h[k] < 0 || i + h[k] >= 20 || j + v[k] < 0 || j + v[k] >= 20)
			{
				continue;
			}
			double dist_s = distance(i + h[k], j + v[k]) - r;

			if(dist_o * dist_s <= 0)
			{
				// keep the one with shorter distance
				if(Math.abs(dist_o) < Math.abs(dist_s))
				{
					id[0] = i;
					id[1] = j;
				}
				else
				{
					id[0] = i + h[k];
					id[1] = j + v[k];
				}
			}
		}

		return id;
	}

	/**
	 * caculate the distance between the centroid to the grid
	 * @param i : index on the board
	 * @param j : index on the board
	 * @return distance in (double) in unit of pixels
	 */
	private double distance(int i, int j)
	{
		return Math.sqrt(Math.pow(25*i - p.x, 2) + Math.pow(25*j - p.y, 2));
	}
}
