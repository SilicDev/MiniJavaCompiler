package pgdp.minijava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SyntaxTreeNode implements Iterable<SyntaxTreeNode>{
    private SyntaxTreeNode[] children;
    private final String value;
    private final Type type;

    public SyntaxTreeNode(Type type, String value) {
        this.children = new SyntaxTreeNode[]{};
        this.value = value;
        this.type = type;
    }

    public SyntaxTreeNode getChild(int id) {
        return children[id];
    }

    public void addChild(SyntaxTreeNode node) {

        children = (Arrays.copyOf(this.children, this.children.length + 1));
        children[children.length - 1] = node;
    }

    public int getNumberChildren() {
        return children.length;
    }

    public boolean isLeaf() {
        return children.length == 0;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if(children.length != 0) {
            out.append("[");
            for (SyntaxTreeNode node : children) {
                out.append(node.toString()).append(", ");
            }
            out = new StringBuilder(out.substring(0, out.length() - 2));
            out.append("]");
            return "{ " + type + ":" + value + " " + out + " }";
        }
        return "{ " + type + ":" + value + " }";
    }

    @Override
    public Iterator<SyntaxTreeNode> iterator() {
        return new NodeIterator(this);
    }

    private static class NodeIterator implements Iterator<SyntaxTreeNode> {
        ArrayList<NodeIterator> iterators = new ArrayList<>();
        boolean outThis = false;
        SyntaxTreeNode node;

        public NodeIterator(SyntaxTreeNode node) {
            for (int i = 0; i < node.getNumberChildren(); i++) {
                iterators.add(new NodeIterator(node.getChild(i)));
            }
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            if(!outThis) {
                return true;
            }
            for (NodeIterator iterator : iterators) {
                if(iterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public SyntaxTreeNode next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            if(!outThis) {
                return node;
            }
            for (NodeIterator iterator : iterators) {
                if(iterator.hasNext()) {
                    return iterator.next();
                }
            }
            throw new NoSuchElementException();
        }
    }

    public enum Type {
        PROGRAM,
        DECL,
        NAME,
        NUMBER,
        BOOL,
        EXPR,
        COND,
        COMP,
        TYPE,
        STMT,
        LABEL,
        SYMBOL,
        FUNCCALL
    }
}
