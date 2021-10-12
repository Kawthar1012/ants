import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Ici sera mis les bouttons qui lancera la simulation
 */
public class PaneRunner extends JPanel implements PanesInterface{

    private static final long serialVersionUID = 3269040576202312270L;

    private Integer nbTour;

    private final JLabel compteur;

    private JButton start;

    private JButton restart;

    private JButton stop;

    /**True quand la simulation s'opère, False sinon */
    private boolean etat;

    public PaneRunner (int x, int y, int w, int h) {
        setBounds(x, y, w, h);
        setLayout(null);
        JPanel up = new JPanel(new GridLayout(1,2)), down = new JPanel(new GridLayout(1,3));
        up.setBounds(0, 0, w, h/3);
        down.setBounds(0,h/3,w,2*h/3);
        add(up); add(down);
        nbTour = 0; etat = false;
        Image curr = null;
        try{
            curr = ImageIO.read(new File("./Images/play.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            this.start = new JButton(new ImageIcon(curr)); 

            curr = ImageIO.read(new File("./Images/pause.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            this.stop = new JButton(new ImageIcon(curr)); 

            curr = ImageIO.read(new File("./Images/restart.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);;
            this.restart = new JButton(new ImageIcon(curr)); 

        }catch (IOException e){
            e.printStackTrace(System.err);
        }
        this.compteur = new JLabel(garderForme());
        up.add(new JLabel("Tours :")); up.add(compteur); 
        down.add(restart); down.add(start);       
        down.add(stop);        
        
        configureListener();
    }

    public void addResetListener(ActionListener l){
        this.restart.addActionListener(l);
    }

    public boolean isRunning(){
        return this.etat;
    }

    /**
     * Incrémente le nombre de Tour
     */
    public void unTour(){
        this.nbTour++;
        this.compteur.setText(garderForme()); 
        this.compteur.repaint();
    }

    /**
     * Remet le compteur à zero
     */
    public void restartCount(){
        this.nbTour = 0;
        this.compteur.setText(garderForme()); 
        this.compteur.repaint();
    }

    private String garderForme(){
        if(this.nbTour==null){ return "000000";}
        String res = ""+this.nbTour;
        for(int i = res.length();i<=6;i++){
            res = "0"+res;
        }
        return res;

    }

    @Override
    public void configureListener() {
        this.start.addActionListener(event->{
            etat = true; 
            System.out.println("\nLancement de la simulation...");
        });
        this.stop.addActionListener(event->{
            etat = false;
            System.out.println("Mise en pause de la simulation avec "+nbTour+" tours effectués;" ); 
        });
    }

}