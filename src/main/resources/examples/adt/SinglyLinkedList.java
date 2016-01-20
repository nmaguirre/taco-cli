
public class SinglyLinkedList {

	public /*@nullable@*/ SinglyLinkedListNode header;

	public SinglyLinkedList() {
	    
	}

	/*@
      @ invariant (\forall SinglyLinkedListNode n; \reach(this.header, SinglyLinkedListNode, next).has(n); \reach(n.next, SinglyLinkedListNode, next).has(n)==false);
      @*/

    /*@
      @ ensures (\exists SinglyLinkedListNode n; \reach(this.header, SinglyLinkedListNode, next).has(n); n.value==valueParam) <==> (\result==true);
      @ signals (RuntimeException e) false;
      @ 
      @*/
	public boolean contains( /*@nullable@*/ Object valueParam, Object pepe1, Object pepe2,  int pepe3 )
	{
		SinglyLinkedListNode current;
		boolean result;
		current = this.header.next; //mutGenLimit 1
		result = false;
		while (result == false && current != null) {
			boolean equalVal;
			if (valueParam == null && current.value == null) {
				equalVal = false; //mutGenLimit 1
			} else {
				if (valueParam == null) {
					if (valueParam == current.value) {
						equalVal = true;
					} else {
						equalVal = false;
					}
				} else {
					equalVal = false;
				} //mutGenLimit 1
			}
			if (equalVal == true) {
				result = true;
			}
			current = current.next.next; //mutGenLimit 1
		}
		return result;
	}

	/*@
      @ requires index>=0 && index<\reach(this.header, SinglyLinkedListNode, next).int_size();
      @
      @ ensures \reach(this.header, SinglyLinkedListNode, next).has(\result)==true; 
      @ ensures \reach(\result, SinglyLinkedListNode, next).int_size() == \reach(this.header, SinglyLinkedListNode, next).int_size()-index;
      @ signals (RuntimeException e) false;
      @*/
	public SinglyLinkedListNode getNode( int index )
	{
		SinglyLinkedListNode current = header; 
		SinglyLinkedListNode result = current; //mutGenLimit 1
		int current_index = 0;
		while (result == null && current != null) {
			if (index == current_index) {
				result = current;  
			}
			current_index = current_index + 1; 
			current = current.next; 
		}
		return result;
	}

	public void insertBack( java.lang.Object arg )
	{
		SinglyLinkedListNode freshNode = new SinglyLinkedListNode();
		freshNode.value = arg;
		freshNode.next = null;
		if (this.header == null) {
			this.header = freshNode;
		} else {
			SinglyLinkedListNode current;
			current = this.header;
			while (current.next != null) {
				current = current.next;
			}
			current.next = freshNode;
		}
	}
}
