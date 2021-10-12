import java.util.LinkedList;

public class Cell {

    // emplacement 
    private final int x, y;

    private int vie;

    /**
     * -1 => obstacle
     * 0 => vide
     * >0 => nourriture (nombre de "points" de nourriture)
     */
    private int nourriture;

    private int pheromone_nid = 0;
    private int pheromone_nourriture = 0;

    public Cell () {
        this(0,-1,-1);
    }

    /**
     * par défaut : la case est vide
     */
    public Cell (int x, int y){
        this(0,x,y);
    }

    public Cell (int type, int x, int y) {
        this.nourriture = type;
        this.x = x;
        this.y = y;
        this.vie = ((int) (Math.random() * 1000));
    }

    /**
     * <p>écriture des données de la cellule sur le writer</p>
     * <p>1 byte pour la valeur de la nourriture</p>
     * <p>2 byte pour la pheromone nid</p>
     * <p>2 byte pour la pheromone nid</p>
     */
    public void writeDatas(LabyrintheSave.ByteWriter writer){
        writer.write1(nourriture);
        writer.write2(pheromone_nid);
        writer.write2(pheromone_nourriture);
    }

    public void readDatas(LabyrintheSave.ByteReader reader){
        nourriture = reader.read1();
        pheromone_nid = reader.read2();
        pheromone_nourriture = reader.read2();
    }

    public int getPheromoneNid () {
        return this.pheromone_nid;
    }

     /**
     * utile pour construire un nouveau labyrinthe dans le bac à sable
     * @param x,y : coordonnées de la nouvelle cellule
     * @param k : information qu'on va préter
     * @return une nouvelle cellule
     */
    public void interpret ( int k) {
        if (k == 0) {
            setObstacle(-1);
        } else if (k == 1) {
            setObstacle(0);
        } else if (k == 2) {
            setObstacle(Labyrinthe.rndInt(1000, 5000));
        } else if (k == 3) {
            getFourmi();
        } 
    }

    public int getPheromoneNourriture () {
        return this.pheromone_nourriture;
    }

    public boolean estVide () {
        return this.nourriture == 0;
    }

    public void setObstacle (int n) {
        this.nourriture = n;
    }

    public int getObstacle(){
        return this.nourriture;
    }

    public int getX () {
        return this.x;
    }

    public int getY () {
        return this.y;
    }

    /**
     * fait apparaître une fourmi sur la case actuelle
     */
    public Fourmi getFourmi () {
        return new Fourmi(this.x,this.y);
    }

    @Override
    public String toString(){
        //B:obstacle; V:vide; N: Nourriture
        String res = (this.nourriture<0)?"B":(this.nourriture==0)?"V":"N";
        return res;
    }

    /**
     * 
     * @param f : quantité de nourriture prise
     * @return true si la case possède assez de nourriture
     */
    public boolean aMange (int f) {
        if (this.nourriture - f >= 0) {
            this.nourriture -= f;
            return true;
        }
        return false;
    }

    /**
     * @param around : cellules autour de celle-ci
     * @param aroundPlus : cellules autour de celle-ci plus largement
     * @return false si pas de nid autour, true sinon
     */
    public boolean checkNid (LinkedList<Cell> around, LinkedList<Cell> aroundPlus) {
        if (this instanceof Fourmiliere) {
            return true;
        }
        for (Cell c : around) {
            if (c instanceof Fourmiliere) {
                return true;
            }
        }
        for (Cell c : aroundPlus) {
            if (c instanceof Fourmiliere) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param around : cellules autour de celle-ci
     * @param aroundPlus : cellules autour de celle-ci plus largement
     * @return false si pas de nourriture autour, true sinon
     */
    public boolean checkNourriture (LinkedList<Cell> around, LinkedList<Cell> aroundPlus) {
        if (this.nourriture > 0) {
            return true;
        }
        for (Cell c : around) {
            if (c.nourriture > 0) {
                return true;
            }
        }
        for (Cell c : aroundPlus) {
            if (c.nourriture > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * ajoute p à la quantité de phéromone_nid en la limitant à 255
     * diminue la quantité de phéromone_nid/nourriture si sa vie est terminée et qu'elle est au-dessus de 50
     * régenère la vie de la phéromone_nid si elle est à 0
     * @param p : la valeur que l'on souhaite ajouter à la quantité de phéromone_nid
     * @param around : cellules autour de celle-ci
     * @param aroundPlus : cellules autour de celle-ci plus largement
     */
    public void setPhenoromoneNid (int p, LinkedList<Cell> around, LinkedList<Cell> aroundPlus) {
        if (pheromone_nid < 255-p && pheromone_nid+p >= 0) {
            this.pheromone_nid += p;
        }
        if (this.vie > 0) {
            this.vie -= 1;
        }
        if (!(checkNid(around,aroundPlus)) && this.pheromone_nid >= 50 && this.vie == 0) {
            this.pheromone_nid -= 50;
        }
        if (!(checkNourriture(around,aroundPlus)) && this.pheromone_nourriture >= 50 && this.vie == 0) {
            this.pheromone_nourriture -= 50;
        }
        if (this.pheromone_nid == 0) {
            this.vie = ((int) (Math.random() * 1000));
        }
    }

    /**
     * ajoute p à la quantité de phéromone_nourriture en la limitant à 255
     * diminue la quantité de phéromone_nid/nourriture si sa vie est terminée et qu'elle est au-dessus de 50
     * régenère la vie de la phéromone_nourriture si elle est à 0
     * @param p : la valeur que l'on souhaite ajouter à la quantité de phéromone_nourriture
     * @param around : cellules autour de celle-ci
     * @param aroundPlus : cellules autour de celle-ci plus largement
     */
    public void setPheromoneNourriture (int p, LinkedList<Cell> around, LinkedList<Cell> aroundPlus) {
        if (this.pheromone_nourriture < 255-p && this.pheromone_nourriture+p >= 0) {
            this.pheromone_nourriture += p;
        }
        if (this.vie > 0) {
            this.vie -= 1;
        }
        if (!(checkNourriture(around,aroundPlus)) && this.pheromone_nourriture >= 50 && this.vie == 0) {
            this.pheromone_nourriture -= 50;
        }
        if (!(checkNid(around,aroundPlus)) && this.pheromone_nid >= 50 && this.vie == 0) {
            this.pheromone_nid -= 50;
        }
        if (this.pheromone_nourriture == 0) {
            this.vie = ((int) (Math.random() * 1000));
        }
    }
    
}