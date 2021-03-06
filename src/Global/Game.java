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
import Elements.Room;



public class Game {
    
    ///Server-side
    /*
    public static String                Database_Host="127.0.0.1";
    public static String                Database_User="root";
    public static String                Database_User_Password="";
    public static String                Database_Name="multidama";
    */
    
    public static String                Database_Host="92.42.34.49";
    public static String                Database_User="mdama_u_121216";
    public static String                Database_User_Password="mdp@2016!_";
    public static String                Database_Name="enes_multidama";
    
    
    public static Player                GamePlayer;

    
    ///Client-side
    public static Room                  Room=null;
    public static Controller            GameController;
    public static Display               GameWindow;
    public static MouseController       Mouse;
    public static DatabaseController    GameDB;
    
         
    public static void main(String[] args) {
        
        ///Görüntüyü Oluştur    
        GameWindow  =   new Display();        
        
        new Thread(new Runnable(){
            public void run(){
                GameDB      =   new DatabaseController(Database_Host,Database_Name,Database_User,Database_User_Password); 
            }
        }).start();
        
        new Thread(new Runnable(){
            public void run(){
                GameController= new Controller_Opening();
            }
        }).start();
        
        
        
        
    } 
    
    
    //Mevcut DrawController için repaint çağırımı
    public static void UpdateFrame(){
        GameController.DrawController.repaint();
    }    
    
    //Herhangi bir sorun karşısında menüye dön
    public static void ResetToMenu(){
        
        GameController=new Controller_Lobby();
        Room=null;
        UpdateFrame();
  
    }
    
    
    
}
