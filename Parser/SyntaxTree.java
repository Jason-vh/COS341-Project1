package Parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SyntaxTree {

    ArrayList<Node> tree = new ArrayList<Node>();
    Node root = null;


    void addNode(String _t, String _d) {
        tree.add(new Node(_t, _d));

        if (root == null) {
            root = new Node(_t, _d);
        }
    }

    Node addNode(String _t, String _d, Node parent) {
        Node n = new Node(_t, _d);
        tree.add(n);

        if (root == null) {
            root = new Node(_t, _d);
        }

        if (parent != null)
            parent.addChild(n);
        return n;
    }

    void print() {
        for (Node n : tree) {
            System.out.println("[" + n.type + " " + n.description + "]");
        }
    }

    void DFS() {
        preorder(root);
    }

    void preorder(Node n) {
        if (n != null) {
            System.out.println(n);
            for (Node c : n.children) {
                preorder(c);
            }
        }
    }

    void generateGraph() {
        try{
            PrintWriter writer = new PrintWriter("graph.txt", "UTF-8");
            writer.println("digraph syntaxtree {");
            for (Node n : tree) {
                writer.println("ID" + n.id + " [label=\"" + (n.description.equals("NT") ? n.type : n.description.replace("\"", "\\\"")) + "\"];");
            }
            for (Node n : tree) {
                if (n.children.size() > 0) {
                    for (Node c : n.children) {
                        writer.println("ID" + n.id + " -> ID" + c.id + ";");
                    }
                }
            }
            writer.println("}");
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
}
