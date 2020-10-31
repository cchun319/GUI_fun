import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

/**
 * @author chunchang
 *
 */
public class FittingCircle extends JComponent {
	Point p;
	Point estimate_center = new Point(0,0); 
	double[] estimate_ = new double[]{0, 0};
	double r = 1; 
	int state = 1;

	ArrayList<Point> selected = new ArrayList<Point>();


	{
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				p = e.getPoint(); 
			}
			public void mouseReleased(MouseEvent e) {
				int i = (int)p.x / 25 ;
				int j = (int)p.y / 25 ;

				if(p.x - 25* i <= 10 && p.y - 25* j <= 10)
				{
					selected.add(new Point(i, j));
				}
				repaint();
			}
		});
		setPreferredSize(new Dimension(500, 500));
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawbackground(g);
		for(int i = 0; i < selected.size(); i++)
		{
			drawSelectedGrid(selected.get(i), g);
		}

		if(state == 2)
		{
			drawCircle(Color.RED, estimate_center.x, estimate_center.y, (int)r, g);
		}
	}

	private void drawbackground(Graphics g)
	{
		for (int i = 0; i < 20; i++)
		{
			for (int j = 0; j < 20; j++)
			{
				g.setColor(Color.GRAY);
				g.fillRect(25*i, 25*j, 10, 10);
			}
		}
	}

	/**
	 * The warpper is a public portal to other class. 
	 * the function is called only when the button is pressed
	 * than the instance does the Fitting algorithm to find the best fit and repaint the component
	 */
	public void wrapper()
	{
		FittingSelectPTs();
		state = 2;
		repaint();
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
	 * @param s list of points
	 * @param g The Graphics instance used for drawing
	 */
	private void drawSelectedGrid(Point s, Graphics g)
	{
		g.setColor(Color.BLUE);
		g.fillRect(25*s.x, 25*s.y, 10, 10);
	}

	/**
	 * @param samplesize: number of points we selected
	 * @return the matrix used for calculation of algebraic fit of the circle
	 */
	private SimpleMatrix Bmatrix(int samplesize)
	{
		SimpleMatrix B = new SimpleMatrix(new double[samplesize][4]);
		for(int i = 0; i < selected.size(); i++)
		{
			double x1 = 25*selected.get(i).x;
			double x2 = 25*selected.get(i).y;
			double x12_sq = x1*x1 + x2*x2;
			B.set(i, 0, x12_sq);
			B.set(i, 1, x1);
			B.set(i, 2, x2);
			B.set(i, 3, 1);

		}
		return B;
	}
	
	/**
	 * The function uses algebraic fit to find the start point of the iterative algo.
	 * Bu = 0, argmax(B) ->min(Bu) -> SVD
	 * @param samplesize: number of points we selected
	 */
	private void FindStart(int samplesize)
	{
		SimpleMatrix B = Bmatrix(samplesize);

		SimpleSVD sv = B.svd();
		double a = sv.getV().get(0, 3);
		double b1 = sv.getV().get(1, 3);
		double b2 = sv.getV().get(2, 3);
		double c = sv.getV().get(3, 3);
		
//		estimate_center.x = (int)(-b1 / (2*a));
//		estimate_center.y = (int)(-b2 / (2*a));
		estimate_[0] = -b1 / (2*a);
		estimate_[1] = -b2 / (2*a);
		r = (Math.sqrt( (b1*b1 + b2*b2)/(4*a*a) - c / a));

	}

	/** 
	 * iterative algorithm to find the best fit of the circle
	 * the function uses Newton-Gauss method the approach the best fit
	 * The loss function is the summation of the points to the centroid subtracted the current radius
	 * then we use jacobian as the gradient descent direction to minimize the loss function 
	 * after an amount of iteration, the error should be converge to best fit condition
	 */
	private void FittingSelectPTs()
	{
		int m = selected.size();
		//find the start point for Newton-Gauss method
		FindStart(m);

		SimpleMatrix u = new SimpleMatrix(new double[3][1]);
		SimpleMatrix h = new SimpleMatrix(new double[3][1]);
		
		u.set(0, 0, estimate_[0]);
		u.set(1, 0, estimate_[1]);
		u.set(2, 0, r);
		
		

		// gradually approach the best fit point by solving non linear least square
		for (int i = 0; i < 10; i++)
		{
			//approximate the nonlinear LS by linear method
			SimpleMatrix J = Jacob(m); 
			SimpleMatrix J_inv = J.pseudoInverse();
			h = J_inv.mult(distMat(m));
			
			// update the current unknown
			u = u.plus(h);
			estimate_[0] = u.get(0, 0);
			estimate_[1] = u.get(1, 0);
			r = u.get(2, 0);
			
			System.out.print(h.toString() + "\n");
			System.out.printf("error: %f\n", err());
			
			
		}
		estimate_center.x = (int)estimate_[0];
		estimate_center.y = (int)estimate_[1];
	}
	
	/**
	 * @param samplesize : number of selected points
	 * @return  matrix(samplesizex1) of residual vector
	 */
	private SimpleMatrix distMat(int samplesize)
	{
		SimpleMatrix F = new SimpleMatrix(new double[samplesize][1]);
		for(int i = 0; i < selected.size(); i++)
		{
			double u_cur = distance(25*selected.get(i).x, 25*selected.get(i).y) - r;
			F.set(i, 0, -u_cur);
		}
		return F;
	}

	/**
	 * @param samplesize : number of selected points
	 * @return matrix(samplesizex3) of jacobian matrix of current state, which is the approxiamted slope of the function
	 */
	private SimpleMatrix Jacob(int samplesize)
	{
		SimpleMatrix J = new SimpleMatrix(new double[samplesize][3]);
		for(int i = 0; i < selected.size(); i++)
		{
			double u_cur = distance(25*selected.get(i).x, 25*selected.get(i).y);
			
			double j1 = (estimate_[0] - 25*selected.get(i).x) / u_cur;
			double j2 = (estimate_[1] - 25*selected.get(i).y) / u_cur;

			J.set(i, 0, j1);
			J.set(i, 1, j2);
			J.set(i, 2, -1);
		}

		return J;
	}

	/**
	 * sum( square of (|xi - centroid| - current radius)) 
	 * @return the error in (double)
	 */
	private double err()
	{
		double er = 0;
		for(int i = 0; i < selected.size(); i++)
		{
			er += Math.pow(distance(25*selected.get(i).x, 25*selected.get(i).y) - r, 2);
		}
		return er;
	}

	/**
	 * calculate the distance between the centroid to the grid
	 * @param i : index on the board
	 * @param j : index on the board
	 * @return distance in (double) in unit of pixels
	 */
	private double distance(int ptx, int pty)
	{
		return Math.sqrt(Math.pow(ptx - estimate_[0], 2) + Math.pow(pty - estimate_[1], 2));
	}

}

