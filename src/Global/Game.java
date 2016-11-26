/*
 * 
 * Multiplayer Dama Oyunu
 * @author: Furkan Enes Apaydın
 * @date: 11.2016
 * İstanbul Gelişim Üniversitesi
 *
 */

package Global;

import Controller.*;
import Drawing.*;
import Elements.Player;

public class Game {
    

    public static Controller        GameController;
    public static Display           GameWindow;
    public static MouseController   Mouse;
    public static Player            GamePlayer;
    public static String            Opponent;
       
    public static void main(String[] args) {
        
        ///Görüntüyü Oluştur    
        GameWindow=new Display();
        GamePlayer=new Player("FEApaydin");
        
        GameController=new Controller_Ingame();
        
          
        
    } 
    
    
    //Mevcut DrawController için repaint çağırımı
    public static void UpdateFrame(){
        GameController.DrawController.repaint();
    }
    
}
