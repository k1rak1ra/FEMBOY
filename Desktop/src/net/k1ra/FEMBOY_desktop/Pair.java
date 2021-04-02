package net.k1ra.FEMBOY_desktop;

public class Pair {
    String first;
    String second;

    public Pair(final String f, final String s){
        first = f;
        second = s;
    }

    public static Pair create(final String f, final String s){
        return new Pair(f, s);
    }
}
