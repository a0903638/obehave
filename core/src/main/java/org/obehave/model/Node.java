package org.obehave.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.obehave.model.modifier.ModifierFactory;
import org.obehave.persistence.impl.NodeDaoImpl;

import java.util.*;

/**
 * A {@code Group} contains children of the type {@code T} and other groups of the same type. No duplicates are allowed, regardless if it's in the same group or in one of the subgroups.
 * <p/>
 * {@code Group} is {@see Iterable}, so using a for each loop will return every item contained in this group or one of it's subgroups.
 * @param <T> the type of the children to store in this {@code Group}
 */
@DatabaseTable(tableName = "Node", daoClass = NodeDaoImpl.class)
public class Node<T extends Displayable> implements Iterable<T>, Displayable {
    public static enum Exclusivity {
        /**
         * Multiple state actions are allowed at the same time
         */
        NOT_EXCLUSIVE,
        /**
         * Not recursive - only one element (regardless if subgroup or action) is allowed to be/contain active state actions
         */
        EXCLUSIVE,
        /**
         * Recursive - only one element is allowed to be active
         */
        TOTAL_EXCLUSIVE
    }

    @DatabaseField(columnName = "actionType")
    private Exclusivity exclusivity;

    @ForeignCollectionField(eager = true)
    private final Collection<Node<T>> children = new ArrayList<>();

    // Fields to store the actual data. Don't call them direcetly, use getData() and setData()!
    // We could have just one generic field "T data", but then there would be problems with ORMLite. This. Sucks.
    @DatabaseField(columnName = "type")
    private Class<T> dataType;

    @DatabaseField(columnName = "subject", foreign = true, foreignAutoRefresh = true)
    private Subject subject;
    @DatabaseField(columnName = "action", foreign = true, foreignAutoRefresh = true)
    private Action action;
    @DatabaseField(columnName = "modifierFactory", foreign = true, foreignAutoRefresh = true)
    private ModifierFactory modifierFactory;
    @DatabaseField(columnName = "observation", foreign = true, foreignAutoRefresh = true)
    private Observation observation;

    @DatabaseField(columnName = "title")
    private String title;

    private Node() {
        // for frameworks
    }

    public Node(Class<T> dataType) {
        this.dataType = dataType;
    }

    public Node(T data, Class<T> dataType) {
        this(dataType);
        setData(data);
    }

    /**
     * Recursively look for {@code element}.
     * @param element the element to look for
     * @return true, if the element is found either within this group itself, or in one of it's subgroups
     */
    public boolean contains(T element) {
        if (element.equals(getData())) {
            return true;
        }

        for(Node<T> node : children) {
            if (node.contains(element)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Recursively look for a {@code child}.
     * @param child the child to look for
     * @return true, if the subgroup is found either within this group itself, or in one of it's subgroups
     */
    public boolean contains(Node<T> child) {
        if (children.contains(child)) {
            return true;
        }

        for(Node<T> node : children) {
            if (node.contains(child)) {
                return true;
            }
        }

        return false;
    }

    public void setData(T data) {
        if (dataType == Subject.class && data instanceof Subject) {
            subject = (Subject) data;
        } else if (dataType == ModifierFactory.class && data instanceof ModifierFactory) {
            modifierFactory = (ModifierFactory) data;
        } else if (dataType == Action.class && data instanceof Action) {
            action = (Action) data;
        } else if (dataType == Observation.class && data instanceof Observation) {
            observation = (Observation) data;
        } else {
            throw new IllegalArgumentException("Can't set data for " + data);
        }

        title = null;
    }

    @SuppressWarnings("unchecked")
    public T getData() {
        if (dataType == Subject.class) {
            return (T) subject;
        } else if (dataType == ModifierFactory.class) {
            return (T) modifierFactory;
        } else if (dataType == Action.class) {
            return (T) action;
        } else if (dataType == Observation.class) {
            return (T) observation;
        }

        throw new IllegalArgumentException("Can't get data - dataType isn't set correctly!");
    }

    public Node<T> addChild(T data) {
        final Node<T> node = new Node<>(data, dataType);
        addChild(node);

        return node;
    }

    public boolean addChild(Node<T> node) {
        if (contains(node.getData())) {
            throw new IllegalArgumentException("node " + node.toString() + " already there");
        }

        makeToParent();

        return children.add(node);
    }

    public void makeToParent() {
        if (getData() != null) {
            children.add(new Node<>(getData(), dataType));
            setData(null);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (getData() != null) {
            makeToParent();
        }

        this.title = title;
    }

    public boolean remove(Node<T> node) {
        return children.remove(node);
    }

    public boolean remove(T data) {
        throw new UnsupportedOperationException("Has to be implemented! Data was " + data);
    }

    public Node<T> getChildren(int i) {
        return getChildren().get(i);
    }

    /**
     * Returns an unmodifiable list of all the children in this group
     * @return an unmodifiable list of all the children in this group
     * @see java.util.Collections#unmodifiableList(java.util.List)
     */
    public List<Node<T>> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children));
    }

    /**
     * This method resolves all nested groups to one flattened list.
     * @return a flattened, unmodifiable list, containing first all children of each subgroup, and then the own children
     */
    public List<T> flatten() {
        List<T> flattened = new ArrayList<>();

        for (Node<T> child : children) {
            flattened.addAll(child.flatten());
        }

        if (getData() != null) {
            flattened.add(getData());
        }

        return Collections.unmodifiableList(flattened);
    }

    @Override
    public Iterator<T> iterator() {
        // there could be a better way than to flatten the group first. But I don't want to implement a new Iterator...
        return flatten().iterator();
    }

    @Override
    public String getDisplayString() {
        return getData() != null ? getData().getDisplayString() : getTitle();
    }

    private void validateActionNode() {
        if (dataType != Action.class) {
            throw new IllegalStateException("Exlusitivity can only be set for Action nodes!");
        }
    }

    public Exclusivity getExclusivity() {
        validateActionNode();

        return exclusivity;
    }

    public void setExclusivity(Exclusivity exclusivity) {
        validateActionNode();

        if (exclusivity == null) {
            throw new IllegalArgumentException("Exclusivity must not be null!");
        }

        this.exclusivity = exclusivity;
    }
}
