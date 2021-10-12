import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

public class Labyrinthe {

    static class CoorFourmi{
        public final int x,y,typeferomone;
        CoorFourmi(int x, int y, int t){
            this.x = x;  
            this.y=y;
            this.typeferomone = t;
        }
    }

    private Cell[][] cellules;
    private int obstacles = 0;
    private LinkedList<Fourmi> fourmis;
    private Fourmiliere mainFourmiliere;

    public LinkedList<Fourmi> Fourmis () {
        return this.fourmis;
    }

    /**
     * <p>écriture des données du labyrinthe sur le writer</p>
     * <p>2 byte pour la largeur</p>
     * <p>2 byte pour la longueur</p>
     * 
     * <p>2 byte pour la position de la fourmilière ( 1 byte pour X , 1 byte pour Y)</p>
     * <p>1 byte pour le nombre de fourmis dans la fourmilière</p>
     * 
     * <p>Données de la cellules</p>
     * <p>Données des fourmis</p>
     */
    public void writeDatas(LabyrintheSave.ByteWriter writer){
        writer.write2(getLargeur());
        writer.write2(getHauteur());

        Fourmiliere fourmiliere = getFourmiliere();
        writer.write1(fourmiliere.getX());
        writer.write1(fourmiliere.getY());
        fourmiliere.writeDatas(writer);

        for(int y = 0;y<getHauteur();y++){
            for(int x = 0;x < getLargeur();x++){
                if(!(getCell(x, y) instanceof Fourmiliere))
                    cellules[y][x].writeDatas(writer);
            }
        }

        writer.write2(fourmis.size());
        for(Fourmi fourmi : fourmis){
            fourmi.writeDatas(writer);
        }
    }

    public void readDatas(LabyrintheSave.ByteReader reader){
        int largeur = reader.read2();
        int hauteur = reader.read2();

        int f_x = reader.read1();
        int f_y = reader.read1();
        Fourmiliere fourmiliere = new Fourmiliere(f_x,f_y,-1);
        fourmiliere.readDatas(reader);

        cellules = new Cell[hauteur][largeur];
        for(int y = 0;y<hauteur;y++){
            for(int x = 0;x<largeur;x++){
                if(x != f_x || y != f_y){
                    cellules[y][x] = new Cell(x,y);
                    cellules[y][x].readDatas(reader);
                }                
            }
        }

        cellules[fourmiliere.getY()][fourmiliere.getX()] = fourmiliere;

        fourmis = new LinkedList<Fourmi>();
        int fourmisCount = reader.read2();
        for(int i = 0;i<fourmisCount;i++){
            Fourmi fourmi = new Fourmi(-1, -1);
            fourmi.readDatas(reader);
            fourmis.add(fourmi);
        }           
    }

    public Labyrinthe (int l, int h) {
        this(l,h,l*h);
    }

