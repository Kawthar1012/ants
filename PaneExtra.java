import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Donne la possibilité à l'utilisateur de placer lui-même des éléments
 */
public class PaneExtra extends JPanel implements PanesInterface{

    /**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;

    private final JRadioButton pictured;
    private final JRadioButton pherNid, pherNour;

    public PaneExtra(int x, int y, int w, int h){
        //init
        Font ref = new Font("Georgia", Font.BOLD, 12);
        this.pictured = new JRadioButton("Pictured Mode");
        this.pherNid = new JRadioButton("Nest view");
        this.pherNour = new JRadioButton("Food view");
        ButtonGroup group = new ButtonGroup();

        //set
        this.pictured.setSelected(true); //coche le radio
        this.pictured.setFont(ref);
        this.pherNid.setFont(ref);
        this.pherNour.setFont(ref);
        setLayout(new GridLayout(3,0));

        //linked
        group.add(pictured); group.add(pherNid); group.add(pherNour);
        add(pictured); add(pherNid); add(pherNour);

        setBounds(x, y, w, h);
    }

    /**
     * Renvoie True si on veut voir en affichage graphique
     * @return booelan
     */
    public boolean getState(){
        return pictured.isSelected(); //si cocher
    }
    
    /**
     * 
     * @param pic  ActionListener
     * @param color  ActionListener
     */
    public void setRadiosListener(ActionListener pic,ActionListener nestview,  ActionListener foodview){
        this.pictured.addActionListener(pic);
        this.pherNour.addActionListener(foodview);
        this.pherNid.addActionListener(nestview);
    }

}