import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

//import javafx.scene.shape.HLineTo;

public class Pinceau extends JPanel {

    private static final long serialVersionUID = 3269040576202312270L;

    private final JTextField [] taille;

    private JSlider taillePixels;

    private final JTextField nbFourmis;

    private JRadioButton [] choix;

    private final JButton valider, applyButton;

    public Pinceau (int x, int y, int w, int h){
        setBounds(x,y,w,h);
        setLayout(new GridLayout(9,1));

        JPanel dim = new JPanel(new GridLayout(1,3));
        dim.setBounds(x,y,w,h/9);
        this.taille = new JTextField[2];
        this.taille[0] = new JTextField(""+SandBox.INIT_VAL);
        this.taille[1] = new JTextField(""+SandBox.INIT_VAL);
        this.applyButton = new JButton("Generate");
        dim.add(taille[0]); dim.add(taille[1]); dim.add(applyButton);

        this.add(dim);
        this.add(new JLabel("Choix de pinceau"));
        this.taillePixels = new JSlider(0,4,1);
        this.taillePixels.setName("Taille du pinceau");
        this.taillePixels.setMajorTickSpacing(1);
        this.taillePixels.setPaintTicks(true);
        this.add(this.taillePixels);

        this.choix = new JRadioButton[5];
        this.choix[0] = new JRadioButton("Chemin");
        this.choix[1] = new JRadioButton("Block");
        this.choix[2] = new JRadioButton("Fourmiliere");
        this.choix[3] = new JRadioButton("Nourriture");
        this.choix[4] = new JRadioButton("Fourmi");
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < this.choix.length; i++) {
            this.add(this.choix[i]);
            group.add(this.choix[i]);
        }
        this.choix[0].setSelected(true);//default
        this.valider = new JButton("Valider");
        this.nbFourmis = new JTextField("10"); //default 
        dim = new JPanel(new GridLayout(1,3));
        dim.add(new JLabel("Nb Fourmis :")); dim.add(nbFourmis);dim.add(valider);
        dim.setBounds(x,(8*h)/9,w,h/9);
        this.add(dim);
    }

    public int getHauteur() {
        int h = 20;
        try {
            h = Integer.parseInt(this.taille[0].getText());
        } catch (NumberFormatException e) {
            System.err.println(this.taille[0].getText()+" is not a number");
        }
        h = (Math.abs(h)>100)?100:h;
        this.taille[0].setText(""+h);
        return h;
    }

    public int getLargeur(){
        int l = 30;
        try {
            l = Integer.parseInt(this.taille[1].getText());
        } catch (NumberFormatException e) {
            System.err.println(this.taille[1].getText()+" is not a number");
        }
        l = (Math.abs(l)>100)?100:l;
        this.taille[1].setText(""+l);
        return l;
    }
    public int getNbFourmi(){
        int nbf = 50;
        try {
            nbf = Integer.parseInt(this.nbFourmis.getText());
        } catch (NumberFormatException e) {
            System.err.println(this.nbFourmis.getText()+" is not a number");
        }
        nbf = (Math.abs(nbf)>200)?100:nbf;
        this.nbFourmis.setText(""+nbf);
        return nbf;
    }

    public void addValidDrawListener(ActionListener l){
        this.valider.addActionListener(l);
    }

    public void addApplyDrawListener(ActionListener l){
        this.applyButton.addActionListener(l);
    }

    public boolean estChemin(){
        return getChoix(0);
    }
    public boolean estBlock(){
        return getChoix(1);
    }
    public boolean estFourmiliere(){
        return getChoix(2);
    }
    public boolean estNourriture(){
        return getChoix(3);
    }
    
    public boolean estFourmi(){
        return getChoix(4);
    }

    private boolean getChoix (int i) {
        return this.choix[i].isSelected();
    }


    public int getTaillePixel () {
        return this.taillePixels.getValue();
    }

    public void setTaillePixel (int v) {
        this.taillePixels.setValue(v);
    }


}