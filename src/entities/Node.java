package entities;

public class Node<T> {
    private T data;
    private Node<T> prev;
    private Node<T> next;

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

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }

    public Node<T> getPrev() {
        return prev;
    }

    public void setPrev(Node<T> prev) {
        this.prev = prev;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
