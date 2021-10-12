import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;

public class LabyrintheSave{
    // save func
    public static void saveLabyrinthe(Labyrinthe labyrinthe, File file){
        FileOutputStream fstream = null;
        try{
            try{
                // init stream with file
                fstream = new FileOutputStream(file);          
            }catch(FileNotFoundException fileErr){
                // if not file found , create file
                System.out.println("File not found!\n[Creation] File : " + file.getAbsolutePath());
                file.createNewFile();              
            }          
            // write byte on file
            ByteWriter writer = new ByteWriter();
            labyrinthe.writeDatas(writer);
            fstream.write(writer.getDatas());
        }  catch(IOException e){
            System.out.println(e.getMessage());
        }finally{
            try{
                // close on end
                fstream.close();
            }catch(IOException err){
                System.out.println(err.getMessage());
            }
        }
    }

    // save func
    public static void saveLabyrinthe(Labyrinthe labyrinthe, String name){
        // init file
        File file = new File("./save/" + name + ".fo2");
        saveLabyrinthe(labyrinthe, file);
    }

    /**
     * Load a file
     */
    public static Labyrinthe loadLabyrinthe(File file){
        // init stream to null
        FileInputStream fstream = null;
        // init labyrinthe 1x1
        Labyrinthe labyrinthe = new Labyrinthe(10,10,0);
        try{
            try{
              fstream = new FileInputStream(file);
              // init reader
              ByteReader reader = new ByteReader(fstream);
              // read data on labyrinthe
              try{
                labyrinthe.readDatas(reader);
              }catch(IllegalArgumentException noMoreData){
                  System.out.println("Le fichier n'a pas etait lu correctement.");
                  labyrinthe = new Labyrinthe(10, 10, 0);
              }
            }catch(FileNotFoundException fileErr){
                System.out.println(fileErr.getMessage());              
            }  
        }  catch(IOException e){
          System.out.println(e.getMessage());
        }finally{
          try{
              // close on end
              fstream.close();
          }catch(IOException err){
              System.out.println(err.getMessage());
          }
        } 
        return labyrinthe;
    }

    /**
     * Load a file in './save/' folder // return null if no file found
     */
    public static Labyrinthe loadLabyrinthe(String name){
        name = "./save/" + name + ".fo2";
        File file = new File(name);
        return loadLabyrinthe(file);
    }

    /**
     * max value = 127
     */
    private static int MAXIMUM_BYTE_1 = Byte.MAX_VALUE;
    /**
     * max value = 127²
     */
    private static int MAXIMUM_BYTE_2 = Byte.MAX_VALUE * Byte.MAX_VALUE;

    /**
     * class CustomData (class pour stocker les donées)
     */
    private static class CustomData {
        protected byte[] datas;

        public int available(){
            return datas.length;
        }

        public byte[] getDatas(){
            return datas;
        }
    }

    /**
     * <p>Reader class ByteReader extends CustomData</p>
     * <p>for reading data</p>
     */
    public static class ByteReader extends CustomData{
        // init with empty data
        public ByteReader(){
            datas = new byte[0];
        }

        // init with data
        public ByteReader(byte[] datas){
            this.datas = datas;
        }

        // init with stream
        /**
         * <p>read all byte in stream and put in byte[] datas</p>
         */
        public ByteReader(FileInputStream stream) throws IOException{
            int len = stream.available();
            datas = new byte[len];
            for(int i = 0;i<len;i++){
                datas[i] = (byte)stream.read();
            }            
        }

        // private func
        // read 'quantity' byte(s) on data
        private int read(int quantity){
            // if no more data throw exception
            if(available() <= 0 || available() < quantity)
                throw new IllegalArgumentException("No more data to read");
            
            // init byte array with 'quantity' required
            byte[] values = new byte[quantity];
            // init byte array with byte in datas
            for(int i = 0;i<quantity;i++)
                values[i] = datas[i];

            // remove readed data
            datas = subarray(datas, quantity);
            
            // return byte array (nombre base 127) to decimal 
            return toIntValue(values);
        }

