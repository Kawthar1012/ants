import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
/**
 * Une affichage agrandit de la carte sur une zone ciblée par la sourie
 */
public class BigMap extends JPanel implements PanesInterface {

	/**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;

    private Image food,block,path,fourmiliere;
    /**
     * Nord, Sud, Est, Ouest
     */
    private Image antN,antS,antE,antO; 

    private MiniMap.Zone zone;
    private int largeur, longueur;
    private Labyrinthe modele;
    //private final JPanel infoCell;

    private boolean graphique;
    private boolean nestView;

    public BigMap(int x, int y, int w, int h,Labyrinthe l){
        setBounds(x,y,w,h);
        this.largeur = w; this.longueur = h;
        this.modele = l;
        this.graphique = true;
        this.nestView = true;
       
    }
    /**
     * A appeler que quand this.zone a Ã©tÃ© initialisÃ©
     */
    private void initImages(){
        try{
            this.food = ImageIO.read(new File("./Images/food.png"));
            this.block = ImageIO.read(new File("./Images/block.png"));
            this.path = ImageIO.read(new File("./Images/path.png"));
            this.fourmiliere = ImageIO.read(new File("./Images/frm.png"));
            this.antN = ImageIO.read(new File("./Images/antN.png"));
            this.antS = ImageIO.read(new File("./Images/antS.png"));
            this.antE = ImageIO.read(new File("./Images/antE.png"));
            this.antO = ImageIO.read(new File("./Images/antO.png"));
        }catch (IOException e){
            System.out.println("[BigMap]:Vérifiez que ces fichier sont bien prÃ©sent :"+
                "\n./Images/chunk.png \n./Images/ant*.png \n./Images/ant.png"
                +"\n./Images/block.png \n./Images/path.png");
        }
    }

    /**
     * Affiche avec les images
     */
    public void setGraphique(boolean b){
        this.graphique =b;
    }

    public void setNestView(boolean b){
        this.nestView = b;
    }

    /**
     * A appeler aprÃ¨s l'instanciation de cette Classe
     * Permet de changer la maniÃ¨re de dessiner la carte
     * Fonction <strong>repaint()</strong> prÃ©sent dans le code
     * @param z {@code MiniMap.Zone}
     */
    public void changeZoneValue(MiniMap.Zone z){
        if(zone == null){
            this.zone = z; initImages();
        }else{
            this.zone.changeInfo(z.getX(), z.getY(), z.getWidth(), z.getHeight());
            repaint();
            System.out.println("\nChangemet de perspective dans {BigMap} "
                +"\nNouvelle zone afficher : "+z);
        }
    }

    /**
     * Renvoie la partie choisie par {@code MiniMap.Zone}
     * @return Cell[][]
     */
    private Cell[][] getCellInstanceOfZone(){
        Cell[][] res = new Cell[zone.getHeight()][zone.getWidth()]; //y x
        for(int y=0;y<zone.getHeight();y++){
            for(int x=0;x<zone.getWidth();x++){
                res[y][x] = modele.getCell(zone.getX()+x, zone.getY()+y);
            }
        }
        return res;
    }

    private Point getScale(){
        return new Point(largeur/zone.getWidth(),longueur/zone.getHeight());
    }

    @Override
    public void paint(Graphics g){
        Cell[][] mod = getCellInstanceOfZone();
        Image img = null;
        Color mapcolor = null;
        Point scale = getScale();

        if(zone!=null){
            for(int y=0; y<zone.getHeight();y++){
                for(int x=0; x<zone.getWidth();x++){
                    //B:obstacle; V:vide; N: Nourriture; F:Fourmi
                    if(mod[y][x].toString().equals("B")){
                        img=this.block.getScaledInstance(scale.x,scale.y, Image.SCALE_DEFAULT); 
                        mapcolor = Color.BLACK;
                    }else{
                        img=this.path.getScaledInstance(scale.x,scale.y, Image.SCALE_DEFAULT);
                        mapcolor = (nestView)?new Color(255,255,255-mod[y][x].getPheromoneNid()):new Color(255,255-mod[y][x].getPheromoneNourriture(),255);
                    }
                    if(this.graphique){
                        g.drawImage(img,x*scale.x,y*scale.y,null);
                    }else{
                        g.setColor(mapcolor);
                        g.fillRect(x*scale.x,y*scale.y,scale.x,scale.y);
                    }
                    if(mod[y][x].toString().equals("N")){
                        g.drawImage(this.food.getScaledInstance(scale.x,scale.y, Image.SCALE_DEFAULT),x*scale.x,y*scale.y,null);
                    }
                    if(mod[y][x].toString().equals("FM")){
                        g.drawImage(this.fourmiliere.getScaledInstance(scale.x,scale.y, Image.SCALE_DEFAULT),x*scale.x,y*scale.y,null);
                    }
                    drawAnt(g, x, y);
                }
            }
        }else{
            setBackground(Color.RED);
            System.out.println("Erreur : Pour avoir l'affichage appeler la fontion changeZoneValue(MiniMap.Zone z) "+
                "\n Au moins une fois après la création d'un BigMap");
        }        
    }

    private void drawAnt(Graphics g, int x, int y){
        Image img = this.antN; //default value
        Point scale = getScale();
        for (int i=0; i < modele.Fourmis().size(); i++) {
            if (x+zone.getX() == modele.Fourmis().get(i).getX() && y+zone.getY() == modele.Fourmis().get(i).getY()) {
                switch(modele.Fourmis().get(i).getDirection()){
                    // 0 : haut, 1 : bas, 2 : gauche, 3 : droite
                    case 0 : img=this.antN; break;
                    case 1 : img=this.antS;break;
                    case 2 : img=this.antO;break;
                    case 3 : img=this.antE;break;
                }
                g.drawImage(img.getScaledInstance(scale.x*2/3,scale.y*2/3, Image.SCALE_DEFAULT),
                    x*scale.x,y*scale.y,null);
            }
        }
    }
}