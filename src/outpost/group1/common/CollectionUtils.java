package outpost.group1.common;

import java.util.*;

public class CollectionUtils {
    public static interface Predicate<T> { boolean test(T val); };
    public static interface Score<T>     { double score(T val); };

    public static <T> List<T> filter(Collection<T> list, Predicate<T> condition) {
        List<T> result = new ArrayList<T>();

        for (T element : list) {
            if (condition.test(element)) {
                result.add(element);
            }
        }

        return result;
    }

    public static <T> T first(Collection<T> list, Predicate<T> condition) {
        for (T element : list) {
            if (condition.test(element)) {
                return element;
            }
        }

        return null;
    }

    public static <T> boolean any(Collection<T> list, Predicate<T> condition) {
        for (T element : list) {
            if (condition.test(element)) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean all(Collection<T> list, Predicate<T> condition) {
        for (T element : list) {
            if (condition.test(element)) {
                return false;
            }
        }

        return true;
    }
    public static <T> List<T> sorted(List<T> list, final Score<T> scorer) {
        Collections.sort(list, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return new Double(scorer.score(o1)).compareTo(scorer.score(o2));
            }
        });

        return list;
    }
    public static <T> List<T> sortedAscending(List<T> list, final Score<T> scorer) {
        Collections.sort(list, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return new Double(-scorer.score(o1)).compareTo(-scorer.score(o2));
            }
        });

        return list;
    }

    public static <T> int count(List<T> list, final Predicate<T> condition) {
        int count = 0;
        for (T t : list) {
            if (condition.test(t)) {
                count += 1;
            }
        }
        return count;
    }
    public static <T> double sum(List<T> list, final Score<T> scorer) {
        double result = 0.0;
        for (T t : list) {
            result += scorer.score(t);
        }
        return result;
    }

    public static <T> T best_golf(Collection<T> list, final Score<T> scorer) {
        return best(list, new Score<T>() {
            public double score(T t) { return -scorer.score(t); };
        });
    }
    public static <T> T best(Collection<T> list, Score<T> scorer) {
        T best = null;
        Double best_score = null;

        for (T element : list) {
            double score = scorer.score(element);
            if (best_score == null || score > best_score) {
                best_score = score;
                best = element;
            }
        }

        return best;
    }

    private static Random random = new Random();
    public static <T> T choice(List<T> list) {
        int stuff = random.nextInt(list.size());
        for (T t : list) {
            if (stuff == 0) return t;
            else stuff -= 1;
        }

        return list.iterator().next();
    }
}
