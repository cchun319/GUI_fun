import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * @author chunchang
 *
 */
public class Integrated_GUI extends JPanel{
	
	JButton JB;
	FittingCircle FC = new FittingCircle();
	Integrated_GUI()
	{
		super(new BorderLayout());
		this.add(FC);
		JB = new JButton("GENERATE");
		// The JButton listen to the click motion and trigger the wrapper method inside FittingCircle instance
		JB.addActionListener(new ActionListener(){
		
			@Override
			public void actionPerformed(ActionEvent e) {
				FC.wrapper();
				// wrapper function calls FittingPts function inside FittingCircle class to do iterative fitting
			}
			
		});
		this.add(JB, BorderLayout.PAGE_END);
	}
	
	
}
