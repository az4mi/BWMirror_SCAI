package MODaStar;

import java.util.Stack;

/**
 * Class Tree represents modified binary tree data structure. Modification is based on permission of element having a
 * sibiling (element with the same value of key).
 */
class Tree
{
    private Block root;             // first node of tree

    // -------------------------------------------------------------
    public Tree()                  // constructor
    { root = null; }            // no nodes in tree yet
    // -------------------------------------------------------------
    public Block find(double pFValue)      // find node with given key
    {                           // (assumes non-empty tree)
        Block current = root;               // start at root
        while(current.getFValue() != pFValue)        // while no match,
        {
            if(pFValue < current.getFValue())         // go left?
                current = current.getLeftChild();
            else                            // or go right?
                current = current.getRightChild();
            if(current == null)             // if no child,
                return null;                 // didn't find it
        }
        return current;                    // found it
    }  // end find()
    // -------------------------------------------------------------
    public Block getSmallestBlock() {
        Block current=null;
        if(root!=null) {
            current=root;
            while(current.hasLeftChild()) {
                current=current.getLeftChild();
            }
        }
        return current;
    }

    public void insert(Block pBlock)
    {
        Block newNode = pBlock;//new Block(pBlock.getRow(),pBlock.getColumn());    // make new node
        //newNode.setfValue(pBlock.getfValue());         // insert data
        if(root==null) {               // no node in root
            root = newNode;
        }
        else                          // root occupied
        {
            Block current = root;       // start at root
            Block parent;
            while(true)                // (exits internally)
            {
                parent = current;
                if(newNode.getFValue() < current.getFValue())  // go left? ToDo: sem dat <= a znova testy :-(
                {
                    current = current.getLeftChild();
                    if(current == null)  // if end of the line,
                    {                 // insert on left
                        parent.setLeftChild(newNode);
                        return;
                    }
                }  // end if go left
                else if(newNode.getFValue() > current.getFValue())    {                                                        // or go right?
                    current = current.getRightChild();
                    if(current == null)  // if end of the line
                    {                 // insert on right
                        parent.setRightChild(newNode);
                        return;
                    }
                } else {                // same value
                    current=current.getSibiling();
                    if(current==null) {
                        parent.setSibiling(newNode);
                        return;
                    }
                }
            }  // end while
        }  // end else not root
    }  // end insert()
    // -------------------------------------------------------------
    public boolean delete(Block pBlock) // delete node with given key
    {                           // (assumes non-empty list)
        Block current = root;
        Block parent = root;
        boolean isLeftChild = true;
        boolean isSibiling = false;

        while((current.getFValue()!=pBlock.getFValue())||((current.getRow()!=pBlock.getRow()||current.getColumn()!=pBlock.getColumn())))        // search for node
        {
            parent = current;
            if(pBlock.getFValue() < current.getFValue())         // go left?
            {
                isLeftChild = true;
                current = current.getLeftChild();
            }
            else if(pBlock.getFValue() > current.getFValue())                           // or go right?
            {
                isLeftChild = false;
                current = current.getRightChild();
            } else {
                isSibiling = true;
                current = current.getSibiling();
            }
            if(current == null)             // end of the line,
                return false;                // didn't find it

        }  // end while
        // found node to delete

        //is sibiling, surely does not have left or right child
        if(isSibiling) {
            if(current.getSibiling()==null) {
                parent.setSibiling(null);
            } else {
                parent.setSibiling(current.getSibiling());
            }
        } else if(current.getLeftChild()==null && current.getRightChild()==null) {
            // if no children, simply delete it
            if(current == root)             // if root,
                root = root.getSibiling();                 // tree is empty
            else if(isLeftChild)
                parent.setLeftChild(current.getSibiling());     // disconnect
            else                            // from parent
                parent.setRightChild(current.getSibiling());
        }

        // if no right child, replace with left subtree
        else if(current.getRightChild()==null)
            if(current == root) {
                if(root.getSibiling()!=null) {
                    root.getSibiling().setLeftChild(root.getLeftChild());
                    root=root.getSibiling();
                } else {
                    root = current.getLeftChild();
                }
            }
            else if(isLeftChild) {
                if(current.getSibiling()!=null) {
                    current.getSibiling().setLeftChild(current.getLeftChild());
                    parent.setLeftChild(current.getSibiling());
                } else {
                    parent.setLeftChild(current.getLeftChild());
                }
            } else {
                if(current.getSibiling()!=null) {
                    current.getSibiling().setLeftChild(current.getLeftChild());
                    parent.setRightChild(current.getSibiling());
                } else {
                    parent.setRightChild(current.getLeftChild());
                }
            }
            // if no left child, replace with right subtree
        else if(current.getLeftChild()==null)
            if(current == root) {
                if(root.getSibiling()!=null) {
                    root.getSibiling().setRightChild(root.getRightChild());
                    root=root.getSibiling();
                } else {
                    root = current.getRightChild();
                }
            }
            else if(isLeftChild) {
                if(current.getSibiling()!=null) {
                    current.getSibiling().setRightChild(current.getRightChild());
                    parent.setLeftChild(current.getSibiling());
                } else {
                    parent.setLeftChild(current.getRightChild());
                }
            }
            else {
                if(current.getSibiling()!=null) {
                    current.getSibiling().setRightChild(current.getRightChild());
                    parent.setRightChild(current.getSibiling());
                } else {
                    parent.setRightChild(current.getRightChild());
                }
            }

        else  // two children, so replace with inorder successor
        {
            if(current.getSibiling()!=null) {

                current.getSibiling().setLeftChild(current.getLeftChild());
                current.getSibiling().setRightChild(current.getRightChild());
                if(current==root) {
                    root=current.getSibiling();
                } else {
                    if(isLeftChild) {
                        parent.setLeftChild(current.getSibiling());
                    } else {
                        parent.setRightChild(current.getSibiling());
                    }
                }
            } else {
                // get successor of node to delete (current)
                Block successor = getSuccessor(current);

                // connect parent of current to successor instead
                if(current == root)
                    root = successor;
                else if(isLeftChild)
                    parent.setLeftChild(successor);
                else
                    parent.setRightChild(successor);

                // connect successor to current's left child
                successor.setLeftChild(current.getLeftChild());
                successor.setRightChild(current.getRightChild());
            }
        }  // end else two children
        // (successor cannot have a left child)
        return true;                                // success
    }  // end delete()
    // -------------------------------------------------------------
    // returns node with next-highest value after delNode
    // goes to right child, then right child's left descendents
    private Block getSuccessor(Block delNode)
    {
        Block successorParent = delNode;
        Block successor = delNode;
        Block current = delNode.getRightChild();   // go to right child
        while(current != null)               // until no more
        {                                 // left children,
            successorParent = successor;
            successor = current;
            current = current.getLeftChild();      // go to left child
        }
        // if successor not
        if(successor != delNode.getRightChild())  // right child,
        {                                 // make connections
            successorParent.setLeftChild(successor.getRightChild());
            successor.setRightChild(delNode.getRightChild());
        }
        return successor;
    }
    // -------------------------------------------------------------
    public void traverse(int traverseType)
    {
        switch(traverseType)
        {
            case 1: System.out.print("\nPreorder traversal: ");
                preOrder(root);
                break;
            case 2: System.out.print("\nInorder traversal:  ");
                inOrder(root);
                break;
            case 3: System.out.print("\nPostorder traversal: ");
                postOrder(root);
                break;
        }
        System.out.println();
    }
    // -------------------------------------------------------------
    private void preOrder(Block localRoot)
    {
        if(localRoot != null)
        {
            System.out.print(localRoot.getFValue() + " ");
            preOrder(localRoot.getLeftChild());
            preOrder(localRoot.getRightChild());
        }
    }
    // -------------------------------------------------------------
    private void inOrder(Block localRoot)
    {
        if(localRoot != null)
        {
            inOrder(localRoot.getLeftChild());
            System.out.print(localRoot.getFValue() + " ");
            inOrder(localRoot.getRightChild());
        }
    }
    // -------------------------------------------------------------
    private void postOrder(Block localRoot)
    {
        if(localRoot != null)
        {
            postOrder(localRoot.getLeftChild());
            postOrder(localRoot.getRightChild());
            System.out.print(localRoot.getFValue() + " ");
        }
    }
    // -------------------------------------------------------------
    public void displayTree()
    {
        Stack globalStack = new Stack();
        globalStack.push(root);
        int nBlanks = 32;
        boolean isRowEmpty = false;
        System.out.println(
                "......................................................");
        while(isRowEmpty==false)
        {
            Stack localStack = new Stack();
            isRowEmpty = true;

            for(int j=0; j<nBlanks; j++)
                System.out.print(' ');

            while(globalStack.isEmpty()==false)
            {
                Block temp = (Block)globalStack.pop();
                if(temp != null)
                {
                    System.out.print(temp.getFValue());
                    System.out.print(" [");
                    Block sib=temp;
                    while(sib.getSibiling()!=null) {
                        System.out.print(temp.getSibiling().getFValue()+",");
                        sib=sib.getSibiling();
                    }
                    System.out.print("]");
                    localStack.push(temp.getLeftChild());
                    localStack.push(temp.getRightChild());

                    if(temp.getLeftChild() != null ||
                            temp.getRightChild() != null)
                        isRowEmpty = false;
                }
                else
                {
                    System.out.print("--");
                    localStack.push(null);
                    localStack.push(null);
                }
                for(int j=0; j<nBlanks*2-2; j++)
                    System.out.print(' ');
            }  // end while globalStack not empty
            System.out.println();
            nBlanks /= 2;
            while(localStack.isEmpty()==false)
                globalStack.push( localStack.pop() );
        }  // end while isRowEmpty is false
        System.out.println(
                "......................................................");
    }  // end displayTree()

