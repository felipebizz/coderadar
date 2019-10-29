package io.reflectoring.coderadar.dependencyMap.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class Node {

    private String filename;
    private String path;
    private String packageName;
    private int level;
    private List<Node> children;
    private List<NodeDTO> dependencies;

    public Node(String filename, String path, String packageName) {
        this.filename = filename;
        this.path = path;
        this.packageName = packageName;
        children = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public Node() {
    }

    /**
     * Check if this Node-object has children.
     *
     * @return true if this Node-object has children.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Check if this Node-object has dependencies.
     *
     * @return true if this Node-object has dependencies.
     */
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    /**
     * Add a Node-object to this Node-object's children.
     *
     * @param node Node-object to add as a child.
     */
    public void addToChildren(Node node) {
        children.add(node);
    }

    /**
     * Add a Node-object to this Node-object's children.
     *
     * @param nodes Node-object to add as children.
     */
    public void addToChildren(List<Node> nodes) {
        children.addAll(nodes);
    }

    /**
     * Add a Node-object to this Node-object's dependencies.
     *
     * @param node path of Node-object to add as a dependency.
     */
    public void addToDependencies(NodeDTO node) {
        dependencies.add(node);
    }

    /**
     * Add Node-objects to this Node-object's dependencies.
     *
     * @param nodes List of paths of Node-objects to add as dependencies.
     */
    public void addToDependencies(List<NodeDTO> nodes) {
        dependencies.addAll(nodes);
    }

    /**
     * Finds a child Node-object with a given filename.
     *
     * @param name filename to use.
     * @return found Node-object. Returns null if no Node-object found.
     */
    public Node getChildByName(String name) {
        return hasChildren() ? children.stream().filter(child -> child.getFilename().equals(name)).findFirst().orElse(null) : null;
    }

    /**
     * Searches a Node-object of a specified path.
     *
     * @param nodePath path of the Node-object.
     * @return the found Node-object or null if object not found or nodePath invalid.
     */
    public Node getNodeByPath(String nodePath) {
        if (nodePath == null || nodePath.equals("")) {
            return null;
        }
        String[] path = nodePath.split("/");
        Node tmp = this;
        // iterate over every part of the new path
        for (String s : path) {
            // if the Node-object already exists, iterate over it
            if (tmp.getChildByName(s) != null) {
                tmp = tmp.getChildByName(s);
            } else {
                return null;
            }
        }
        return tmp;
    }

    /**
     * Creates a Node-object of a specified path. Non-existent Node-objects on the path will also be created.
     *
     * @param nodePath path of the Node-object.
     * @return the created Node-object or null if object not found or nodePath invalid.
     */
    public Node createNodeByPath(String nodePath) {
        if (nodePath == null || nodePath.equals("")) {
            return null;
        }
        String[] path = nodePath.split("/");
        Node tmp = this;
        // iterate over every part of the new path
        int i = 0;
        while (i < path.length) {
            String s = path[i];
            // if the Node-object already exists, iterate over it
            if (tmp.getChildByName(s) != null) {
                tmp = tmp.getChildByName(s);
            } else {
                // create new Node
//                Node node = new Node(new ArrayList<>(), tmp.getPath() + "/" + s, s, packageName);
                Node node = new Node(s, tmp.getPath().equals("") ? s : tmp.getPath() + "/" + s, "");
                tmp.addToChildren(node);
                tmp = node;
            }
            i++;
        }
        return tmp;
    }

    /**
     * Counts dependencies @this has on a given Node-object.
     *
     * @param node Node-object to check.
     * @return amount of dependencies @this has on a given Node-object.
     */
    public int countDependenciesOn(Node node) {
        int counter = 0;
        for (NodeDTO dependency : dependencies) {
            // if @node represents a file
            // else if @node represents a folder
            if (!node.hasChildren()) {
                // if the if @node is found, raise the counter
                if (dependency.getPath().equals(node.getPath())) {
                    counter++;
                }
            } else {
                // if @dependency is in the folder @node
                if (dependency.getPath().contains((node.getPackageName().equals("") ? node.getFilename() : node.getPackageName()).replaceAll("\\.", "/"))) {
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Check if this has a dependency on a given Node-object.
     *
     * @param node Node-object to check.
     * @return true if this has a dependency on the given Node-object.
     */
    public boolean hasDependencyOn(Node node) {
        for (NodeDTO dependency : dependencies) {
            if (!this.hasChildren() && !node.hasChildren() || this.hasChildren() && !node.hasChildren()) {
                if (dependency.getPath().equals(node.getPath())) {
                    return true;
                }
            } else if (!this.hasChildren() && node.hasChildren() || this.hasChildren() && node.hasChildren()) {
                if (dependency.getPath().contains((node.getPackageName().equals("") ? node.getFilename() : node.getPackageName()).replaceAll("\\.", "/"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get this Node-object's parent Node-object.
     *
     * @param root root Node-object to begin the search by path.
     * @return null if the root Node-object equals this Node-object else the parent Node-object.
     */
    public Node getParent(Node root) {
        if (this.path.equals(root.path)) {
            return null;
        } else if (!path.contains("/")) {
            return root;
        } else {
            String parentPath = this.path.substring(0, this.path.lastIndexOf("/"));
            return root.getNodeByPath(parentPath);
        }
    }

    public NodeDTO getDependencyByPath(String path) {
        return dependencies.stream().filter(dependency -> dependency.getPath().equals(path)).findFirst().orElse(null);
    }

    /**
     * Traverse the Tree in post order. Call method in TraverseInterface for every found Node-object.
     *
     * @param traverseInterface method to call.
     */
    public void traversePost(TraverseInterface traverseInterface) {
        Stack<Node> stack = new Stack<>();
        HashSet<Node> hash = new HashSet<>();
        Node root = this;
        stack.push(root);
        while (!stack.isEmpty()) {
            root = stack.peek();
            if (root.children.size() == 0 || hash.contains(root)) {
                traverseInterface.traverseMethod(stack.pop());
            } else {
                root.children.forEach(stack::push);
                hash.add(root);
            }
        }
    }

    /**
     * Traverse the Tree in pre order. Call method in TraverseInterface for every found Node-object.
     *
     * @param traverseInterface method to call.
     */
    public void traversePre(TraverseInterface traverseInterface) {
        Node root = this;
        Stack<Node> stack = new Stack<>();
        stack.add(root);

        while(!stack.isEmpty()) {
            root = stack.pop();
            stack.addAll(root.children);
            traverseInterface.traverseMethod(root);
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *                      Getter & Setter                    *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public List<NodeDTO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<NodeDTO> dependencies) {
        this.dependencies = dependencies;
    }
}
