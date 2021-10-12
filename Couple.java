public class Couple<T , R> {
    private T element1;
    private R element2;

    public Couple(T element1, R element2){
        this.element1 = element1;
        this.element2 = element2;
    }

    public void setElement1(T element1){
        this.element1 = element1;
    }

    public void setElement2(R element2){
        this.element2 = element2;
    }

    public T getElement1(){
        return this.element1;
    }

    public R getElement2(){
        return this.element2;
    }
}
