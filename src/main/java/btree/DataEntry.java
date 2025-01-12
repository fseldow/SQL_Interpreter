package btree;

import java.util.ArrayList;

class DataEntry {
    int key;
    ArrayList<Rid> rids;

    public DataEntry(int key, ArrayList<Rid>rids) {
        this.key = key;
        this.rids = new ArrayList<>(rids);
    }

    public int getKey() {
        return key;
    }

    public ArrayList<Rid> getRids() {
        return rids;
    }

    public Rid getRidAtIndex(int index) {
        return rids.get(index);
    }

    @Override
    public String toString() {
        return "DataEntry{" +
                "key=" + key +
                ", rids=" + rids +
                '}';
    }
}