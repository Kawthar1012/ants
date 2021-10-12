import java.util.LinkedList;


public class Fourmi {

    // emplacement
    private int x, y;

    private int vie;

    /**
     * type de phéromone déposé
     * 0 : pheromone_nid, 1 : phermone_nourriture
     */
    private int pheromone = 0;

    private int nourriture = 0;

    // 0 : haut, 1 : bas, 2 : gauche, 3 : droite
    private int direction = 0;

    private LinkedList<Cell> memoire;


    public Fourmi (int x, int y) {
        this.x = x;
        this.y = y;
        this.memoire = new LinkedList<Cell>();
        this.vie = 500 + ((int) (Math.random() * 3000));
    }
    /**
     * <p>écriture des données de la fourmi sur le writer</p>
     * <p>2 byte pour la position ( 1 byte pour X , 1 byte pour Y)</p>
     * <p>1 byte pour la nourriture</p>
     * <p>1 byte pour la direction de la fourmi</p>
     * <p>2 bytes pour la vie</p>
     */
    public void writeDatas(LabyrintheSave.ByteWriter writer){
        writer.write1(x);
        writer.write1(y);
        writer.write1(nourriture);
        writer.write1(direction);
        writer.write2(vie);
    }

    public void readDatas(LabyrintheSave.ByteReader reader){
        x = reader.read1();
        y = reader.read1();
        nourriture = reader.read1();
        pheromone = nourriture > 0 ? 1 : 0;
        direction = reader.read1();
        vie = reader.read2();
    }

    /**
     * type de phéromone déposé
     * 0 : pheromone_nid, 1 : phermone_nourriture
     * @return boolean
     */
    public int getTypePheromone(){
        return this.pheromone;
    }

    public int getNourriture () {
        return this.nourriture;
    }

