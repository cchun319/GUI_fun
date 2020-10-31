import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author chunchang
 *
 */
public class CircleTest2 {

	/**
	 * @param args
	 * @throws IOException
	 * main script which generate JFrame and JPanel which we draw the circle on
	 */
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("FittingCircle");
		
		JPanel jp = new Integrated_GUI();
		frame.add(jp);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