    public Labyrinthe(int largeur, int hauteur, int nbObstacle) {
        largeur = Math.max(2, largeur);
        hauteur = Math.max(2, hauteur);
        cellules = new Cell[hauteur][largeur];

        fourmis = new LinkedList<>();

        // limite le nb d'obstacle à 80% de la map
        nbObstacle = Math.min(nbObstacle, (int) ((largeur * hauteur) * 0.80));

        // on défini une liste de Point qui va stocké les cellule bloquante
        LinkedList<Point> blockSave = new LinkedList<>();

        // initialisation de la grille en remplissant tout par des cases bloquante
        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                cellules[i][j] = new Cell(-1, j, i);
                blockSave.add(new Point(j,i));
            }
        }

        // initialisation du nombre d'obstacle
        obstacles = largeur * hauteur;

        // list qui va stocké quelque cellules vide ( pour éviter de parcourir le tableau à chaque fois qu'on veut une cellule vide )
        LinkedList<Point> emptyCells = new LinkedList<>();

        // défini les coordonées de la premiere case vide
        int cellId = rndInt(0, largeur * hauteur);
        int cellX = cellId % largeur;
        int cellY = cellId / largeur;

        // on ajoute la première cellule vide
        emptyCells.add(new Point(cellX, cellY));

        // retire l'obstacle sur les coordonées défini
        setObstacle(0, cellX, cellY);

        // tant qu'il n'y a pas le nombre d'obstacle requis ou que la list des cellules vide n'est pas vide
        while (obstacles > nbObstacle && emptyCells.size() > 0){
            // on récupère une cellule vide
            int rndEmpty = rndInt(0, emptyCells.size());
            Point emptyPoint = emptyCells.get(rndEmpty);

            // on vérifie si le nombre de block autour de la cellule vide est > 1 (strictement)
            LinkedList<Point> blockArround = getBlockArround(emptyPoint.x, emptyPoint.y);
            if(blockArround.size() > 1){
                // si il y a au moins 1 block on prend un bloque au hasard parmis les block autour et on le transforme en case vide
                int rndPoint = rndInt(0, blockArround.size());
                Point point = blockArround.get(rndPoint);
                try{                    
                    setObstacleErr(0, point.x, point.y);
                }catch(NobodyNearException nne){
                    setObstacle(-1, nne.getPoint().x, nne.getPoint().y);
                }
                
                blockSave.removeIf(pp -> {
                    return pp.x == point.x && pp.y == point.y;
                });
                emptyCells.add(new Point(point.x, point.y));
            }

            // on retire toute les cellules qui ont moins de 1 block autour d'eux
            emptyCells.removeIf(point -> {
                return getBlockArround(point.x, point.y).size() <= 1;
            });
        }

        // Cette boucle est nécessaire si la boucle d'en haut ne s'exécute pas jusqu'a la fin (s'il n'y a pas le nombre d'obstacle défini)
        // on prend un block aléatoire parmis la liste des blocks
        while(obstacles >= nbObstacle && blockSave.size() > 0){
            int rnd = rndInt(0, blockSave.size());
            try{                    
                setObstacleErr(0, blockSave.get(rnd).x, blockSave.get(rnd).y);                
                Point p = blockSave.remove(rnd);
            }catch(NobodyNearException nne){
                setObstacle(-1, nne.getPoint().x, nne.getPoint().y);
            }
        }

    }

    /**
     * interpréteur permettant de créer un labyrinthe à partir d'un tableau de tableaux d'entiers
     * utile pour l'extension permettant de construire son labyrinthe
     * @param tab : informations fournies par l'utilisateur
     * @param nbFourmi : idem
     * @return un labyrinthe correct
     */
    public Labyrinthe (int [][] tab, int nbFourm) {
        int nbObs = 0;
        this.fourmis = new LinkedList<Fourmi>();
        this.cellules = new Cell[tab.length][tab[0].length];
        for (int i = 0; i < tab.length; i++) { 
            for (int j = 0; j < tab[0].length; j++) { 
                this.cellules[i][j] =(tab[i][j]!=4)? new Cell(j,i):new Fourmiliere(j,i);
                this.cellules[i][j].interpret(tab[i][j]);
                if (tab[i][j] == 0) {
                    nbObs++;
                }
                if (tab[i][j] == 3) {
                    this.fourmis.add(new Fourmi(j,i));
                }
                if( this.cellules[i][j] instanceof Fourmiliere){
                    this.mainFourmiliere = (Fourmiliere)this.cellules[i][j];
                }
            }
        }
        this.obstacles = nbObs;
        this.mainFourmiliere.setNbFourmis(nbFourm);
        
    }

    /**
     * retrouve la fourmiliere du labyrinthe
     * @return la fourmiliere du labyrinthe
     */
    public Fourmiliere getFourmiliere () {
        for (int i=0; i < cellules.length; i++) {
            for (int j=0; j < cellules[0].length; j++) {
                if (getCell(j,i) instanceof Fourmiliere) {
                    return (Fourmiliere)cellules[i][j];
                }
            }
        }
        return null;
    }

    public int getLargeur(){
        if(cellules.length == 0)
            return 0;

        return cellules[0].length;
    }

    public int getHauteur(){
        return cellules.length;
    }
    
    public int nbCellules(){
        return getHauteur() * getLargeur();
    }

    public void reloadLaby(){
        reloadLaby(getLargeur(), getHauteur(), obstacles);
    }

    public void reloadLaby(int largeur,int hauteur,int nbObs){
        Labyrinthe copie = new Labyrinthe(largeur, hauteur, nbObs);
        cellules = new Cell[hauteur][largeur];

        for(int i = 0;i<cellules.length;i++){
            for(int j = 0;j<cellules[i].length;j++){
                cellules[i][j] = copie.cellules[i][j];
            }
        }

        obstacles = copie.obstacles;
        fourmis = copie.fourmis;
    }

    public void reloadLaby(Labyrinthe laby){
        if(laby == null)
            return;
        
        cellules = laby.cellules;
        obstacles = laby.obstacles;
        fourmis = laby.fourmis;
    }

    /**
     * déplace chaque fourmi se trouvant sur le labyrinthe
     * si elle est mortelle, déplace la fourmi uniquement si il lui reste de la vie
     * sinon, déplace la fourmi en fonction de ce qu'elle cherche (nid ou nourriture)
     * @param d : 1 : algo sans mémoire, 2 : algo avec mémoire
     * @param mange : false si la nourrriture est illimitée, true sinon
     * @param eternite : true si les fourmis sont mortelles, false sinon
     * @param s : taille de la memoire de la fourmi
     */
    public void deplacement (int d, boolean mange, boolean eternite, int s) {
        for (int i=0; i < this.fourmis.size(); i++) {
            Cell c = this.getCell(this.fourmis.get(i).getX(),this.fourmis.get(i).getY());
            if (eternite && this.fourmis.get(i).getVie() <= 0) {
                this.fourmis.remove(i);
            } else {
                Cell f = new Cell ();
                if (this.fourmis.get(i).getTypePheromone() == 0) {
                    f = this.fourmis.get(i).moveNourriture(cellAround(c),mange,d,s);
                } else {
                    f = this.fourmis.get(i).moveNid(cellAround(c),d,s);
                }
                this.fourmis.get(i).setCoordonnees(f,cellAround(f),cellAroundPlus(f));
            } 
        }
    }

    /**
     * @param c : cellule prise pour référence
     * @return une liste des cellules directement autour de c
     */
    public LinkedList<Cell> cellAround (Cell c) {
        LinkedList<Cell> around = new LinkedList<>();
        Cell haut = new Cell ();
        Cell bas = new Cell ();
        Cell gauche = new Cell ();
        Cell droite = new Cell ();
        if (c.getY() > 0) {
            haut = this.getCell(c.getX(),c.getY()-1);
            around.add(haut);
        }
        if (c.getY() < cellules.length-1) {
            bas = this.getCell(c.getX(),c.getY()+1);
            around.add(bas);
        } 
        if (c.getX() > 0) {
            gauche = this.getCell(c.getX()-1,c.getY());
            around.add(gauche);
        }
        if (c.getX() < cellules[0].length-1) {
            droite = this.getCell(c.getX()+1,c.getY());
            around.add(droite);
        } 
        return around;
    }

    /**
     * @param c : cellule prise pour référence
     * @return une liste des cellules plus largement autour de c
     */
    public LinkedList<Cell> cellAroundPlus (Cell c) {
        LinkedList<Cell> aroundPlus = new LinkedList<>();
        Cell plusHaut = new Cell ();
        Cell plusBas = new Cell ();
        Cell plusDroite = new Cell ();
        Cell plusGauche = new Cell ();
        Cell hautGauche = new Cell ();
        Cell hautDroite = new Cell ();
        Cell basGauche = new Cell ();
        Cell basDroite = new Cell ();
        if (c.getY() > 1) {
            plusHaut = this.getCell(c.getX(),c.getY()-2);
            aroundPlus.add(plusHaut);
        }
        if (c.getY() < cellules.length-2) {
            plusBas = this.getCell(c.getX(),c.getY()+2);
            aroundPlus.add(plusBas);
        } 
        if (c.getX() > 1) {
            plusGauche = this.getCell(c.getX()-2,c.getY());
            aroundPlus.add(plusGauche);
        }
        if (c.getX() < cellules[0].length-2) {
            plusDroite = this.getCell(c.getX()+2,c.getY());
            aroundPlus.add(plusDroite);
        } 
        if (c.getY() > 0 && c.getX() > 0) {
            hautGauche = this.getCell(c.getX()-1,c.getY()-1);
            aroundPlus.add(hautGauche);
        }
        if (c.getY() < cellules.length-1 && c.getX() > 0) {
            basGauche = this.getCell(c.getX()-1,c.getY()+1);
            aroundPlus.add(basGauche);
        } 
        if (c.getX() < cellules[0].length-1 && c.getY() > 0)  {
            hautDroite = this.getCell(c.getX()+1,c.getY()-1);
            aroundPlus.add(hautDroite);
        }
        if (c.getX() < cellules[0].length-1 && c.getY() < cellules.length-1) {
            basDroite = this.getCell(c.getX()+1,c.getY()+1);
            aroundPlus.add(basDroite);
        } 
        return aroundPlus;
    }

    /**
     * dépose de la nourriture de façon aléatoire sur les cases
     * @param nb : nombre de cases où on va déposer la nourriture
     * @param max : quantité maximum de nourriture déposé sur les cases
     */
    public void setPlusNourriture (int nb, int max) {
        nb = Math.min((getHauteur() * getLargeur()) - obstacles, nb);

        int rand = 0;
        for (int i = 0; i < nb; i++) {
            rand = rndInt(1000, max);
            setNourriture(rand);
        }
    }

    private boolean asignNourritureInLine(int y,int nourritureValue){
        nourritureValue = Math.min(nourritureValue, Byte.MAX_VALUE);

        ArrayList<Integer> cells = new ArrayList<Integer>();
        for(int x = 0;x<getLargeur();x++){
            if(getCell(x,y).estVide()){
                cells.add(x);
            }
        }

        if(cells.size() > 0){
            int x = cells.get(rndInt(0, cells.size()));
            int obsValue = getCell(x,y).getObstacle();
            try{                
                setObstacleErr(nourritureValue, x, y);
            }catch(NobodyNearException nne){
                setObstacle(obsValue, nne.getPoint().x, nne.getPoint().y);
                System.out.println(nne.getMessage());
                return false;
            }
            return getCell(cells.get(rndInt(0, cells.size())), y).getObstacle() > 0;
        }
        return false;
    }

    /**
     * utile pour construire un nouveau labyrinthe dans le bac à sable
     * @param x,y : coordonnées de la nouvelle cellule
     * @param k : information qu'on va interpréter
     * @return une nouvelle cellule
     */
    public Cell interpret (int x, int y, int k) {
        Cell c = new Cell(x,y);
        if (k == 0) {
            c.setObstacle(-1);
        } else if (x == 2) {
            c.setObstacle(rndInt(1000, 5000));
        } else if (x == 3) {
            c.getFourmi();
        } else if (x == 4) {
            c = new Fourmiliere(x,y);
        }
        return c;
    }

    public Labyrinthe newLab (int [][] tab) {
        int hauteur = tab.length;
        int largeur = tab[0].length;
        Labyrinthe lab = new Labyrinthe (hauteur,largeur);
        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                lab.cellules[i][j] = interpret(i,j,tab[i][j]);
            }
        }
        return lab;
    }

    /**
     * place de la nourriture de façon aléatoire sur une case vide
     * @param n : quantité de nourriture à déposer sur la cellule
     */
    public void setNourriture (int n) {
        int rndY = rndInt(0, getHauteur());

        while(asignNourritureInLine(rndY % getHauteur(), n)){
            rndY++;            
        }
    }

    /**
     * ajoute une fourmilière avec nbFourmi fourmi à une position vide aléatoire
     * @param nbFourmi : nombre de fourmis dans la fourmiliere
     */
    public void initFourmiliere(int nbFourmi){
        int x = (int) (Math.random()*cellules[0].length);
        int y = (int) (Math.random()*cellules.length);
        while (!(getCell(x,y).estVide())) {
            x = (int) (Math.random()*cellules[0].length);
            y = (int) (Math.random()*cellules.length);
        }
        this.mainFourmiliere = new Fourmiliere(x,y,nbFourmi);
        cellules[y][x] = this.mainFourmiliere;
    }

    public void setNbFourmisFourmiliere(int n){
        initFourmiliere(n);
     }


    
    /**
     * modifie la valeur de l'obstacle d'une cellule défini par ses coordonées.
     * incrémente obstacle si la valeur != 0
     * décrémente si obstacle est > 0 et la valeur == 0
     */
    public void setObstacle(int obsCount , int x,int y){
        Cell cell = getCell(x,y);
        if(cell != null) {
            cell.setObstacle(obsCount);
            obstacles += (obsCount != 0 ? 1 : (obstacles > 0 ? -1 : 0));
        }
    }

    private void setObstacleErr(int obsCount , int x ,int y) throws NobodyNearException{
        Cell cell = getCell(x,y);
        if(cell != null) {
            cell.setObstacle(obsCount);
            obstacles += (obsCount != 0 ? 1 : (obstacles > 0 ? -1 : 0));
        }

        if(getEmptyArround(x, y).size() == 0 && obsCount >= 0)
            throw new NobodyNearException(new Point(x,y)); 
    }

    public static class NobodyNearException extends Exception{
        private Point point;
        private static final long serialVersionUID = 3269040576202312270L;
        
        public NobodyNearException(Point p){
            point = p;
        }

        public Point getPoint(){
            return point;
        }

        public String getMessage(){
            return "NNE catch : pos [" + point.getX() + "," + point.getY() + "]";
        }
    }

    /**
     * retourne les cases dans la grille autour d'une coordonée x , y
     */
    private LinkedList<Point> getArround(int x,int y){
        LinkedList<Point> arr = new LinkedList<>();

        Cell top = getCell(x, y - 1);
        if(top != null)
            arr.add(new Point(x, y - 1));

        Cell right = getCell(x + 1,y);
        if(right != null)
            arr.add(new Point(x + 1,y));

        Cell bottom = getCell(x, y + 1);
        if(bottom != null)
            arr.add(new Point(x, y + 1));

        Cell left = getCell(x - 1,y);
        if(left != null)
            arr.add(new Point(x - 1, y));

        return arr;
    }

    /**
     * retourne les cases bloquante autour d'une coordonée x , y
     */
    private LinkedList<Point> getBlockArround(int x,int y){
        LinkedList<Point> arr = new LinkedList<>();

        Cell top = getCell(x, y - 1);
        if(top != null && !top.estVide())
            arr.add(new Point(x, y - 1));

        Cell right = getCell(x + 1,y);
        if(right != null && !right.estVide())
            arr.add(new Point(x + 1,y));

        Cell bottom = getCell(x, y + 1);
        if(bottom != null && !bottom.estVide())
            arr.add(new Point(x, y + 1));

        Cell left = getCell(x - 1,y);
        if(left != null && !left.estVide())
            arr.add(new Point(x - 1, y));

        return arr;
    }

    /**
     * retourne les cases vide autour d'une coordonée x , y
     */
    private LinkedList<Point> getEmptyArround(int x,int y){
        LinkedList<Point> arr = new LinkedList<>();

        Cell top = getCell(x, y - 1);
        if(top != null && top.estVide())
            arr.add(new Point(x, y - 1));

        Cell right = getCell(x + 1,y);
        if(right != null && right.estVide())
            arr.add(new Point(x + 1,y));

        Cell bottom = getCell(x, y + 1);
        if(bottom != null && bottom.estVide())
            arr.add(new Point(x, y + 1));

        Cell left = getCell(x - 1,y);
        if(left != null && left.estVide())
            arr.add(new Point(x - 1, y));

        return arr;
    }

    /**
     * retourne la cellule à partir de son id avec l'id < nombre total de cases
     * ( valeur x = id % largeur )
     * ( valeur y = id / largeur )
     */
    public Cell getCell(int id){
        int cellX = id % getLargeur();
        int cellY = id / getLargeur();

        return getCell(cellX, cellY);
    }

    /**
     * retourne la cellule à la position x , y
     */
    public Cell getCell(int x,int y){
        if(isOut(x,y))
            return null;

        return cellules[y][x];
    }

    /**
     * vérification si une coordonée (x,y) est dans la grille
     */
    public boolean isOut(int x,int y){
        return x < 0 || y < 0 || x >= getLargeur() || y >= getHauteur();
    }

    /**
     * fonction qui permet d'obtenir un nombre aléatoire entre a et b
     *
     */
    public static int rndInt(int a,int b){
        return (int)(Math.random() * (b - a)) + a;
    }

    public void afficheTest(){
        for(int i = 0;i<cellules.length;i++){
            for(int j = 0;j<cellules[i].length;j++){
                System.out.print(cellules[i][j].estVide() ? " " : "X");
            }
            System.out.println();
        }
    }

    public CoorFourmi[] getcoordonnee(){
        int i =0;
        CoorFourmi[] res = new CoorFourmi[this.fourmis.size()];
        for(Fourmi e:this.fourmis){
            res[i] = new CoorFourmi(e.getX(),e.getY(),e.getTypePheromone()); i++;
        }
        return res;
    }
}