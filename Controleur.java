import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Ici que se gère les connections entre le modèle et la vue
 * Listeners; manipulation des getters/setters
 */
public class Controleur{

    public final static int LABYWIDTH = 30, LABYHEIGHT = 30;  
    public final static int NBOBSTACLE = 500;

    private final Timer temps;
    private final Labyrinthe modele;
    private final Fenetre vue;

    public Controleur (){

        this.modele = new Labyrinthe(LABYWIDTH, LABYHEIGHT, NBOBSTACLE);
        this.vue = new Fenetre(modele);
        this.modele.initFourmiliere(10); // par défaut
        this.modele.setPlusNourriture(1,5000); // par défaut 
        LabyrintheSave.saveLabyrinthe(this.modele,new File("./save/saveFile"));
        temps = new Timer(600,new ActionListener(){
            /**
             * Ici se fera tous les appels de fonctions demander par le simulateur 
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!vue.estEnPause()){ 
                    modele.getFourmiliere().getFourmi(modele,true); // true : naissance de nouvelles fourmis
                    modele.deplacement(vue.typeAlgo(),!vue.isUnlimitedFood(),vue.isSurvivalMode(),vue.getTaileMemoireAlgo()); // premier algo par défaut, deuxieme en cours de construction, 
                    // 1er boolean : true = mange, false = ne mange pas
                    // 2e boolean : true = mortelle, false = immortelle
                    // derniere valeur (10 par défaut) = taille de la memoire
                    vue.updatePopInfo(modele.Fourmis().size());
                    vue.changementEtat();
                }
                temps.setDelay(vue.getSpeedValue());//regule la vitesse de la simulation
            }
        });

        gestionnaireDesEvenement();
        temps.start(); //lance le chrono
        affInfo();

    }

    /**
     * Ici sera gérer les évènements qui sont indépendants du Timer
     */
    private void gestionnaireDesEvenement(){
        //Creation à partir des entrers de l'utilisateur
        this.vue.confAcceptListener(event->{
            if(vue.isCorrect()){
                LinkedList<Integer> values = vue.getNewLabValues();
                System.out.println(values.get(0)+"     "+values.get(1));
                modele.reloadLaby(
                    (values.get(0)<=values.get(1)/2)?values.get(1)/2+1:values.get(0),
                    (values.get(1)<=values.get(0)/2)?values.get(0)/2+1:values.get(1),
                    values.get(2)
                );
            
                modele.setNbFourmisFourmiliere(values.get(3));//nb fourmi
                int nbNourr =(values.get(4)<(values.get(0)*values.get(1)-values.get(2))/2)?values.get(4):(values.get(0)*values.get(1)-values.get(2))/4;
                modele.setPlusNourriture(nbNourr,5000);//nb nourriture
                LabyrintheSave.saveLabyrinthe(this.modele,new File("./save/saveFile")); //update
                vue.resetAll();
                vue.refreshScreen();
                vue.setPaneCreatorVisible(false);
                System.out.println("[Creation] Labyrinthe avec commme configuration :\n"
                +"\tLongueur : "+values.get(1)+"\n\tLargeur : "
                +values.get(0)+"\n\tNombre d'obstacle : "+values.get(2));
            }
        });

        try{
            JFileChooser chooser = new JFileChooser("./save");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Sauvegarde","fo2");
            chooser.setFileFilter(filter);
            vue.addMenuActionListenerTo("open", l->{
                int res = chooser.showOpenDialog(vue); //affiche dans fenetre le FileChooser
                if(LabyrintheSave.isCorrect(chooser.getSelectedFile())&& res == JFileChooser.OPEN_DIALOG){
                    try{
                        System.out.println("\n[Creation] un labyrinthe avec pour modèle le fichier "+chooser.getSelectedFile().getName());
                        modele.reloadLaby(LabyrintheSave.loadLabyrinthe(chooser.getSelectedFile())); //update
                        LabyrintheSave.saveLabyrinthe(this.modele,new File("./save/saveFile")); //update
                        vue.resetAll();
                        vue.refreshScreen();
                    }catch (NullPointerException e){
                        System.err.println("Erreur lors de la lecture du fichier "+chooser.getSelectedFile() );
                    }
                }else if (chooser.getSelectedFile()!=null&&res == JFileChooser.OPEN_DIALOG){
                    System.err.println("Erreur : seul les fichier *.fo2 sont prises en charge");
                }
            });

            JFileChooser save = new JFileChooser("./save");
            save.setFileFilter(filter);
            save.setDialogTitle("Save");
            vue.addMenuActionListenerTo("save", l->{
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_mmss");
                Date date = new Date();
                String name = "svg_"+dateFormat.format(date) +".fo2";
                System.out.println("\tcréation de la sauvegarde ./save/"+name);
                save.setSelectedFile(new File(name));
                
                if(save.showSaveDialog(vue) == JFileChooser.APPROVE_OPTION){
                    try{
                        LabyrintheSave.saveLabyrinthe(modele, save.getSelectedFile());
                    }catch (IllegalArgumentException e ){
                        System.err.println("Erreur lors de la sauvegarde ");
                    }
                }
            });

            vue.addMenuActionListenerTo("screen", l->{
                try{
                    Robot robot = new Robot();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy mmss");
                    String fname ="./save/"+dateFormat.format(new Date())+".jpg";
                    ImageIO.write(robot.createScreenCapture(
                        new Rectangle(vue.getLocation().x,vue.getLocation().y,Fenetre.LARGEUR,Fenetre.LONGUEUR)), 
                        "jpg", new File(fname));
                    System.out.println("Nouveau fichier créer : ./save/"+fname);
                }catch (AWTException | IOException e){
                    e.printStackTrace();
                }
            });
            vue.addMenuActionListenerTo("draw", l->{  vue.setPrincipalToScreen(false);  });
        }catch(Exception e){
            e.printStackTrace();
        }

        this.vue.confZoomListener(l->{
            vue.setMapSourisZoom(modele.getLargeur()/vue.getZoomValue(), modele.getHauteur()/vue.getZoomValue(),vue.getZoomValue());
        });

        this.vue.addRadiosListener(
            pic->{
                vue.setGraphicBigMap(true);
                vue.refreshScreen();
            }, 
            nestview->{
                vue.setGraphicBigMap(false);
                vue.setNestViewBigMap(true);
                vue.refreshScreen();
            },
            foodview->{
                vue.setGraphicBigMap(false);
                vue.setNestViewBigMap(false);
                vue.refreshScreen();
            }
        );

        this.vue.addResetListener(l->{
            modele.reloadLaby(LabyrintheSave.loadLabyrinthe(new File("./save/saveFile"))); //reprise
            vue.resetAll();
            vue.refreshScreen();
        });

        this.vue.addValidDrawListener(l->{
            if(vue.getDrawFourmiliereIsSet()){ //reste la verif
                Labyrinthe lab = new Labyrinthe(vue.getArrayOfDrawLab(), vue.getNbFourmiByDraw());
                modele.reloadLaby(lab);
                //LabyrintheSave.saveLabyrinthe(this.modele,new File("./save/saveFile")); //update
                vue.resetAll();
                vue.refreshScreen();                
            }else{
                String wrg = "Le labyrinthe déssiner est invalide";
                JOptionPane.showMessageDialog(vue,wrg,"Erreur", JOptionPane.WARNING_MESSAGE);
                System.err.println(wrg);
            }
            vue.setPrincipalToScreen(true);
            
        });
    }
    
    private void affInfo(){
        System.out.println("[Creation] Labyrinthe avec commme configuration :\n"
            +"\tLongueur : "+LABYHEIGHT+"\n\tLargeur : "
            +LABYWIDTH+"\n\tNombre d'obstacle : "+NBOBSTACLE);
    }

}