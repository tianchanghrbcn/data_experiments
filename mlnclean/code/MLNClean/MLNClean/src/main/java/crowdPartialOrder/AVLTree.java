package crowdPartialOrder;

import java.awt.print.Printable;
import java.util.ArrayList;

public class AVLTree<T extends Comparable> implements Tree<PartialNode> {

	/** The tree root. */
	public AVLNode<PartialNode> root;

	public boolean isEmpty() {
		return root == null;
	}

	public int size() {
		return size(root);
	}

	public int size(AVLNode<PartialNode> subtree) {
		if (subtree == null) {
			return 0;
		} else {
			return size(subtree.left) + 1 + size(subtree.right);
		}

	}

	public int height() {
		return height(root);
	}

	/**
	 * @param p
	 * @return
	 */
	public int height(AVLNode<PartialNode> p) {
		return p == null ? -1 : p.height;
	}

	public String preOrder() {
		String sb = preOrder(root);
		if (sb.length() > 0) {
			// 去掉尾部","号
			sb = sb.substring(0, sb.length() - 1);
		}
		return sb;
	}

	public ArrayList<PartialNode> findAll(AVLNode<PartialNode> p){
		ArrayList<PartialNode> result = new ArrayList<PartialNode>();
		if (p != null) {
			result.addAll(findAll(p.left));
			result.add(p.data);
			result.addAll(findAll(p.right));
		}
		return result;
	}
	
	/**
	 * 先根遍历
	 * 
	 * @param subtree
	 * @return
	 */
	public String preOrder(AVLNode<PartialNode> subtree) {
		StringBuilder sb = new StringBuilder();
		if (subtree != null) {
			// 先访问根结点
			sb.append(subtree.data).append(",");
			// 访问左子树
			sb.append(preOrder(subtree.left));
			// 访问右子树
			sb.append(preOrder(subtree.right));
		}
		return sb.toString();
	}

	public String inOrder() {
		String sb = inOrder(root);
		if (sb.length() > 0) {
			// 去掉尾部","号
			sb = sb.substring(0, sb.length() - 1);
		}
		return sb;
	}

	/**
	 * 中根遍历
	 * 
	 * @param subtree
	 * @return
	 */
	private String inOrder(AVLNode<PartialNode> subtree) {
		StringBuilder sb = new StringBuilder();
		if (subtree != null) {
			// 访问左子树
			sb.append(inOrder(subtree.left));
			// 访问根结点
			sb.append(subtree.data).append(",");
			// 访问右子树
			sb.append(inOrder(subtree.right));
		}
		return sb.toString();
	}

	public String postOrder() {
		String sb = postOrder(root);
		if (sb.length() > 0) {
			// 去掉尾部","号
			sb = sb.substring(0, sb.length() - 1);
		}
		return sb;
	}

	/**
	 * 后根遍历
	 * 
	 * @param subtree
	 * @return
	 */
	private String postOrder(AVLNode<PartialNode> subtree) {
		StringBuilder sb = new StringBuilder();
		if (subtree != null) {
			// 访问左子树
			sb.append(postOrder(subtree.left));
			// 访问右子树
			sb.append(postOrder(subtree.right));
			// 访问根结点
			sb.append(subtree.data).append(",");
		}
		return sb.toString();
	}

	public String levelOrder() {
		/**
		 * @see BinarySearchTree#levelOrder()
		 * @return
		 */
		return null;
	}

	/**
	 * 插入方法
	 * 
	 * @param data
	 */
	public void insert(PartialNode data) {
		if (data == null) {
			throw new RuntimeException("data can\'t not be null ");
		}
		this.root = insert(data, root);
	}

	public ArrayList<PartialNode> getAllNodes(AVLNode<PartialNode> t) {
		ArrayList<PartialNode> nodes = new ArrayList<PartialNode>();
		if (t.data.leaf == 1 || (t.left==null && t.right==null)) {
//			t.data.leaf = 0;
			nodes.add(t.data);
		}else{
			if(t.left!=null){
				nodes.addAll(getAllNodes(t.left));
			}
			if(t.right!=null){
				nodes.addAll(getAllNodes(t.right));
			}
		}
		return nodes;
	}
	
