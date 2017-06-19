
public class MaxHeap {
	
	private Customer[] cust;
	private int size;
	
	public MaxHeap(int capacity){
		cust = new Customer[capacity + 1];
	}
	
	public boolean isEmpty(){
		return size == 0;
	}
	
	private void heapify(int k){
		while(k>1 && less(k/2,k)){
			swap(k, k/2);
			k = k/2;
		}
	}
	
	public Customer peek(){
		return  cust[1];
	}
	
	public void insert(String CustomerId, String lastName, double SimpleLTV){
		
		size++;
		cust[size] = new Customer(CustomerId, lastName, SimpleLTV);
		heapify(size);
		
	}
	
	private void demote(int k){
		while(2*k <= size){
			int j = 2*k;
			if(j < size && less(j,j+1)) j++;
			if(!less(k,j)) break;
			swap(k,j);
			k = j;
		}
	}
	
	public Customer delMax(){
		Customer item = cust[1];
		swap(1, size);
		size--;
		demote(1);
		cust[size + 1] = null;
		return item;
	}
	
	private boolean less(int i, int j){
		return (cust[i].getSimpleLTV() - cust[j].getSimpleLTV()) < 0.0;
	}
	
	private void swap(int i, int j){
		Customer temp = cust[i];
		cust[i] = cust[j];
		cust[j] = temp;
	}

}
