import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * Affiche en taille réduite l'ensemble des éléments du tableau 
 */
public class MiniMap extends JPanel implements PanesInterface{

    /**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;
    
    /**
     * Ici les changement respectent la taille du Labyrinthe
     */
    protected class Zone {

        private int x,y,width,heigth;

        /**
         * 
         * @param x int position
         * @param y int position
         * @param w int distance
         * @param h int distance
         */
        private Zone(int x,int y, int w, int h){
            this.x = x; this.y = y;
            this.width = w; this.heigth = h;
        }
        
        public int getX(){ return x;}
        public int getY(){ return y;}
        public int getWidth(){ return width;}
        public int getHeight(){ return heigth;}

        /**
         * <p>Met à jours les informations de la Zone encadrer</p>
         * @param x int 
         * @param y int
         * @param w int
         * @param h int
         * @throws IllegalArgumentException
         */
        public void changeInfo(int x,int y, int w, int h) throws IllegalArgumentException{
            //verification des paramètres
            if((x|y|w|h)>=0){
                this.x =(w+x)>=modele.getLargeur()?modele.getLargeur()-w:x; 
                this.y =(y+h)>=modele.getHauteur()?modele.getHauteur()-h:y;
                this.width = w; this.heigth = h;
            } else throw new IllegalArgumentException();
        }
        @Override
        public String toString(){
            return "[x="+x+",y="+y+",x->width="+(getWidth()+x)+",y->heigth="+(getHeight()+y)+"]";
        }
    }

    /**
     * Coeficient multiplicateur de la {@code MiniMap} en {@code BigMap}
     */
    public int coefSize = PaneControl.DEFAULTZOOM;
    /**
     * S'affiche quand les données sont pas exploitablées
     */
    private final Color error = Color.RED;
    
    /**
     * Un lien de partage avec le labyrinthe principal
     */
    private final Labyrinthe modele;
    /**
     * Donne l'emplacement du rectangle dessiné sur le mini-carte
     */
    private final Zone souris; 

    private final int largeur, longueur;

    /**
     * {@code m} Devrait être un pointeur d'un labyrithe déjà initialiser 
     * @param x int 
     * @param y int
     * @param w int 
     * @param h int
     * @param m Labyrinthe
     */
    public MiniMap(int x, int y, int w, int h, Labyrinthe m){
        this.largeur = w; this.longueur = h;
        this.modele = m;
        this.souris = new Zone(0,0,modele.getLargeur()/coefSize,modele.getHauteur()/coefSize); //val init
        setBackground(error);
        setBounds(x,y,w,h);
        configureListener(); getWidth();
    }
  

    protected Labyrinthe getLabyrinthe(){
        return this.modele;
    }

    public int getLargeur() {
        return this.largeur;
    }


    public int getLongueur() {
        return this.longueur;
    }

    /**
     * 
     * @return Permet à {@code PaneManager} de savoir le niveau de Zoom
     */
    public int getZoomScale(){
        return this.coefSize;
    }

    public void setZoomScale(int scale){
        this.coefSize = scale;
    }

    /**
     * Permet d'élargir ou réduire les champs de la sourie
     * Mini d'un <strong>repaint()</strong>
     * @param w int 
     * @param h int
     */
    public void setSourisWH(int w, int h){
        this.souris.changeInfo(souris.getX(), souris.getY(), w, h);
        repaint();
    }

    /**
     * A appeler après la génération d'une nouvelle grille de labyrinthe
     */
    public void resetEmplSouris(){
        this.souris.changeInfo(0,0,modele.getLargeur()/coefSize,modele.getHauteur()/coefSize);
        repaint();
    }

      /**
     * Avec un {@code Mouselistener} il permettra de donner la zone ciblé par la sourie en un clic
     * @return {@code MiniMap.Zone} une copie
     */
    protected Zone getEmplSourie(){
        return new Zone(this.souris.x,this.souris.y,this.souris.width,this.souris.heigth);
    }

    /**
     * Coordonne la taille des pixels
     * @return int
     */
    private int xBitsSize(){
        return this.largeur/this.modele.getLargeur();
    }

    /**
     * Coordonne la taille des pixels
     * @return int
     */
    private int yBitsSize(){
        return this.longueur/this.modele.getHauteur();
    }

    @Override
    public void paint(Graphics g){
        //Organiser la taille des pixels
        
        for(int x=0; x<this.modele.getLargeur();x++){
            for(int y =0; y<this.modele.getHauteur();y++){
                //B:obstacle; V:vide; N: Nourriture; F:Fourmi; FM:Fourmilière
                switch (this.modele.getCell(x, y).toString()){//choix de la couleur
                    case "B": g.setColor(Color.BLACK); break;
                    case "V" : g.setColor(Color.lightGray); break;
                    case "N": g.setColor(new Color(0,Math.abs((255-this.modele.getCell(x, y).getObstacle()/4)%255),255)); break;
                    case "FM" : g.setColor(new Color(0,100,0));
                }
                g.fillRect(x*xBitsSize(), y*yBitsSize(), xBitsSize(), yBitsSize());
            }
        }
        
        for (Labyrinthe.CoorFourmi coor:modele.getcoordonnee()){
            if(coor.typeferomone==0){
                g.setColor(Color.RED);
            }else{
                g.setColor(new Color(138,43,226));
            }
            g.fillRoundRect(coor.x*xBitsSize(), coor.y*yBitsSize(), xBitsSize(), yBitsSize(),15,5);
        }
        g.setColor(new Color(230,10,10));
        g.drawRect(souris.getX()*xBitsSize(),souris.getY()*yBitsSize(),souris.width*xBitsSize(),souris.heigth*yBitsSize());

    }

    @Override
    public void configureListener() {
        addMouseListener(new Comportement(){
            @Override
            public void mouseClicked(MouseEvent e) {     
                //Le place bien au milieu grâce aux donnnées fournies par le Controleur 
                // la sourie et les calcules de pixels
                deplacementZone(
                    e.getX()-(xBitsSize()*modele.getLargeur())/(coefSize*2),
                    e.getY()-(xBitsSize()*modele.getHauteur())/(coefSize*2)
                );
                repaint();
            }
        });
    }

    private void deplacementZone(int x, int y){
        try{
            x = (x<0)?0:x/xBitsSize();
            y = (y<0)?0:y/yBitsSize();
            this.souris.changeInfo(x, y, this.souris.getWidth(),souris.getHeight());
        }catch (IllegalArgumentException e){

        }
    }

}