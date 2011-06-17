package agentspring.engine.graphstore;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static <T> List<T> asList(Iterable<T> iterable) {
        List<T> list;
        if (iterable instanceof List<?>) {
            list = (List<T>) iterable;
        } else {
            list = new ArrayList<T>();
            for (T t : iterable) {
                list.add(t);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends T> List<E> asCastedList(Iterable<T> iterable) {
        List<E> list = new ArrayList<E>();
        for (T t : iterable) {
            list.add((E) t);
        }
        return list;
    }

    public static <E, T extends E> List<E> asDownCastedList(Iterable<T> iterable) {
        List<E> list = new ArrayList<E>();
        for (T t : iterable) {
            list.add((E) t);
        }
        return list;
    }

}
