package mini.app.orbis;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    public static <E> int[] allIndices(E element, E[] array) {
        int[] list = new int[count(array, element)];
        int j = 0;
        for(int i=0;i<array.length;i++) {
            if(array[i].equals(element)) {
                list[j] = i;
                j++;
            }
        }
        return list;
    }

    public static int[] allIndices(boolean element, boolean[] array) {
        int[] list = new int[count(array, element)];
        int j = 0;
        for(int i=0;i<array.length;i++) {
            if(array[i] == element) {
                list[j] = i;
                j++;
            }
        }
        return list;
    }

    public static <E> boolean contains(E[] array, E element) {
        for(E item : array) {
            if(item.equals(element))
                return true;
        }
        return false;
    }

    public static boolean contains(int[] array, int element) {
        for(int item : array) {
            if(item == element)
                return true;
        }
        return false;
    }

}
