package outpost.group1.common;

public interface Formation {
    Point getMove(Game g, Outpost o);
    boolean isFull();
    void join(Outpost o);
    void chooseTarget();
}
