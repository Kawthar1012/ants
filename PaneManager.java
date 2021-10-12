import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;


public class PaneManager extends JPanel implements PanesInterface {

    private static final long serialVersionUID = 3269040576202312270L;

    public static final int DEFAULTZOOM = 4;

    private final JButton zoomPlus, zoomMoin;

    private final JLabel infoZoom;

    private final JButton actualiser;

    private JTextField[] config; 

    private int zoomScale;

    public PaneManager (int x, int y, int w, int h) {
        setBounds(x,y,w,h);
        //init
        Font ref = new Font("Arial", Font.BOLD, 26);
        this.zoomScale = DEFAULTZOOM;
        this.zoomMoin = new JButton();
        this.zoomPlus = new JButton();
        this.infoZoom = new JLabel("    "+(zoomScale-1)+"x");
        this.infoZoom.setFont(ref);
        this.actualiser = new JButton(); 
        this.config = new JTextField [3];
        JLabel labZ = new JLabel("Zoom:");   labZ.setFont(ref);

        try {
            Image sta = ImageIO.read(new File("./Images/refresh.png"));
            this.actualiser.setIcon(new ImageIcon(sta));
            Image plus = ImageIO.read(new File("./Images/plus.png"));
            this.zoomPlus.setIcon(new ImageIcon(plus));
            Image moins = ImageIO.read(new File("./Images/moins.png"));
            this.zoomMoin.setIcon(new ImageIcon(moins));
        } catch (IOException e) {
            System.out.println("Erreur dans la classe PaneManager, au niveau des images");
        }

        config[0] = new JTextField("Longueur");
        config[1] = new JTextField("Largeur");
        config[2] = new JTextField("Nbre obstacle");
        //add
        setLayout(new GridLayout(2,4));
        add(labZ); add(zoomPlus); add(infoZoom); add(zoomMoin); 
        add(config[0]);add(config[1]);add(config[2]); add(actualiser);
       
        configureListener();
    }
    /**
     * Donne accès à l'entier qui diminue/augmente après le toucher
     * @return int
     */
    public int getScaleZoom(){
        return this.zoomScale;
    }

    /**
     * Permet de reguler les changements du zoom +/-
     * @param plus boolean
     * @return boolean
     */
    public boolean setZoom(boolean plus){
        boolean fait = false;
        if(zoomScale+1<7&&plus){
            zoomScale++;    fait = true;
        }else if(zoomScale-1>1&&!plus){ //On ne prend pas 1 comme valeur
            zoomScale--;    fait = true;
        }
        if(fait){
            infoZoom.setText("    "+(zoomScale-1)+"x");
            infoZoom.repaint();
        }
        return fait;
    }

    /**
     * Permet de configurer la touche lancement
     * @param l {@code ActionListener}
     */
    public void eventRefresh(ActionListener l){
        this.actualiser.addActionListener(l);
    }

    /**
     * Permet de donner des attributs
     * @param plus {@code ActionListener}
     * @param moin {@code ActionListener}
     */
    public void setZoomActionListener(ActionListener plus, ActionListener moin){
        this.zoomPlus.addActionListener(plus);
        this.zoomMoin.addActionListener(moin);
    }

    /**
     * Permet d'obtenir un tableau d'entier qui permet d'avoir de nouveaux labyrinthe
     * @return int[] taille 3
     */
    public int[] getNewLabValues(){
        int[] res = new int[3]; int c = 0;
        for(JTextField j : this.config){
            int sol =(c==2)?350:30; //default value
            try{
                sol = Integer.parseInt(j.getText());
                sol = (sol<20&&c!=2)?20:sol; //minimum
                sol = (sol>120&&c!=2)?120:sol; //max
            }catch(NumberFormatException e){

            }
            res[c] = sol; this.config[c].setText(""+sol);c++; //rester cohérant
        }
        System.out.println("\n[Creation] Labyrinthe avec commme configuration :\n"
            +"\tLongueur : "+res[1]+"\n\tLargeur : "
            +res[0]+"\n\tNombre d'obstacle : "+res[2]);
        return res;
    }

    @Override
    public void configureListener() {

    }

}