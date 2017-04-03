package Parser;

import java.util.ArrayList;
import java.util.Arrays;

public class Node {
    static int nodeNumber = 0;
    String type;
    String description;
    int pointer;
    int id;
    ArrayList<Node> children = new ArrayList<Node>();

    Node(String _t, String _d) {
        type = _t;
        description = _d;
        id = nodeNumber++;
    }

    void addChild(Node n) {
        children.add(n);
    }

    @Override
    public String toString() {
        return "[" + type + " - " + description + " ]";
    }

    public void print() {
        System.out.println(toString() + " {" + Arrays.toString(children.toArray()) + "}");
    }
}
