package entities;

public class Node<T> {
    public T data;
    public Node<T> prev;
    public Node<T> next;

    public Node(Node<T> node) {
        if (node == null) {
            return;
        }
        this.prev = node.prev;
        this.data = node.data;
        this.next = node.next;
    }
    
    public Node(Node<T> prev, T data, Node<T> next) {
        this.prev = prev;
        this.data = data;
        this.next = next;
    }

    @Override
    public String toString() {
        T prevData = (prev != null) ? prev.data : null;
        T curData = data;
        T nextData = (next != null) ? next.data : null;
        return "Node{" +
                "prev<" + (prevData != null ? ("'" + prevData + "'") : "null") + ">, " +
                "data<" + (curData != null ? ("'" + data + "'") : "null") + ">, " +
                "next<" + (nextData != null ? ("'" + nextData + "'") : "null") + ">" +
                "}";
    }
}
