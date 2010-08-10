import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class ScrambleSolver {

	private static final int GAME_SIZE = 4;
	private static final int BUFFER_SIZE = 2;
	private static final String exactWordQuery = "SELECT word from words where word=?";
	private static final String partialWordQuery = "SELECT word from words where word LIKE(?)";
	private String drivers, url, username, password;
	private Set<String> words;
	private Graph<Node> g;
	private Connection conn;
	private PreparedStatement exact, partial;
	
	public ScrambleSolver(String jumbleFile, String propFile) throws Exception{
		initConnection(propFile);
		g = buildGraph(jumbleFile);
		words = new HashSet<String>();
	}
		
	private void initConnection(String propFile) throws Exception{
		Properties props = new Properties();
		FileInputStream in = new FileInputStream(propFile);
		props.load(in);
		in.close();
		drivers = props.getProperty("jdbc.drivers");
		if (drivers != null) System.setProperty("jdbc.drivers", drivers);
		url = props.getProperty("jdbc.url");
		username = props.getProperty("jdbc.username");
		password = props.getProperty("jdbc.password");
	}
	
	private Graph<Node> buildGraph(String file) throws Exception{
		Node[][] board = readBoardTo2DArray(file);
		Graph<Node> g = new Graph<Node>();
		for(int i = 1; i < GAME_SIZE + 1; i++){
			for(int j = 1; j < GAME_SIZE + 1; j++){
				Node from = board[i][j];
				g.addEdge(from, board[i-1][j]);
				g.addEdge(from, board[i-1][j-1]);
				g.addEdge(from, board[i-1][j+1]);
				g.addEdge(from, board[i+1][j]);
				g.addEdge(from, board[i+1][j-1]);
				g.addEdge(from, board[i+1][j+1]);
				g.addEdge(from, board[i][j-1]);
				g.addEdge(from, board[i][j+1]);
			}
		}
		return g;
	}
	
	private Node[][] readBoardTo2DArray(String file) throws Exception{
		Node[][] board = new Node[GAME_SIZE + BUFFER_SIZE][GAME_SIZE + BUFFER_SIZE];
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int row = 1;
		while((line = br.readLine()) != null){
			String[] cline = line.split(" ");
			for(int i = 0; i < cline.length; i++){
				board[row][i+1] = new Node(cline[i]);
			}
			row++;
		}
		return board;
	}
	private void openConnection() throws SQLException{
		conn = DriverManager.getConnection(url, username, password);
		exact = conn.prepareStatement(exactWordQuery);
		partial = conn.prepareStatement(partialWordQuery);
	}
	
	private void closeConnection() throws SQLException{
		exact.close();
		partial.close();
		conn.close();
	}
	
	public Set<String> findWords(){
		try{
			openConnection();
			HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
			for(Node l : g.getVertices())
				visited.put(l, false);
		
			for(Node l : g.getVertices()){
				buildWords(g, l, visited, l.getValue());
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}finally{
			try{
				closeConnection();
			}catch(Exception e){}
		}
		return words;
	}
	
	private void buildWords(Graph<Node> g, Node n, HashMap<Node, Boolean> visited, String word){
		visited.put(n, true);
		for(Node v: g.getAdjacentVertices(n)){
			String currWord = word + v.getValue();
			if(!visited.get(v)){
				visitWord(currWord);
				if(wordsMatch(currWord)){
					buildWords(g, v, visited, currWord);
				}
			}
		}
		visited.put(n, false);
	}
	
	private void visitWord(String word){
		if(isWord(word) && !words.contains(word)){
			words.add(word);	
		}
	}
	
	private boolean isWord(String word){
		ResultSet rs = null;
		try{
			exact.setString(1, word);
			rs = exact.executeQuery("SELECT word from words where word=\""+word+"\"");
		if(rs.next()){
			return true;
		}
		} catch(Exception e){
			
		} finally {
			try{
				rs.close();
			}catch(Exception e){}
		}
		return false;
	}
	
	private boolean wordsMatch(String word){
		ResultSet rs = null;
		try{
			partial.setString(1, word+"%");
			rs = partial.executeQuery();
			if(rs.next()){
				return true;
			}
		} catch(Exception e){
			
		} finally {
			try{
				rs.close();
			}catch(Exception e){}
		}
		return false;
	}
	
	public static void main(String[] args){
		try{
			ScrambleSolver solver = new ScrambleSolver("./jumble.txt", "database.properties");
			List<String> words = new ArrayList<String>(solver.findWords());
			Collections.sort(words, new LengthComparator());
			for(String word : words){
				System.out.println(word);
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}
