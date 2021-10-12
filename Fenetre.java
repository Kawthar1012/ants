import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import java.awt.Font;
import java.awt.GridLayout;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeListener;

/**
 * Sur le principe de MVC, cette classe servira de fenêtre d'affichage à notre Projet
 * Donner les getter/setter necessaires afin de manipuler les données plus simplement
 */
public class Fenetre extends JFrame{
    /**
     * Pour eviter le warning lors de la compilation avec -Xlint
     */
    private static final long serialVersionUID = 3269040576202312270L;
    public final static int LARGEUR = Toolkit.getDefaultToolkit().getScreenSize().width-200;
    public final static int LONGUEUR = Toolkit.getDefaultToolkit().getScreenSize().height-100;

    private final JPanel principal;
    private final SandBox secondaire;
    private final BigMap bigMap;
    private final MiniMap miniMap;
    private final PaneControl paneControl;
    private final PaneExtra paneExtra;
    private final PaneRunner paneRunner;
    private final InternalPane paneCreator;
    private final Menu menu;
    private final JLabel population;

    /**
     * Creer une fenetre visible
     * @param modele {@code Labyrinthe}
     * @param width int largeur initiale
     * @param heigth int longueur initiale 
     */
    public Fenetre(Labyrinthe modele){
        int width =LARGEUR;
        int heigth = LONGUEUR;
        setVisible(true);
        setSize(width,heigth);
        setTitle("Simulateur de Fourmi");
        try{
            setIconImage(ImageIO.read(new File("./Images/ant_icon.png")) );
        }catch (IOException e){
            System.out.println("Vérifiez que le fichier ci-dessous est bien présent :"+
                "\n./Images/ant_icon.png");
        }
        setResizable(false); //desactive l'agrandissement de la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.principal = new JPanel(null);
        this.secondaire = new SandBox();
        this.getContentPane().add(this.principal);
        width -=5;  heigth-=60; //question de design

        //init 
        this.population = new JLabel();
        JPanel empl = new JPanel(new GridLayout(1,1));
        this.bigMap = new BigMap(5,5,width*2/3-10,heigth,modele);
        this.miniMap = new MiniMap(width*2/3,heigth/15,width/3,heigth/2,modele);
        this.paneControl = new PaneControl(width*2/3,heigth*3/5+40,width/3,heigth/5);
        this.paneExtra = new PaneExtra(width*5/6,heigth*7/8,width/6,heigth/10);
        this.paneRunner = new PaneRunner(width*2/3,5+heigth*7/8,width/6,heigth/10);
        this.paneCreator = new InternalPane();
        this.menu = new Menu();

        this.bigMap.changeZoneValue(miniMap.getEmplSourie());//pour permettre l'affichage 
        //add
        empl.setBounds(width*2/3,heigth/100,width/4,30);
        empl.add(population);
        population.setFont(new Font("Georgia", Font.TYPE1_FONT, 12));

        setJMenuBar(menu); //met en place le JMenu
        ajouterEdansT(this.principal,bigMap,miniMap,paneControl,paneExtra,paneRunner,paneCreator,empl);
        configBigMapViewUpdate();
        affInfo(width,heigth);
    }

    public void updatePopInfo(int n){
        population.setText("Population de fourmi: "+n);
    }

    public int[][] getArrayOfDrawLab(){
        return secondaire.getTabofLab();
    }

    public int getNbFourmiByDraw(){
        return secondaire.getNbFourmi();
    }

    public boolean getDrawFourmiliereIsSet(){
        return secondaire.aUneFourmiliere();
    }

