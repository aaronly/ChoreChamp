package us.echols.chorechamp;

public class Achievement {

    private final String name;
    private final int count;
    private boolean complete = false;

    public Achievement(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

}
