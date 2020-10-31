import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * @author chunchang
 *
 */
public class CircleTest {

	/**
	 * @param args
	 * @throws IOException
	 * main script which generate JFrame and JPanel which we draw the circle on
	 */
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("SelectedGrid");
		Circle cc = new Circle();
		frame.add(cc);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}