	public ArrayList<PartialNode> getSubNodes(AVLNode<PartialNode> t) {
		ArrayList<PartialNode> nodes = new ArrayList<PartialNode>();
		if (t.data.leaf == 1 || (t.left==null && t.right==null)) {
			nodes.add(t.data);
		}else{
			if(t.left!=null){
				nodes.addAll(getSubNodes(t.left));
			}
			if(t.right!=null){
				nodes.addAll(getSubNodes(t.right));
			}
		}
		return nodes;
	}
	
	
	public ArrayList<PartialNode> findQualifiedNode(Group data, AVLNode<PartialNode> p) {
		if(p==null){
			return null;
		}
		ArrayList<PartialNode> qualifiedList = new ArrayList<PartialNode>();
		int result = p.data.compare(data);
//		int result = data.compare(p.data);
		if(result == 2){//代表是同一个node
			// 该节点不是叶子节点，则check它的左节点
			ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.left);
			if (tmp_qualifiedList != null) {
				qualifiedList.addAll(tmp_qualifiedList);
			}
		}else if (result <= 0) {
			if (p.left == null && p.right == null) {
				// 该节点为叶子节点，是一个qualified node
				qualifiedList.add(p.data);
			} else { // 该节点不是叶子节点，则它的左节点是一个qualified node
				qualifiedList.add(p.left.data);
				ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.right);
				if (tmp_qualifiedList != null) {
					qualifiedList.addAll(tmp_qualifiedList);
				}
			}
		} else {
			if (p.left == null && p.right == null) {
				// 该节点为叶子节点，则prune it
				return null;
			} else {
				// 该节点不是叶子节点，则check它的左节点
				ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.left);
				if (tmp_qualifiedList != null) {
					qualifiedList.addAll(tmp_qualifiedList);
				}
			}
		}
		return qualifiedList;
	}
	
	public ArrayList<PartialNode> findQualifiedNode(PartialNode data, AVLNode<PartialNode> p) {
		if(p==null){
			return null;
		}
		ArrayList<PartialNode> qualifiedList = new ArrayList<PartialNode>();
		int result = p.data.compareTo(data);
		if(result == 2){//代表是同一个node
			// 该节点不是叶子节点，则check它的左节点
			ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.left);
			if (tmp_qualifiedList != null) {
				qualifiedList.addAll(tmp_qualifiedList);
			}
		}else if (result <= 0) {
			if (p.left == null && p.right == null) {
				// 该节点为叶子节点，是一个qualified node
				qualifiedList.add(p.data);
			} else { // 该节点不是叶子节点，则它的左节点是一个qualified node
				qualifiedList.add(p.left.data);
				ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.right);
				if (tmp_qualifiedList != null) {
					qualifiedList.addAll(tmp_qualifiedList);
				}
			}
		} else {
			if (p.left == null && p.right == null) {
				// 该节点为叶子节点，则prune it
				return null;
			} else {
				// 该节点不是叶子节点，则check它的左节点
				ArrayList<PartialNode> tmp_qualifiedList = findQualifiedNode(data, p.left);
				if (tmp_qualifiedList != null) {
					qualifiedList.addAll(tmp_qualifiedList);
				}
			}
		}
		return qualifiedList;
	}

	/**
	 * 找到data这个节点对应的所有qualified nodes
	 * @param data
	 */
	public ArrayList<PartialNode> find_qualified_nodes(PartialNode data) {
		ArrayList<PartialNode> qualifiedList = findQualifiedNode(data, root);
		return qualifiedList;
	}
	
	public ArrayList<PartialNode> find_qualified_nodes(Group data) {
		ArrayList<PartialNode> qualifiedList = findQualifiedNode(data, root);
		return qualifiedList;
	}

	public ArrayList<PartialNode> getUnderNodes(PartialNode data){
		AVLNode<PartialNode> node = findNode(data,root);
		
		ArrayList<PartialNode> results = getSubNodes(node);
		
		return results;
	}
	
	private static void merge(PartialNode data, AVLNode<PartialNode> p){
		ArrayList<Group> groups = data.groups;
		for(Group g: groups){
			p.data.groups.add(g);
		}
	}
	
	
	private AVLNode<PartialNode> insert(PartialNode data, AVLNode<PartialNode> p) {

		// 说明已没有孩子结点,可以创建新结点插入了.
		if (p == null) {
			p = new AVLNode<PartialNode>(data);
		}

		int result = data.compareTo(p.data);
		if(result == 2){
			;
		}else if (result == -1) {// 向左子树寻找插入位置
			p.left = insert(data, p.left);

			// 插入后计算子树的高度,等于2则需要重新恢复平衡,由于是左边插入,左子树的高度肯定大于等于右子树的高度
			if (height(p.left) - height(p.right) == 2) {
				// 判断data是插入点的左孩子还是右孩子
				if (data.compareTo(p.left.data) < 0) {
					// 进行LL旋转
					p = singleRotateLeft(p);
				} else {
					// 进行左右旋转
					p = doubleRotateWithLeft(p);
				}
			}
		} else if (result == 1) {// 向右子树寻找插入位置
			p.right = insert(data, p.right);

			if (height(p.right) - height(p.left) == 2) {
				if (data.compareTo(p.right.data) < 0) {
					// 进行右左旋转
					p = doubleRotateWithRight(p);
				} else {
					p = singleRotateRight(p);
				}
			}
		} else{ //result == 0,表明是两个不同的data，但是在该属性值上具有相同的相似度
			merge(data, p);
		}
			;// if exist do nothing
				// 重新计算各个结点的高度
		p.height = Math.max(height(p.left), height(p.right)) + 1;

		return p;// 返回根结点
	}

	/**
	 * 删除方法
	 * 
	 * @param data
	 */
	public void remove(PartialNode data) {
		if (data == null) {
			throw new RuntimeException("data can\'t not be null ");
		}
		this.root = remove(data, root);
	}

	/**
	 * 删除操作
	 * 
	 * @param data
	 * @param p
	 * @return
	 */
	private AVLNode<PartialNode> remove(PartialNode data, AVLNode<PartialNode> p) {

		if (p == null)
			return null;

		int result = data.compareTo(p.data);

		// 从左子树查找需要删除的元素
		if (result < 0) {
			p.left = remove(data, p.left);

			// 检测是否平衡
			if (height(p.right) - height(p.left) == 2) {
				AVLNode<PartialNode> currentNode = p.right;
				// 判断需要那种旋转
				if (height(currentNode.left) > height(currentNode.right)) {
					// RL
					p = doubleRotateWithRight(p);
				} else {
					// RR
					p = singleRotateRight(p);
				}
			}

		}
		// 从右子树查找需要删除的元素
		else if (result > 0) {
			p.right = remove(data, p.right);
			// 检测是否平衡
			if (height(p.left) - height(p.right) == 2) {
				AVLNode<PartialNode> currentNode = p.left;
				// 判断需要那种旋转
				if (height(currentNode.right) > height(currentNode.left)) {
					// LR
					p = doubleRotateWithLeft(p);
				} else {
					// LL
					p = singleRotateLeft(p);
				}
			}
		}
		// 已找到需要删除的元素,并且要删除的结点拥有两个子节点
		else if (p.right != null && p.left != null) {

			// 寻找替换结点
			p.data = findMin(p.right).data;

			// 移除用于替换的结点
			p.right = remove(p.data, p.right);
		} else {
			// 只有一个孩子结点或者只是叶子结点的情况
			p = (p.left != null) ? p.left : p.right;
		}

		// 更新高度值
		if (p != null)
			p.height = Math.max(height(p.left), height(p.right)) + 1;
		return p;
	}

	/**
	 * 查找最小值结点
	 * 
	 * @param p
	 * @return
	 */
	private AVLNode<PartialNode> findMin(AVLNode<PartialNode> p) {
		if (p == null)// 结束条件
			return null;
		else if (p.left == null)// 如果没有左结点,那么t就是最小的
			return p;
		return findMin(p.left);
	}

	public PartialNode findMin() {
		return findMin(root).data;
	}

	public PartialNode findMax() {
		return findMax(root).data;
	}

	/**
	 * 查找最大值结点
	 * 
	 * @param p
	 * @return
	 */
	private AVLNode<PartialNode> findMax(AVLNode<PartialNode> p) {
		if (p == null)
			return null;
		else if (p.right == null)// 如果没有右结点,那么t就是最大的
			return p;
		return findMax(p.right);
	}

	public AVLNode<PartialNode> findNode(PartialNode data, AVLNode<PartialNode> p) {
		if (p == null || data == null) {
			return null;
		}
		// 计算比较结果
		int compareResult = data.compareTo(p.data);

		if (compareResult == -1) {// 从左子树查找
			return findNode(data, p.left);
		} else if (compareResult == 1) {// 从右子树查找
			return findNode(data, p.right);
		} else{// match
			return p;
		}
	}

	public boolean contains(PartialNode data) {
		return data != null && contain(data, root);
	}

	public boolean contain(PartialNode data, AVLNode<PartialNode> subtree) {

		if (subtree == null)
			return false;

		int result = data.compareTo(subtree.data);

		if (result < 0) {
			return contain(data, subtree.left);
		} else if (result > 0) {
			return contain(data, subtree.right);
		} else {
			return true;
		}
	}

	public void clear() {
		this.root = null;
	}

	/**
	 * 左左单旋转(LL旋转) w变为x的根结点, x变为w的右子树
	 * 
	 * @param x
	 * @return
	 */
	private AVLNode<PartialNode> singleRotateLeft(AVLNode<PartialNode> x) {
		// 把w结点旋转为根结点
		AVLNode<PartialNode> w = x.left;
		// 同时w的右子树变为x的左子树
		x.left = w.right;
		// x变为w的右子树
		w.right = x;
		// 重新计算x/w的高度
		x.height = Math.max(height(x.left), height(x.right)) + 1;
		w.height = Math.max(height(w.left), x.height) + 1;
		return w;// 返回新的根结点
	}

	/**
	 * 右右单旋转(RR旋转) x变为w的根结点, w变为x的左子树
	 * 
	 * @return
	 */
	private AVLNode<PartialNode> singleRotateRight(AVLNode<PartialNode> w) {

		AVLNode<PartialNode> x = w.right;

		w.right = x.left;
		x.left = w;

		// 重新计算x/w的高度
		x.height = Math.max(height(x.left), w.height) + 1;
		w.height = Math.max(height(w.left), height(w.right)) + 1;

		// 返回新的根结点
		return x;
	}

	/**
	 * 左右旋转(LR旋转) x(根) w y 结点 把y变成根结点
	 * 
	 * @return
	 */
	private AVLNode<PartialNode> doubleRotateWithLeft(AVLNode<PartialNode> x) {
		// w先进行RR旋转
		x.left = singleRotateRight(x.left);
		// 再进行x的LL旋转
		return singleRotateLeft(x);
	}

	/**
	 * 右左旋转(RL旋转)
	 * 
	 * @param w
	 * @return
	 */
	private AVLNode<PartialNode> doubleRotateWithRight(AVLNode<PartialNode> w) {
		// 先进行LL旋转
		w.right = singleRotateLeft(w.right);
		// 再进行RR旋转
		return singleRotateRight(w);
	}

	
	private void printTree(AVLNode<PartialNode> t) {
		if (t != null) {
			printTree(t.left);
			System.out.print(t.data + ",");
			printTree(t.right);
		}
	}

	/**
	 * 测试
	 * @param arg
	 */
	public static void main(String arg[]) {

		
	}

	public BinaryNode findNode(PartialNode data) {
		// TODO Auto-generated method stub
		return null;
	}

}
