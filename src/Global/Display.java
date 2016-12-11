package Global;

import java.awt.Color;

import javax.swing.JFrame;



public class Display extends JFrame{
    
    public static int window_width  = 800;
    public static int window_height = 600;
    public static int content_width, content_height;
    
    Display(){
        
        ///Pencere Ayarları
        this.setVisible(true);
        this.setSize(window_width,window_height);
        this.setTitle("Multiplayer Dama by. Furkan Enes Apaydın");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);      
        this.setLayout(null);        
        content_width=getContentPane().getWidth();
        content_height=getContentPane().getHeight();
        
        
        ///Mouse Listener
        MouseController MC=new MouseController();                
        this.addMouseListener(Game.Mouse=MC);
        
       
        
        
    }   
   
    
    
}
