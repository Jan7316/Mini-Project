package mini.app.orbis;

/**
 * Created by Jan on 05/11/2016.
 */

public class Util {

    public static <E> int indexOf(E element, E[] array) {
        for(int i=0;i<array.length;i++) {
            if(array[i].equals(element))
                return i;
        }
        return -1;
    }

    public static <E> int count(E[] array, E element) {
        int num = 0;
        for(E item : array) {
            if(item.equals(element))
                num++;
        }
        return num;
    }

    public static int count(boolean[] array, boolean element) {
        int num = 0;
        for(boolean item : array) {
            if(item == element)
                num++;
        }
        return num;
    }

}
