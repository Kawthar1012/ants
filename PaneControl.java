import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

/**
 * Donnera des données utiles pour les simulations
 */
public class PaneControl extends JPanel implements PanesInterface{
    /**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;
    public final static int DEFAULTZOOM = 3;
    
    /**
     * Les choix d'affichages de départ
     */
    
    private final JSlider vitesseSimul;
    private final JSlider zoom;

    public PaneControl(int x, int y, int w, int h){
        setBounds(x,y,w,h);
        setLayout(null);

        //init
        this.vitesseSimul = new JSlider(5,40,20);
        this.zoom = new JSlider(1,6,DEFAULTZOOM);

        this.vitesseSimul.setMajorTickSpacing(5);
        this.vitesseSimul.setPaintTicks(true);
        this.vitesseSimul.setPaintLabels(true);

        this.zoom.setMajorTickSpacing(1);
        this.zoom.setPaintTicks(true);

        JPanel g = new JPanel(), d = new JPanel();
        g.setBounds(0, 0, w/3, h);  d.setBounds(w/3, 0, w*23/40, h);
        g.setLayout(new GridLayout(2,1)); d.setLayout(new GridLayout(2,1));

        //add
        this.add(g); this.add(d);
        g.add(new JLabel("Vitesse de simulation :"));
        d.add(this.vitesseSimul);
        g.add(new JLabel("Niveau du zoom :"));
        d.add(this.zoom);

    }

    public int getVitesse(){
        return (vitesseSimul.getMaximum()+vitesseSimul.getMinimum()-vitesseSimul.getValue())*2; //regulateur
    }

    public int getZoomLevel(){
        return zoom.getValue();
    }

    public void confZoomListener(ChangeListener l){
        zoom.addChangeListener(l);
    }

}