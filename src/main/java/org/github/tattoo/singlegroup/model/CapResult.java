package org.github.tattoo.singlegroup.model;

public class CapResult {
    private final int redCaps;
    private final int blueCaps;

    public CapResult(int redCaps, int blueCaps) {
        this.redCaps = redCaps;
        this.blueCaps = blueCaps;
    }

    public int getRedCaps() {
        return redCaps;
    }

    public int getBlueCaps() {
        return blueCaps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapResult)) return false;

        CapResult that = (CapResult) o;

        if (redCaps != that.redCaps) return false;
        if (blueCaps != that.blueCaps) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = redCaps;
        result = 31 * result + blueCaps;
        return result;
    }

    @Override
    public String toString() {
        return "Score:" + redCaps + "-" + blueCaps;
    }
}
