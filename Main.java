import java.awt.EventQueue;

public class Main{
    public static void main(String[] args){
        //javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
        EventQueue.invokeLater(() ->{
            new Controleur();
        });
        
    }
}