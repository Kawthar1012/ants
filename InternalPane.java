import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Cette classe sera présenter lors se l'appui du bon composent dans le JMenu
 * Elle permettra de lancer une nouvelle simulation et d'entrer les bonnes informations
 * la fonction setVisible permettra de le faire appraître
 */
public class InternalPane extends JInternalFrame{

    /**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;
    public static final int LARGEUR = 300, LONGUEUR = 350; 

    private final JLabel[] names;
    private final JTextField[] fields;
    private final JButton accept, annuler;

    public InternalPane(){
        setBounds(Fenetre.LARGEUR/5,Fenetre.LONGUEUR/6,LARGEUR+10,LONGUEUR);
        setResizable(false);
        setLayout(null);
        Container pane = getContentPane();
        pane.setLayout(null);
        
        //init
        names = new JLabel[5];
        fields = new JTextField[5];
        accept = new JButton("Valider");
        annuler = new JButton("Annuler");
        
        //conf   
        String[] nameStrings = new String[]{"Largeur","Longueur ", "Nombres d'obstacles",
            "Nombre de fourmis ", "Nombre de Nourriture"};
        for(int i=0; i<names.length;i++){
            names[i] = new JLabel(nameStrings[i]);
            if(i==2){fields[i] = new JTextField("1800");}
            else if(i==4){fields[i] = new JTextField("6");}
            else {fields[i] = new JTextField("50");}
            names[i].setBounds(0,i*LONGUEUR/10, LARGEUR*2/3, LONGUEUR/10);
            fields[i].setBounds(LARGEUR*2/3, i*LONGUEUR/10, LARGEUR/3, LONGUEUR/10);
            pane.add(names[i]);  
            pane.add(fields[i]);
            
        }
        annuler.setBounds(LARGEUR/3,names.length*LONGUEUR/10, LARGEUR/3, LONGUEUR/10);
        accept.setBounds(LARGEUR*2/3, names.length*LONGUEUR/10, LARGEUR/3, LONGUEUR/10);
        pane.add(annuler);      
        pane.add(accept);
        annuler.addActionListener(e->{setVisible(false);});

    }

    /**
     * permet d'actionner le {@code JButton} accept
     * @param l ActionListener
     */
    public void confAcceptListener(ActionListener l){
        accept.addActionListener(l);
    }

    /**
     * Renvoie true si tout les informations sont corrects
     * @return boolean
     */
    public boolean isCorrect(){
        boolean correct = true;
        for(int i=0;i<fields.length;i++){
            int val =-99; //error val
            try{
                val = Integer.parseInt(fields[i].getText());
            }catch (NumberFormatException exp){

            }
            if(val<0){
                correct=false;
            }else if(i==(0|1|3)&&val==0){
                correct=false;
            }
        }
        return correct;
    }

    /**
     * Donne le couple (clé,valeur) des éléments pris en arguments
     * @return LinkedHashMap
     */
    public LinkedHashMap<String,Integer> getValuesEntry(){
        LinkedHashMap<String,Integer> res = new LinkedHashMap<>();
        for (int i=0;i<names.length;i++){
            res.put(names[i].getText(),Integer.parseInt(fields[i].getText()));
        }
        return res;
    }

    public int getValuesSize(){
        return fields.length;
    }

}