    /**
     * Permet de switcher de l'écran principal à l'écran secondaire
     * @param b boolean
     */
    public void setPrincipalToScreen(boolean b){
        if (b) setContentPane(this.principal);  else  setContentPane(this.secondaire);
        repaint();
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
     * Informe sur le contenue des informations donner par l'utilisateur
     * @return  LinkedList<Integer>
     */
    public LinkedList<Integer> getNewLabValues(){
        LinkedList<Integer> res = new LinkedList<>();
        paneCreator.getValuesEntry().forEach((key,value)->{
            res.add(value);
        });
        return res;
    }

    /**
     * Renvoie true si tout les informations renseignés sont corrects
     * @return boolean
     */
    public boolean isCorrect(){
        return paneCreator.isCorrect();
    }

    /**
     * permet d'actionner le {@code JButton} accept
     * @param l ActionListener
     */
    public void confAcceptListener(ActionListener l){
        paneCreator.confAcceptListener(l);
    }

    public void confZoomListener(ChangeListener l){
        paneControl.confZoomListener(l);
    }

    public void setPaneCreatorVisible(boolean b){
        paneCreator.setVisible(b);
    }

    /**
     * Gère la manière dont {@code BigMap} est dessinée
     * @return boolean
     */
    public boolean estAffichageGraphique(){
        return this.paneExtra.getState();
    }

    /**
     * Vrai si le boutton Pause a été touché
     * @return boolean 
     */
    public boolean estEnPause(){
        return !this.paneRunner.isRunning()||(getContentPane() instanceof SandBox);
    }

    public void addResetListener(ActionListener l){
        this.paneRunner.addResetListener(l);
    }

    public void addValidDrawListener(ActionListener l){
        secondaire.addValidDrawListener(l);
    }

    /**
     * repaint ({@code BigMap}  {@code MiniMap}  {@code Fenetre})
     */
    public void refreshScreen(){
        this.bigMap.repaint();
        this.miniMap.repaint();
        repaint();
    }

    /**
     * Ici se fera tout les appels nécessaires pour passer d'un état à une autre
     */
    public void changementEtat(){
        refreshScreen();   
        this.paneRunner.unTour();  //avance le compteur
    }

    /**
     * Renseigne sur la valeur du {@code JSlider} dans {@code PaneControl}
     * @return int 
     */
    public int getSpeedValue(){
        return this.paneControl.getVitesse();
    }

    /**
     * A appeler après la génération d'une nouvelle grille de labyrinthe
     */
    public void resetAll(){
        this.paneRunner.restartCount();
        this.miniMap.resetEmplSouris();
        this.bigMap.changeZoneValue(miniMap.getEmplSourie());
    }
    
    /**
     * Permet de raffrachir la vue de la Map après chngement de la zone du sourie
     */
    private void configBigMapViewUpdate(){
        this.miniMap.addMouseListener(new Comportement(){
            @Override
            public void mouseClicked(MouseEvent e) {
                bigMap.changeZoneValue(miniMap.getEmplSourie());
                repaint();
            }
        });
        this.principal.addMouseMotionListener(new  Comportement(){
            @Override
            public void mouseMoved(MouseEvent e) {
                if(!paneCreator.isVisible()){
                    bigMap.setVisible(true);
                }
            }
        });
    }

    public int getZoomValue(){
        return paneControl.getZoomLevel();
    }

    public void addRadiosListener(ActionListener pic,ActionListener nest,ActionListener food){
        this.paneExtra.setRadiosListener(pic, nest, food);
    }

    public void setGraphicBigMap(boolean b){
        String s = (b)?"garphique":"vue sur les phéromones";
        System.out.println("\tAffichage de la carte en mode "+s+"\n");
        this.bigMap.setGraphique(b);
    }

    public void setNestViewBigMap(boolean b){
        this.bigMap.setNestView(b);
    }

    /**
     * Permet d'élargir ou réduire les champs de la sourie
     * et change l'affichage au niveau de {@code BigMap}
     * @param width int 
     * @param height int
     */
    public void setMapSourisZoom(int width, int height,int scale){
        try{
            this.miniMap.setSourisWH(width, height); //change la longeur et largeur de la MiniMap.Zone
            this.miniMap.setZoomScale(scale); //change la valeur du champ coefSize
            this.bigMap.changeZoneValue(miniMap.getEmplSourie()); //Met à jour l'affichage réel
            repaint();
        }catch(IllegalArgumentException e){
            System.out.println("\nErreur au niveau du zoom + : Eloignez vous de la zone des bordures du tableau ");
        }
    }

    private void affInfo(int w, int h){
        System.out.println("Lancement du projet dans un environement "+System.getProperty("os.name")+ 
            " choix de la taile du fenêtre[width="+w+"; heigth="+h+"]");
    }


    /**
     * <p>Outils permettant d'accèder aux composants du menu </p>
     *  name = save|open
     * @param name String
     * @param l ActionListener
     * @throws Exception
     */
    public void addMenuActionListenerTo(String name,ActionListener l) throws Exception {
        menu.addMenuActionListenerTo(name, l);
    }

    /**
     * Permet au modele de savoir si la nourriture doit disparaitre
     * @return boolean
     */
    public boolean isUnlimitedFood(){
        return menu.cb1MenuItem.isSelected();
    }

    public boolean isSurvivalMode(){
        return menu.cb2MenuItem.isSelected();
    }

    /**
     * Les {@code JRadioButtonMenuItem} algo1 et 2
     * @return int 1|2 
     */
    public int typeAlgo(){
        return (menu.algo1.isSelected())?1:2;
    }

    public int getTaileMemoireAlgo(){
        return menu.tailleMemoire;
    }

    private class Menu extends JMenuBar{
        private static final long serialVersionUID = 3269040576202312270L;
        private int tailleMemoire;
        JMenuItem nouv = new JMenuItem("New ", KeyEvent.VK_N);
        JMenuItem open = new JMenuItem("Open ", KeyEvent.VK_O);
        JMenuItem save = new JMenuItem("Save ", KeyEvent.VK_S);
        JMenuItem draw = new JMenuItem("Draw a maze ");
        JButton screen = new JButton("Screenshot");
        JCheckBoxMenuItem cb1MenuItem = new JCheckBoxMenuItem("Unlimited food");
        JCheckBoxMenuItem cb2MenuItem = new JCheckBoxMenuItem("Mode survival");
        JRadioButtonMenuItem algo1= new JRadioButtonMenuItem("Algo (sans memoire)"),algo2= new JRadioButtonMenuItem("Algo (avec memoire)"); 
        private Menu(){
            JMenu menu  = new JMenu("Options");
            JMenuItem exit = new JMenuItem("Exit ");
            this.tailleMemoire = 10;  //defult
            //add
            menu.setMnemonic(KeyEvent.VK_O); //Alt+O
            add(menu); add(screen);
            ajouterEdansT(menu, nouv,open,save,draw);

            //groupe de radio
            menu.addSeparator();
            ButtonGroup group = new ButtonGroup();
            algo1.setSelected(true);
            group.add(algo1);
            group.add(algo2);
            menu.add(algo1);
            menu.add(algo2);

            menu.addSeparator();
            cb1MenuItem.setSelected(true);
            menu.add(cb1MenuItem);
            menu.add(cb2MenuItem);

            menu.addSeparator();
            menu.add(exit);

            //listener
            nouv.addActionListener(act->{
                bigMap.setVisible(false);
                paneCreator.setVisible(true);
            });
            exit.addActionListener(l->{
                System.exit(0);
            });
            algo2.addActionListener(l->{
                try {
                    String res = JOptionPane.showInputDialog(Fenetre.this, "Veuillez renseigner un entier (10 par default)",
                             "Definition de la taille memoire", JOptionPane.QUESTION_MESSAGE);
                    tailleMemoire = Integer.parseInt(res);
                } catch (HeadlessException | NumberFormatException  e) {
                    System.err.println("Erreur la valeurs renseignée n'est pas un entier");
                }finally{
                    tailleMemoire = 10; //default
                } 
                
            });  
        }
        /**
         * <p>Outils permettant d'accèder aux composants du menu </p>
         *  name = save|open
         * @param name String
         * @param l ActionListener
         * @throws Exception
         */
        private void addMenuActionListenerTo(String name,ActionListener l) throws Exception {
            switch(name){
                case "open" : open.addActionListener(l);break;
                case "save" : save.addActionListener(l);break;
                case "screen" : screen.addActionListener(l);break;
                case "draw" : draw.addActionListener(l); break;
                default : throw new Exception(name+" n'est pas prise en charge !"); 
            }
        }
    }
    
}