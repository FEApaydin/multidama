package Elements;

import java.awt.Color;
import javax.swing.JTextField;

public class TextField extends JTextField {
    
    public TextField(int posX, int posY, int width, int height){
        
        setSize(width,height);  
        setLocation(posX,posY);
        setBackground(new Color(39,39,14));
        //setBackground(Color.RED);
        setBorder(null);
        setForeground(Color.WHITE);
        
        
        repaint();
        
    }
    
    
    
    
    
}
