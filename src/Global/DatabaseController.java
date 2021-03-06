package Global;


import Controller.Controller_Ingame;
import Controller.Controller_Lobby;
import Elements.*;
import static Global.Game.GameController;
import static Global.Game.Room;
import static Global.Game.UpdateFrame;
import java.awt.Color;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.swing.JOptionPane;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseController {
    
    public String host, dbname, username, pass;
    public static int GameID;
    public static boolean IsConnected=false;      
    
    public Connection Connection;    
    
    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
    
    
    DatabaseController(String h, String d, String u, String p){
        
        this.host=h;
        this.dbname=d;
        this.username=u;
        this.pass=p;
        
        try{
            
            Connection=DriverManager.getConnection("jdbc:mysql://"+host+"/"+dbname+"?characterEncoding=utf8",username,pass);             
            IsConnected=true;
            System.out.println("Veritabanı bağlantısı sağlandı.");
            
            
        }catch(Exception e)
        {            
            Game.GameWindow.setVisible(false);
            JOptionPane.showMessageDialog(null,"Veri tabanı bağlantı hatası.");
            System.exit(0);
        }
        
    }
    
    
    ////Data İşlemleri
    
    
    //Odaya Bağlanma
    public void RoomInit(int GameId, String Password){
        if(IsConnected)
        {
            try{
                PreparedStatement s;
                s=Connection.prepareStatement("SELECT * FROM game_rooms WHERE r_id=? AND r_pass=? AND r_p1!=-1");
                s.setInt(1, GameId);
                s.setString(2, Password);                
                ResultSet rs=s.executeQuery();
                if(rs.next())
                {
                    System.out.println("Oda bulundu.");
                    
                    ResultSet p1=Connection.createStatement().executeQuery("SELECT * FROM game_players WHERE p_id="+rs.getInt("r_p1"));
                    
                    if(p1.next())
                    {           
                        System.out.println("P1 bulundu.");
                        
                        Game.Room=new Room();
                        Game.Room.ID=rs.getInt("r_id");
                        Game.Room.GameName=rs.getString("r_ad");
                        Game.Room.GamePassword=Password;
                        Game.Room.PlayTurn=rs.getInt("r_turn");
                        
                        System.out.println("Oda yüklendi: "+Game.Room.GameName);
                        
                        if(p1.getInt("p_id")==Game.GamePlayer.ID)
                        {
                            System.out.println("Oyuncu P1 (Host).");
                            Game.GamePlayer.host=true;
                            
                            if(rs.getInt("r_p2")!=-1)
                            {
                                System.out.println("Oyunun P2 si var.");
                                ResultSet opponent=Connection.createStatement().executeQuery("SELECT * FROM game_players WHERE p_id="+rs.getInt("r_p2"));

                                if(opponent.next())
                                {
                                    System.out.println("Opponent (P2) yüklendi: "+opponent.getString("p_ad"));
                                    Game.Room.Opponent=new Player(opponent.getString("p_ad"),opponent.getInt("p_id"));
                                    Game.Room.startGame();
                                    GetGameData();
                                    GetGridList();
                                }
                                else //Player2 Database'de Yok
                                {
                                   JOptionPane.showMessageDialog(null, "Atanmış ikinci oyuncu bulunamadı.");
                                   Game.ResetToMenu(); 
                                }


                            } 
                            else //Player2 yok
                            {
                                System.out.println("İkinci oyuncu bekleniyor.");
                                //bekle
                            }
                            
                        }
                        else //Oyuncu host değil
                        {
                            
                         
                            if(rs.getInt("r_p2")==-1)
                            {
                                System.out.println("Oyunun P2 si "+Game.GamePlayer.name+" olarak atandı.");
                                Connection.createStatement().executeUpdate("UPDATE game_rooms SET r_p2="+Game.GamePlayer.ID+" WHERE r_id="+rs.getInt("r_id"));                            
                                Connection.createStatement().executeUpdate("UPDATE game_grids SET g_durum=1, g_owner="+Game.GamePlayer.ID+" WHERE g_roomId="+rs.getInt("r_id")+" AND (g_posY=2 OR g_posY=3)");                            

                                
                                System.out.println("Opponent (P1) yüklendi: "+p1.getString("p_ad"));
                                Game.Room.Opponent=new Player(p1.getString("p_ad"),p1.getInt("p_id"));
                                Game.Room.startGame();
                                GetGameData();
                                GetGridList();
                            }
                            else if(rs.getInt("r_p2")==Game.GamePlayer.ID)           
                            {
                                
                                System.out.println("Opponent (P1) yüklendi: "+p1.getString("p_ad"));
                                Game.Room.Opponent=new Player(p1.getString("p_ad"),p1.getInt("p_id"));
                                Game.Room.startGame();
                                GetGameData();
                                GetGridList();
                            }
                            else //Spectator
                            {
                                Game.GamePlayer.spectator=true;                                                              
                                Game.Room=null;                                
                                JOptionPane.showMessageDialog(null, "Oda dolu."); 
                                return;
                            }
                            
                            
                            
                        }
                        
                        Game.GameController=new Controller_Ingame();
                        Game.UpdateFrame();
                        
                        
                    }
                    else //Player1 yok
                    {
                        JOptionPane.showMessageDialog(null, "Oda yüklenirken teknik bir hata oluştu.");
                        Game.ResetToMenu();
                    }
                    
                }
                else // Oda Yok
                {
                    JOptionPane.showMessageDialog(null, "Oyuna bağlanılamadı.\nOda dolu ya da parola hatalı.");
                    Game.ResetToMenu();
                }
                
                
                
                
                
            }
            catch(SQLException e)
            {
                JOptionPane.showMessageDialog(null, "Oyun yüklenirken bir sorun oluştu.");
                Game.ResetToMenu();
            }
        }
      
    } //End RoomInit()
    
    
    
    ///////////// IN-GAME FUNCS //////////////////////
    
    
    //Oda Gridleri Çekme
    public void GetGridList(){
        if(IsConnected  && roomExists() && opponentExists())
        {        
            int GameId=Game.Room.ID;
            try {
                
                ResultSet count=Connection.createStatement().executeQuery("SELECT COUNT(*) FROM game_grids WHERE g_roomId="+GameId);
                if(count.next())
                {
                    if(count.getInt("COUNT(*)")==64)
                    {
                
                        PreparedStatement s = Connection.prepareStatement("SELECT * FROM game_grids WHERE g_roomId=?");
                        s.setInt(1, GameId);

                        ResultSet rsg=s.executeQuery();

                        Controller_Ingame.GridDrawPointX=Controller_Ingame.GridDrawStartPointX;
                        Controller_Ingame.GridDrawPointY=Controller_Ingame.GridDrawStartPointY;                
                        Arrays.fill(Controller_Ingame.GridList,null);
                        
                        int gridId=0;
                        while(rsg.next())
                        {                    
                            gridId=rsg.getInt("g_inGameId");
                            
                            Controller_Ingame.GridDrawPointX=Controller_Ingame.GridDrawStartPointX+(Controller_Ingame.GridSize*(rsg.getInt("g_posX")-1));
                            Controller_Ingame.GridDrawPointY=Controller_Ingame.GridDrawStartPointY+(Controller_Ingame.GridSize*(rsg.getInt("g_posY")-1));

                            Color gridColor=gridId%2==(rsg.getInt("g_posY")%2)?new Color(44,10,1):Color.WHITE;
                            Controller_Ingame.GridList[gridId]=new Grid(gridId,rsg.getInt("g_posX"),rsg.getInt("g_posY"),Controller_Ingame.GridDrawPointX,Controller_Ingame.GridDrawPointY,gridColor);

                            if(rsg.getInt("g_owner")!=0 && Game.Room.Opponent!=null)
                            {
                                if(rsg.getInt("g_owner")==Game.GamePlayer.ID)
                                    Controller_Ingame.GridList[gridId].owner=Game.GamePlayer;
                                else if(rsg.getInt("g_owner")==Game.Room.Opponent.ID)
                                    Controller_Ingame.GridList[gridId].owner=Game.Room.Opponent;
                            }

                            Controller_Ingame.GridList[gridId].durum=(short)rsg.getInt("g_durum");


                        }
                        
                        getTurn();
                                    
                                                
                        rsg.close();
                        
                    

                    }
                    else //64 grid yok
                    {
                         JOptionPane.showMessageDialog(null, "Grid listesi eksik.");
                         Game.ResetToMenu();
                    }
                }
                
                count.close();
                
                
            } catch (SQLException ex) {               
                JOptionPane.showMessageDialog(null, "Grid listesi alınırken bir sorun oluştu.");
                Game.ResetToMenu();
            }      
            
            Game.UpdateFrame();            
            
        }
        
    } //End GetGridList()
    
    
    
    public void getLastMoved(){
        if(IsConnected && roomExists() && opponentExists() && Controller_Ingame.GameLogic.islenen==null)
        {
            try{
                ResultSet rt=Connection.createStatement().executeQuery("SELECT r_lastMove, r_lastMoveFrom FROM game_rooms WHERE r_id="+Game.Room.ID);
                if(rt.next())
                {
                    Game.Room.LastMoved=rt.getInt("r_lastMove");
                    Game.Room.LastMovedFrom=rt.getInt("r_lastMoveFrom");
                  
                }
                else
                {
                    Game.Room.LastMoved=-1;
                    Game.Room.LastMovedFrom=-1;
                }
            }catch(Exception e)
            {
                Game.Room.LastMoved=-1;
                Game.Room.LastMovedFrom=-1;
            }
        
        }  
        else
        {
            Game.Room.LastMoved=-1;
        }
    }
    
    
    public void setLastMoved(int lastmoveId, int fromId)
    {
        if(IsConnected && roomExists() && opponentExists())
        {
            try{
                Connection.createStatement().executeUpdate("UPDATE game_rooms SET r_lastMove="+lastmoveId+", r_lastMoveFrom="+fromId+" WHERE r_id="+Game.Room.ID);
                
            }catch(Exception e)
            {
               System.out.println("Son oynanan düzenlenemedi.");
            }
        
        }  
    }
    
    
    
    
    //Sıra çek
    public int getTurn(){
        if(IsConnected  && roomExists())
        {
            try{        

                ResultSet rt=Connection.createStatement().executeQuery("SELECT r_turn FROM game_rooms WHERE r_id="+Game.Room.ID);
                if(rt.next())          
                {
                    Game.Room.PlayTurn=rt.getInt("r_turn");                    
                    return rt.getInt("r_turn");                      
                }
                else
                    return -1;

            }catch(SQLException e)
            {
                return -1;
            }
            
        }
        else
            return -1;
    } //end getTurn()
    
    
    //Sıra değiştir
    public boolean setTurn(int playerId){
        if(IsConnected  && roomExists() && opponentExists())
        {
            if(Game.Room.PlayTurn==Game.GamePlayer.ID)
            {
                try{
                    String query="UPDATE game_rooms SET r_turn="+playerId+" WHERE r_id="+Game.Room.ID+" AND ";
                    if(Game.GamePlayer.host)
                        query+="r_p1="+Game.GamePlayer.ID+" AND r_p2="+Game.Room.Opponent.ID;
                    else
                        query+="r_p1="+Game.Room.Opponent.ID+" AND r_p2="+Game.GamePlayer.ID;
                    
                    Game.Room.PlayTurn=playerId;
                    if(Connection.createStatement().executeUpdate(query)>0)
                        return true;
                    else
                        return false;
                    
                
                }catch(SQLException e)
                {
                    return false;
                }
            
            } //Sıra oyuncuda değil
            else{
                return false;
            }
            
        }
        else
            return false;
    }//end setTurn()
    
    
    
    //Grid güncelle
    public void UpdateGrid(Grid g)
    {
        if(IsConnected  && roomExists() && opponentExists())
        {
            String query="UPDATE game_grids SET ";
            query+="g_durum="+g.durum;
            query+=",g_posX="+g.posX;
            query+=",g_posY="+g.posY;
            if(g.owner!=null)
                query+=",g_owner="+g.owner.ID;
            else
                query+=",g_owner=0";
            
            query+=" WHERE g_roomId="+Game.Room.ID+" AND g_inGameId="+g.ID;
        
            try {
                Connection.createStatement().executeUpdate(query);
            } catch (SQLException ex) {
                System.err.println("Update Hatası [x01]");
            }
        
        }
    }//end UpdateGrid()
    
    
    //Son online update
    public void UpdateLastOnline(){
        if(IsConnected  && roomExists())
        {            
            String query="UPDATE game_players SET p_lastOnline='"+getCurrentDate()+"'";
            query+=" WHERE p_id="+Game.GamePlayer.ID;
            try {
                Connection.createStatement().executeUpdate(query);
            } catch (SQLException ex) {}
            
        }
    }//end UpdateLastOnline()
    
    
    //Taş sayısını servera yükle
    public void UpdateTasSayisi(){
        if(IsConnected  && roomExists() && opponentExists())
        {            
            String query="UPDATE game_rooms SET ";
            if(Game.GamePlayer.host)
                query+="r_p2_tas="+Game.Room.Opponent.tasSayisi;
            else
                query+="r_p1_tas="+Game.Room.Opponent.tasSayisi;            
            
            
            query+=" WHERE r_id="+Game.Room.ID;
            
            try {
                Connection.createStatement().executeUpdate(query);
            } catch (SQLException ex) {}
            
        }
    }//end UpdateTasSayisi()
    
    
    //Taş sayılarını servera göre güncelle
    public void getTasSayisi(){
        if(IsConnected  && roomExists() && opponentExists())
        {
            String query="SELECT r_p1_tas, r_p2_tas FROM game_rooms WHERE r_id="+Game.Room.ID;
            
            try {
                ResultSet rts=Connection.createStatement().executeQuery(query);
                
                if(rts.next() && roomExists())
                {
                    if(Game.GamePlayer.host)
                    {
                        Game.GamePlayer.tasSayisi=rts.getInt("r_p1_tas");
                        Game.Room.Opponent.tasSayisi=rts.getInt("r_p2_tas");
                    }
                    else
                    {
                        Game.GamePlayer.tasSayisi=rts.getInt("r_p2_tas");
                        Game.Room.Opponent.tasSayisi=rts.getInt("r_p1_tas");
                    }
                }
                
            } catch (SQLException ex) {}
            
            
        }
    }//end getTasSayisi()
    
    
    
    
    public boolean roomExists(){
        if(IsConnected && Game.Room!=null)
        {
            ResultSet game;
            try {

                game = Connection.createStatement().executeQuery("SELECT * FROM game_rooms WHERE r_id="+Game.Room.ID);
                if(game.next())
                    return true;
                else
                    return false;


            } catch (SQLException ex) {
                return false;
            }
        }
        else
            return false;
    }
    
    
    
    public boolean opponentExists(){
        if(IsConnected && Game.Room!=null)
        {
            ResultSet game;
            try {

                game = Connection.createStatement().executeQuery("SELECT * FROM game_rooms WHERE r_id="+Game.Room.ID);
                if(game.next())
                {
                    if(game.getInt("r_p2")!=-1)
                    {
                        if(Game.Room.Opponent==null)
                        {
                            int opid=game.getInt("r_p2");
                            game.close();
                            
                            
                            ResultSet oprs=Connection.createStatement().executeQuery("SELECT * FROM game_players WHERE p_id="+opid);                            
                            if(oprs.next()){
                                Game.Room.Opponent=new Player(oprs.getString("p_ad"),oprs.getInt("p_id")); 
                                Game.Room.startGame();
                                GetGridList();
                                GetGameData();                            
                                
                            }
                        }
                                    
                        
                        return true;
                    }
                    else
                        return false;
                }
                else
                    return false;


            } catch (SQLException ex) {
                System.out.println("err update opponent: "+ex.getMessage());
                return false;
            }
            
            
        }
        else
            return false;
    
    }
    
    
    
    //Data Çek
    public void GetGameData(){
        if(IsConnected)
        {           
                if(roomExists())
                {
                    UpdateLastOnline();
                    
                    getTasSayisi();
                    getLastMoved();

                    if(Controller_Ingame.GameLogic.islenen==null)
                        GetGridList();

                    CheckWinner();        
                    System.out.println("GameData Güncellendi.");                    
                                    
                }
                else
                {
                    if(Game.GamePlayer.tasSayisi>Game.Room.Opponent.tasSayisi)
                        JOptionPane.showMessageDialog(null, "Oyun Bitti, siz kazandınız "+Game.GamePlayer.name+" !!");
                    else if(Game.GamePlayer.tasSayisi<Game.Room.Opponent.tasSayisi)
                        JOptionPane.showMessageDialog(null, "Oyun Bitti, KAYBETTİN ! ");
                    else
                        JOptionPane.showMessageDialog(null, "Oyun Bitti.");
                    
                    
                    Game.GameController=new Controller_Lobby();
                    Game.Room=null;

                    Game.UpdateFrame();
                }
            
        
            
        }
    }//end GetGameData()
    
    
    //Tarihi string olarak al
    public String getCurrentDate(){
        Date d=new Date();
        return df.format(d);        
    }//end getCurrentDate();
    
    
    
    //Kazanan kontrol
    public void CheckWinner(){
        if(IsConnected && roomExists() && opponentExists())
        {
            
            try {
                ResultSet rs=Connection.createStatement().executeQuery("SELECT r_winner FROM game_rooms WHERE r_id="+Game.Room.ID);
            
                if(rs.next())
                {
                    int winner=rs.getInt("r_winner");
                    if(winner==-1)
                    {
                    
                        if(Game.Room.Opponent.tasSayisi==0)
                        {
                            Game.Room.Winner=Game.GamePlayer.ID;
                            Connection.createStatement().executeUpdate("UPDATE game_rooms SET r_winner="+Game.Room.Winner+" WHERE r_id="+Game.Room.ID);

                            
                            deleteRoom();
                            JOptionPane.showMessageDialog(null, "Oyun Bitti, Siz Kazandınız "+Game.GamePlayer.name+" !");
                            
                        }
                        else if(Game.GamePlayer.tasSayisi==0)
                        {
                            Game.Room.Winner=Game.Room.Opponent.ID;
                            Connection.createStatement().executeUpdate("UPDATE game_rooms SET r_winner="+Game.Room.Winner+" WHERE r_id="+Game.Room.ID);


                            deleteRoom();
                            
                            JOptionPane.showMessageDialog(null, "Oyun Bitti, KAYBETTİN!!! ");
                        }
                        
                    
                    }
                    else //winner -1 değil
                    {
                        if(winner==Game.GamePlayer.ID)
                        {
                            deleteRoom();                            
                            JOptionPane.showMessageDialog(null, "Oyun Bitti, Siz Kazandınız "+Game.GamePlayer.name+" !");
                        }
                        else if(winner==Game.Room.Opponent.ID)
                        {

                            deleteRoom();
                            JOptionPane.showMessageDialog(null, "Oyun Bitti, KAYBETTİN! ");
                            
                        }
                    }
                
                
                }
            
            
            } catch (SQLException ex) {}

            
        }
    }//end CheckWinner
    
    
    
    public void pesEt(){
        if(IsConnected && roomExists())
        {
            if(Game.Room!=null && Game.Room.Opponent!=null)
            {
                try {
                    Connection.createStatement().executeUpdate("UPDATE game_rooms SET r_winner="+Game.Room.Opponent.ID+" WHERE r_id="+Game.Room.ID);
                    CheckWinner();
                } catch (SQLException ex) {
                    System.out.println("Pes et sql error");
                }
                
            }       
            
        }
    } //End pesEt()
    
    
    
    public void deleteRoom(){
        if(IsConnected  && roomExists())
        {
            if(Game.Room!=null)
            {
                try {
                    Connection.createStatement().executeUpdate("DELETE FROM game_grids WHERE g_roomId="+Game.Room.ID);
                    Connection.createStatement().executeUpdate("DELETE FROM game_rooms WHERE r_id="+Game.Room.ID);
                    Game.GameController=new Controller_Lobby();
                    Game.Room=null;


                    Game.UpdateFrame();
                    
                } catch (SQLException ex) {
                    System.out.println("Room delete sql error");
                }

            }        
        }    
    }//end deleteRoom()
    
    
    
    public void deleteRoom(int rid){
        if(IsConnected)
        {            
            try {
                Connection.createStatement().executeUpdate("DELETE FROM game_grids WHERE g_roomId="+rid);
                Connection.createStatement().executeUpdate("DELETE FROM game_rooms WHERE r_id="+rid);
                
                Game.GameController=new Controller_Lobby();
                Game.Room=null;
                
                Game.UpdateFrame();

            } catch (SQLException ex) {

            }
                   
        }    
    }//end deleteRoom(int rid)
    
    
    
    public void addGrid(int roomId, int owner, int durum, int posX, int posY, int inRoomId){
        if(IsConnected)
        {
            try {
                PreparedStatement ps=Connection.prepareStatement("INSERT INTO game_grids(g_owner,g_durum,g_posX,g_posY,g_roomId,g_inGameId) VALUES (?,?,?,?,?,?)");
                ps.setInt(1,owner);
                ps.setInt(2,durum);
                ps.setInt(3,posX);
                ps.setInt(4,posY);
                ps.setInt(5,roomId);
                ps.setInt(6,inRoomId);
                
                ps.executeUpdate();
            
            
            } catch (SQLException ex) {
                System.out.println("Grid oluşturma hatası: "+roomId+" -> "+inRoomId+" | "+posX+","+posY);
            }
        }
    }
    
    
        
    
}
