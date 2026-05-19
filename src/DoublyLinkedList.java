// Doubly linked list used to store high scores in descending order.
public class DoublyLinkedList
{

    private class Node
    {
        HighScoreEntry data;
        Node prev;
        Node next;

        Node(HighScoreEntry data)
        {
            this.data = data;
        }
    }

    private Node head;
    private Node tail;

    // Inserts a new entry maintaining descending score order (highest first).
    public void insertSorted(HighScoreEntry entry)
    {
        Node newNode = new Node(entry);
        if (head == null)
        {
            head = tail = newNode;
            return;
        }

        Node current = head;
        while (current != null && current.data.getScore() >= entry.getScore())
        {
            current = current.next;
        }

        if (current == head)
        {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        else if (current == null)
        {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        else
        {
            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
        }
    }

    // Converts the linked list to an array for saving to file and displaying scores.
    public HighScoreEntry[] toArray()
    {
        int count = 0;
        Node current = head;
        while (current != null)
        {
            count++;
            current = current.next;
        }

        HighScoreEntry[] entries = new HighScoreEntry[count];
        current = head;
        int entryIndex = 0;
        while (current != null)
        {
            entries[entryIndex++] = current.data;
            current = current.next;
        }
        return entries;
    }
}
