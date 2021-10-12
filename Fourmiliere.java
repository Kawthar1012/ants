public class Fourmiliere extends Cell {
    private int nourriture;
    private int nbFourmis;

    public Fourmiliere (int x, int y, int n) {
        super(x,y);
        this.nbFourmis = n;
        this.nourriture = 0;
    }

    public Fourmiliere (int x, int y) {
        super(x,y);
        this.nbFourmis = 1 + (int) (Math.random()*50);
        this.nourriture = 0;
    }

    public void setNourriture (int n) {
        this.nourriture += n;
    }

    public void setNbFourmis(int n){
        this.nbFourmis = n;
    }

    @Override
    public String toString(){
        return "FM";
    }

    /**
     * au départ fait sortir nbFourmi fourmis du nid
     * ensuite vérifie les conditions permettant la naissance d'une fourmi et en fait sortir une si elles sont réunies
     * @param lab : labyrinthe dans lequel apparaissent les fourmis
     * @param naissance : true si les fourmis peuvent naître, false sinon
     * @return une nouvelle fourmi
     */
    public Fourmi getFourmi (Labyrinthe lab, boolean naissance) {
        if (naissance && this.nourriture > 1000) {
            Fourmi f = new Fourmi(this.getX(),this.getY());
            lab.Fourmis().add(f);
            this.nourriture = 0;
            return f;
        }
        if (this.nbFourmis > 0) {
            this.nbFourmis--;
            Fourmi f = new Fourmi(this.getX(),this.getY());
            lab.Fourmis().add(f);
            return f;
        }
        return null;
    }

    /**
     * <p>écriture des données de la fourmiliere sur le writer</p>
     * <p>2 byte pour le nombre de fourmi</p>
     * <p>2 byte pour la nourriture</p>
     */
    public void writeDatas(LabyrintheSave.ByteWriter writer){
        writer.write2(nbFourmis);
        writer.write2(nourriture);
    }

    public void readDatas(LabyrintheSave.ByteReader reader){
        nbFourmis = reader.read2();
        nourriture = reader.read2();
    }


    
}