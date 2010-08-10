import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Gurch
 * @param <T> The vertex type
 * 
 * A unweighted, undirected graph represented by an adjacency list
 */
public class Graph<T> {
	private HashMap<T, HashSet<T>> adj;
	
	public Graph(){
		adj = new HashMap<T, HashSet<T>>();
	}
	
	public void addEdge(T from, T to){
		if(from != null && to != null){
			if(!hasVertex(from)) addVertex(from);
			if(!hasVertex(to)) addVertex(to);
			adj.get(from).add(to);
			adj.get(to).add(from);
		}
	}
	
	public void addVertex(T c){
		adj.put(c, new HashSet<T>());
	}
	
	public boolean hasVertex(T c){
		return adj.get(c) != null;
	}
	
	public Set<T> getVertices(){
		return adj.keySet();
	}
	
	public Set<T> getAdjacentVertices(T c){
		return adj.get(c);
	}
	
	public String toString(){
		String out = "";
		String graph = "";
		for(T c : adj.keySet()){
			out += c+":";
			for(T a : adj.get(c)){
				out += a+" ";
			}
			graph += out+"\n";
			out = "";
		}
		return graph;
	}
}
