import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
/**
 * Notre bac à sable
 */
public class SandBox extends JPanel {

    private static final long serialVersionUID = 3269040576202312270L;
    public static final int INIT_VAL = 30; //cote x cote

    /**
     * Les données seronts fournis sous formes de coordonnée x,y
     * 0 = block
     * 1 = chemin
     * 2 = nourriture
     * 3 = fourmi
     * 4 = fourmiliere
     * [y][x]
     */
    private int[][] data;
    private Point fourmiliere;//unique

    private final Pinceau pinceau;
    
    public SandBox(){
        setBounds(0,50,Fenetre.LARGEUR,Fenetre.LONGUEUR-50);
        setLayout(null);
        this.pinceau =new Pinceau(SandBox.this.getWidth()*3/4,0,SandBox.this.getWidth()/4,(SandBox.this.getHeight()*2)/3);
        Tableau ardoise = new Tableau();
        ajouterEdansT(this, ardoise, this.pinceau);     
        this.data = new int[INIT_VAL][INIT_VAL];
        for(int i=0;i<INIT_VAL;i++){
            for(int j=0;j<INIT_VAL;j++){
                data[i][j] = 0;
            }
        }
        pinceau.addApplyDrawListener(l->{
            data = new int[pinceau.getHauteur()][pinceau.getLargeur()];
            fourmiliere = null;
            ardoise.repaint();
            repaint();
        });
    }

    /**
     * Les données seronts fournis sous formes de coordonnée x,y
     * largeur = data[0].length ; longueur = data.length ; 
     * 0 = block
     * 1 = chemin
     * 2 = nourriture
     * 3 = fourmi
     * 4 = fourmiliere
     * [y][x]
     * @return int[][]
     */
    public int[][] getTabofLab(){
        return data.clone();
    }

    /**
     * Le nombre de fourmi dans la fourmillière
     * @return int
     */
    public int getNbFourmi(){
        return pinceau.getNbFourmi();
    }

    public boolean estUn(int x, int y, int val){
        return estCorrect(x, y) && data[y][x] == val;
    }

    public boolean aUneFourmiliere(){
        return fourmiliere!=null;
    }

    public boolean estCorrect(int x,int y){
        return x>=0 && y>=0 && x<data[0].length && y<data.length;
    }

    public void addValidDrawListener(ActionListener l){
        pinceau.addValidDrawListener(l);
    }

    private void setFourmiliere(int x, int y){
        if(estUn(x,y,1)){ //if chemin
            if(fourmiliere == null){
                fourmiliere = new Point(x,y);
                setFourmiliere(x,y);
            }else{
                data[fourmiliere.y][fourmiliere.x]=1; //supprimer l'ancien
                fourmiliere.setLocation(x, y);
                data[fourmiliere.y][fourmiliere.x]=4; //ajout nouveau
            }
        }
    }


    private void addFourmi(int x, int y){
        if(estCorrect(x, y)&&estUn(x, y, 1)){
            data[y][x] = 3;
        }
    }

    private void addNourriture(int x, int y){
        if(estCorrect(x, y)&&estUn(x, y, 1)){
            data[y][x] = 2;
        }
    }
  
    private void tracer(boolean chemin, int taille,int x, int y){
        int val = (chemin)?1:0;
        if(taille==0){
            data[y][x] = val;
        }else{
            for(int i=y-taille;i<y+taille;i++){ //y
                for(int j=x-taille;j<x+taille;j++){ //x
                    if(estCorrect(j,i)){
                        if(data[i][j]==4&&!chemin){ //block
                            data[i][j] = val; 
                            fourmiliere=null; //fmlr
                        }else if(data[i][j]!=4) data[i][j] = val;
                    }
                }
            }
        }
    }

    /**
     * Permet d'ajouter beaucoup de {@code JComponent} dans une seule
     * @param target JComponent
     * @param elements JComponent[]
     */
    private void ajouterEdansT(JComponent target,JComponent... elements){
        for (int i =0; i<elements.length;i++){
            target.add(elements[i]);
        }
    }

    /**
     * Permet au Controleur de configurer le boutton valider
     * @param l ActionListener
     */
    public void addValiderAcListener(ActionListener l){
        pinceau.addValidDrawListener(l);
    }

    private final class Tableau extends JPanel implements PanesInterface{
        private static final long serialVersionUID = 3269040576202312270L;
        private final int largeur,longueur;
        private final Comportement comp = new Comportement(){
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX()/(largeur/data[0].length) , y = e.getY()/(longueur/data.length);

                if(pinceau.estChemin()){ tracer(true,pinceau.getTaillePixel(),x,y);  }
                if(pinceau.estBlock()){ tracer(false,pinceau.getTaillePixel(),x,y);  }
                          
                repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX()/(largeur/data[0].length) , y = e.getY()/(longueur/data.length);

                if(pinceau.estChemin()){ tracer(true,pinceau.getTaillePixel(),x,y);  }
                if(pinceau.estBlock()){ tracer(false,pinceau.getTaillePixel(),x,y);  }

                if(pinceau.estFourmiliere ()){ setFourmiliere(x, y); }
                if(pinceau.estNourriture()){ addNourriture(x, y); }
                if(pinceau.estFourmi()){  addFourmi(x,y);  }    
                repaint();
            }

        };

        Tableau(){
            largeur = SandBox.this.getWidth()*3/4;
            longueur = SandBox.this.getHeight()-40;
            setBounds(0,0,largeur,longueur+40);
            configureListener();
        }
        @Override
        public void paint(Graphics g){
            Point pixel = new Point(largeur/data[0].length, longueur/data.length);
            for(int y = 0;y<data.length;y++){ //y
                for(int x=0;x<data[0].length;x++){ //x
                    switch (data[y][x]){
                        case 0 : g.setColor(Color.BLACK);break;
                        case 1 : g.setColor(Color.WHITE);break;
                        case 2 : g.setColor(Color.YELLOW);break;
                        case 3 : g.setColor(Color.RED);break;
                        case 4 : g.setColor(Color.BLUE);break;
                        default : g.setColor(Color.BLACK);break;
                    }
                    g.fillRect(x*pixel.x, y*pixel.y, pixel.x, pixel.y); //draw
                    g.setColor(Color.CYAN);
                    g.drawRect(x*pixel.x, y*pixel.y, pixel.x, pixel.y);
                }
            }
        }
        
        @Override
        public void configureListener() {
            addMouseMotionListener((MouseMotionListener)comp);
            addMouseListener((MouseListener)comp);
        }
    }
}