    public static void main(String[] args) {
        Tree tr=new Tree();

        Block r1=new Block(null,5,2,3,null);
        r1.setFValue(20);
        Block r2=new Block(null,5,1,3,null);
        r2.setFValue(8);
        Block r3=new Block(null,5,8,3,null);
        r3.setFValue(22);
        Block r4=new Block(null,5,4,3,null);
        r4.setFValue(4);
        Block r5=new Block(null,5,5,8,null);
        r5.setFValue(12);
        Block r6=new Block(null,5,9,8,null);
        r6.setFValue(10);
        Block r7=new Block(null,5,3,8,null);
        r7.setFValue(14);
        Block r8=new Block(null,5,1,4,null);
        r8.setFValue(4);
        Block r9=new Block(null,5,1,5,null);
        r9.setFValue(4);
//        Block r10=new Block(null,5,7,8,null);
//        r10.setFValue(1);
//        Block r11=new Block(null,5,7,8,null);
//        r11.setFValue(2);
//        Block r12=new Block(null,5,7,8,null);
//        r12.setFValue(1);
//        Block r13=new Block(null,5,7,8,null);
//        r13.setFValue(2);

        tr.insert(r1);
        tr.insert(r2);
        tr.insert(r3);
        tr.insert(r4);
        tr.insert(r5);
        tr.insert(r6);
        tr.insert(r7);
        tr.insert(r8);
        tr.insert(r9);
//        tr.insert(r10);
//        tr.insert(r11);
//        tr.insert(r12);
//        tr.insert(r13);


        tr.displayTree();
//        System.out.println("Block row = " + tr.getSmallestBlock().getRow());
//
//
//        System.out.println("Value = " +  tr.find(2).getFValue());
//        tr.delete(r1);
//        tr.displayTree();
//        System.out.println("Block row = " + tr.getSmallestBlock().getRow());
        //System.out.println("Successor of " + r2.getFValue() + " is " + tr.getSuccessor(r2).getFValue());
        tr.displayTree();
        System.out.println("Smallest block = " + tr.getSmallestBlock().getFValue()+" ["+tr.getSmallestBlock().getRow()+";"+tr.getSmallestBlock().getColumn()+"]");
        tr.delete(tr.getSmallestBlock());
        tr.displayTree();
        System.out.println("Smallest block = " + tr.getSmallestBlock().getFValue() + " [" + tr.getSmallestBlock().getRow() + ";" + tr.getSmallestBlock().getColumn() + "]");
    }
// -------------------------------------------------------------
}  // end class Tree