    public void setNourriture (int n) {
        this.nourriture = n;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getDirection () {
        return this.direction;
    }

    public int getTaille () {
        return this.memoire.size();
    }

    public int getVie () {
        return this.vie;
    }


    public String toString () {
        return "F";
    }

    /**
     * vérifie si la cellule est dans la mémoire à l'aide de ses coordonnées
     * @return true si la cell c est dans la mémoire, false sinon
     */
    public boolean estDansMemoire (Cell c) {
        for (Cell m : this.memoire) {
            if (c.getX() == m.getX() && c.getY() == m.getY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * définit les cellules possibles pour la fourmi à l'aide de choixNourriture
     * vérifie d'abord si l'une des cellules a de la nourriture et return celle-ci si c'est le cas
     * sinon return une cellule au hasard avec plus de chance sur celle ayant le plus de pheromone_nourriture
     * @param cellAround : liste de cellules entourant la cellule actuelle
     * @param mange : true si la nourriture disparait de la cellule, false sinon
     * @param d : 1 : la fourmi n'a pas de mémoire, 2 : la cellule a de la mémoire
     * @param s : taille de la mémoire (0 si d == 1)
     * @return la nouvelle position de la cellule
     */
    public Cell moveNourriture (LinkedList<Cell> cellAround, boolean mange, int d, int s) {
        LinkedList<Cell> choix = choixNourriture(cellAround);
        Cell futur = new Cell ();
        int c = 0;
        boolean b = true;
        while (b) {
            switch (c) {
                case 0 : if (choix.size() == 0) { // cas où la fourmi ne peut pas bouger
                            futur = new Cell (this.x,this.y);
                            b = false; break;
                        } 
                        c++; break;
                case 1 : int rand = ((int) (Math.random() * choix.size()));
                        int com = 0;
                        while (com < choix.size()) {
                            if (checkNourriture(choix.get(rand),mange)) { // a trouvé de la nourriture
                                futur = choix.get(rand);
                                this.pheromone = 1;
                                b = false;
                                break;
                            } else { 
                                rand = (rand+1)%choix.size();
                                com++;
                            }
                        }
                        c++; break;
                case 2 : rand = ((int) (Math.random() * choix.size()));
                            while (choix.get(rand).getX() == -1 || choix.get(rand).getY() == -1) {
                                rand = ((int) (Math.random() * choix.size()));
                            }
                            this.direction = toDirection(choix.get(rand));
                            futur = choix.get(rand);
                            b = false;
                            break;
            }
        } 
        if (d != 1) { // supprime la premiere cellule de la mémoire et ajoute la derniere dans la limite de la taille définit
            if (this.memoire.size() < s) {
                this.memoire.add(futur);
            } else {
                this.memoire.removeFirst();
                this.memoire.add(futur);
            }
        }
        return futur;
    }

    /**
     * ajoute les cellules au choix au final si :
     * elles n'ont pas de block, elles ne sont pas dans la mémoire (pour éviter les retours en arrière)
     * augmente leur apparition en fonction de leur quantite de pheromone_nourriture
     * diminue leur apparition si elles contiennent plus de pheromone_nid que de pheromone_nourriture
     * @param cellAround : liste de cellules entourant la cellule actuelle
     * @return une linkedlist des cellules possibles pour la fourmi
     */
    public LinkedList<Cell> choixNourriture (LinkedList<Cell> cellAround) {
        LinkedList<Cell> choix = new LinkedList<>();
        for (Cell c : cellAround) {
            if (c.getObstacle() >= 0) {
                for (int i = 0; i < c.getPheromoneNourriture()+1; i++) {
                    choix.add(c);
                }
                if (c.getPheromoneNid() > c.getPheromoneNourriture()) {
                    for (int i = 0; i < c.getPheromoneNourriture()/2; i++) {
                        choix.remove(c);
                    }
                }
                if (this.memoire.size() > 0) {
                    if (estDansMemoire(c) && c.getObstacle() <= 0) {
                        choix.remove(c);
                    }
                }
            }
        }
        return choix;
    }

    /**
     * ajoute les cellules au choix au final si :
     * elles n'ont pas de block, elles ne sont pas dans la mémoire (pour éviter les retours en arrière)
     * augmente leur apparition en fonction de leur quantite de pheromone_nid
     * diminue leur apparition si elles contiennent plus de pheromone_nourriture que de pheromone_nid
     * @param cellAround : liste de cellules entourant la cellule actuelle
     * @return une linkedlist des cellules possibles pour la fourmi 
     */
    public LinkedList<Cell> choixNid (LinkedList<Cell> cellAround) {
        LinkedList<Cell> choix = new LinkedList<>();
        for (Cell c : cellAround) {
            if (c.getObstacle() >= 0) {
                for (int i = 0; i < c.getPheromoneNid()+1; i++) {
                    choix.add(c);
                }
                if (c.getPheromoneNid() < c.getPheromoneNourriture()) {
                    for (int i = 0; i < c.getPheromoneNid()/2; i++) {
                        choix.remove(c);
                    }
                }
                if (this.memoire.size() > 0) {
                    if (estDansMemoire(c) && !(c instanceof Fourmiliere)) {
                        choix.remove(c);
                    }
                }

            }
        }
        return choix;
    }

    /**
     * définit les cellules possibles pour la fourmi à l'aide de choixNid
     * vérifie d'abord si l'une des cellules est la fourmiliere et return celle-ci si c'est le cas
     * sinon return une cellule au hasard avec plus de chance sur celle ayant le plus de pheromone_nid
     * @param cellAround : liste de cellules entourant la cellule actuelle
     * @param d : 1 : la fourmi n'a pas de mémoire, 2 : la cellule a de la mémoire
     * @param s : taille de la mémoire (0 si d == 1)
     * @return la nouvelle position de la cellule
     */
    public Cell moveNid (LinkedList<Cell> cellAround, int d, int s) {
        LinkedList<Cell> choix = choixNid(cellAround);
        Cell futur = new Cell ();
        int c = 0;
        int rand = 0;
        boolean b = true;
        while (b) {
            switch (c) {
                case 0 : if (choix.size() == 0) { // cas où la fourmi ne peut pas bouger
                            futur = new Cell (this.x,this.y);
                            b = false; break;
                        } 
                        c++; break;
                case 1 : for (int i=0; i < choix.size(); i++) { // cherche la fourmilière
                            if (choix.get(i) instanceof Fourmiliere) { 
                                this.direction = toDirection(choix.get(i));
                                ((Fourmiliere)choix.get(i)).setNourriture(this.nourriture);
                                futur = choix.get(i);
                                this.pheromone = 0;
                                this.nourriture = 0;
                                b = false;
                                break;
                    }
                }
                c++; break;
                case 2 :  rand = ((int) (Math.random() * choix.size()));
                            while (choix.get(rand).getX() == -1 || choix.get(rand).getY() == -1) {
                                rand = ((int) (Math.random() * choix.size()));
                            }
                            this.direction = toDirection(choix.get(rand));
                            futur = choix.get(rand);
                            b = false;
                            break;
            }
        } 
        if (d != 1) { // supprime la premiere cellule de la mémoire et ajoute la derniere dans la limite de la taille définit
            if (this.memoire.size() < s) {
                this.memoire.add(futur);
            } else {
                this.memoire.removeFirst();
                this.memoire.add(futur);
            }
        }
        return futur;
    }


    /**
     * vérifie si la cellule a de la nourriture 
     * @return true si la cellule a de la nourriture, false sinon
     */
    public boolean checkCell (Cell c) {
        if (c == null || c.getX() == -1 || c.getY() == -1) return false;
        return c.getObstacle() > 0;
    }

    /**
     * si la cellule a de la nourriture, la fourmi la prend et fait diminue le nb de nourriture sur la case si b = true
     * @param c : cellule qu'on vérifie
     * @param b : boolean indiquant si la nourriture disparait de la case
     * @return true si la fourmi a bien pu prendre la nourriture de la case
     */
    public boolean checkNourriture (Cell c, boolean b) {
        if (checkCell(c)) { // vérifie si la case a de la nourriture
            int f = 0;
            int m = 1 + ((int) (Math.random() * 5));
            if (b) {
                f = m;
            }
            if (c.aMange(f)) { // vérifie si la fourmi peut manger cette nourriture (ne mange rien pour l'instant)
                this.nourriture += m;
                return true;
            }
        }
        return false;
    }

    /**
     * vérifie si l'une des cellule de l a de la nourriture
     * @param l : liste de cellules
     * @return true si l'une des cellules a de la nourriture, false sinon
     */
    public boolean checkNourritureAround (LinkedList<Cell> l) {
        for (Cell c : l) {
            if (c.getObstacle() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * vérifie si le nid se trouve parmi les cellules de l
     * @param l : liste de cellules
     * @return true si le nide se trouve parmi ces cellules
     */
    public boolean checkNidAround (LinkedList<Cell> l) {
        for (Cell c : l) {
            if (c instanceof Fourmiliere) {
                return true;
            }
        }
        return false;
    }


    /**
     * permet de connaotre la direction de la fourmi pour l'affichage
     * @param c : future cellule de la fourmi
     * @return la direction de la fourmi (0 : haut, 1 : bas, 2 : gauche, 3 : droite)
     */
    public int toDirection (Cell c) {
        int id = 0;
        if (c.getX() < this.x && c.getY() == this.y) {
            id = 2;
        }
        if (c.getX() > this.x && c.getY() == this.y) {
            id = 3;
        }
        if (c.getX() == this.x && c.getY() > this.y) {
            id = 1;
        }
        if (c.getX() == this.x && c.getY() < this.y) {
            id = 0;
        }
        return id;
    }

    /**
     * place la fourmi sur sa nouvelle cellule et dépose la phéromone en fonction de sa position par rapport à la nourriture/nid
     * @param c : nouvelle position de la fourmi
     * @param around : cellules entourant la cellule c
     * @param aroundPlus : cellules entourant plus largement la cellule c
     */
    public void setCoordonnees (Cell c, LinkedList<Cell> around, LinkedList<Cell> aroundPlus) {
        this.vie -= 1;
        this.x = c.getX();
        this.y = c.getY();
        int p = 1;
        if (this.pheromone == 0) {
            if (checkNidAround(around)) {
                p = 5;
            } else if (checkNidAround(aroundPlus)) {
                p = 3;
            }
            c.setPhenoromoneNid(p,around,aroundPlus);
        } else {
            if (checkNourritureAround(around)) {
                p = 5;
            } else if (checkNourritureAround(aroundPlus)) {
                p = 3;
            }
            c.setPheromoneNourriture(p,around,aroundPlus);
        }
    }


    
}