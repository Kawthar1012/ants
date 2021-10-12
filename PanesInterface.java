/**
 * Est implémenter par les Objets de type {@code Pane...}
 */
interface PanesInterface{
    
    /**
     * <p>Ici est traiter les comportements de l'utilisateur vis à vis du
     * {@code Panel}</p>
     * <p>En résumer l'ajout des {@code Listener} de java Swing</p>
     * <p> Lance une excéption si utiliser sans <code>Override</code></p>
     */
    default void configureListener(){
        throw new UnsupportedOperationException("Fonction "+this.getClass().getName()
            +".configureListener() n'est pas Overridé ");
    }
}