        // read 1 byte
        /**
         * read 1 byte in data (remove byte read)
         */
        public int read1(){
            return read(1);
        }

        // read 2 byte
        /**
         * read 2 byte in data (remove bytes read)
         */
        public int read2(){
            return read(2);
        }
    }

    // writer class
    /**
     * <p>Writer class ByteWriter extends CustomData</p>
     * <p>for writing data</p>
     */
    public static class ByteWriter extends CustomData{
        // init with empty data
        public ByteWriter(){
            datas = new byte[0];
        }

        // init with data
        public ByteWriter(byte[] datas){
            this.datas = datas;
        }

        // write (concat in right)
        private void write(byte[] values){
            datas = concat(datas, values);
        }

        // write on 1 byte max value 127 min value -128
        public void write1(int value){
            if(value > MAXIMUM_BYTE_1)
                throw new IllegalArgumentException("too high value ! Maximum value is '" + MAXIMUM_BYTE_1 + "'");

            if(value < (-MAXIMUM_BYTE_1) - 1)
                throw new IllegalArgumentException("too low value ! Minimum valus is '" +((-MAXIMUM_BYTE_1) - 1) + "'");
            
            write(new byte[]{(byte)value});
        }

        // write on 2 byte max value 127² min value 0
        public void write2(int value){            
            if(value > MAXIMUM_BYTE_2)
                throw new IllegalArgumentException("too high value ! Maximum value is '" + MAXIMUM_BYTE_2 + "'");

            if(value < 0)
                throw new IllegalArgumentException("too low value ! Minimum valus is '0'");
            
            write(toByteArray(value,2));
        }
    }

    /**
     * <p>convert byte array to int value</p>
     * <p>convert byte array (base 127 to decimal(base 10))</p>
     */
    public static int toIntValue(byte[] arr){
        int sum = 0;

        while(arr.length > 0){
            // conversion base 127 -> base 10
            // on ajoute à 'sum' la multiplication de arr[0] par 127 à la puissance arr.length - 1             
            sum += (arr[0] * Math.pow(Byte.MAX_VALUE, arr.length - 1));
            // on retire le premier terme du tableau
            arr = subarray(arr, 1);
        }

        return sum;
    }

    // only value > 0
    /**
     * convert 'value'(base 10) to byte array (base 127) with 'arrayLength' required
     */
    public static byte[] toByteArray(int value, int arrayLength){
        byte[] arr = new byte[0];

        while(value > 1 && arr.length < arrayLength){
            // concat to arr value % 124
            arr = concat(new byte[]{(byte)(value % Byte.MAX_VALUE)}, arr);
            // divide value by 127
            value /= Byte.MAX_VALUE;
        }

        // fill with 0
        while(arr.length < arrayLength){
            arr = concat(new byte[]{0}, arr);
        }

        return arr;
    }

    /**
     * concat 'arr1' with 'arr2' (arr1 + arr2)
     */
    public static byte[] concat(byte[] arr1, byte[] arr2){
        byte[] newArr = new byte[arr1.length + arr2.length];

        for(int i = 0;i<newArr.length;i++){
            if(i >= arr1.length){
                newArr[i] = arr2[i - arr1.length];
            }else{
                newArr[i] = arr1[i];
            }
        }
        
        return newArr;
    }

    // retourne la partie droite du tableau à partir de l'indice spécifié
    public static byte[] subarray(byte[] array, int startIndex){
        if(startIndex >= array.length)
            return new byte[0];

        if(startIndex <= 0)
            return array;

        byte[] newArray = new byte[array.length - startIndex];
        for(int i = 0;i<newArray.length;i++){
            newArray[i] = array[i + startIndex];
        }

        return newArray;
    }

    public static boolean isCorrect(File f){
        if(f==null||f.getName().length()<4){return false;}
        return f.getName().substring(f.getName().length()-3, f.getName().length()).equals("fo2");
    }

}
 