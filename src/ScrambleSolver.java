import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;


public class ScrambleSolver {

	private static final int GAME_SIZE = 4;
	private static final String exactWordQuery = "SELECT word from words where word=?";
	private static final String partialWordQuery = "SELECT word from words where word LIKE(?)";
	private static String drivers, url, username, password;
	private static ArrayList<String> words = new ArrayList<String>();
	private static Connection conn;
	private static PreparedStatement exact, partial;
	
	public static void main(String[] args){
		try{
			initConnection();
			Graph<Node> g = buildGraph("./jumble.txt");
			findWords(g);
			Collections.sort(words, new LengthComparator());
			for(String word : words){
				System.out.println(word);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				exact.close();
				partial.close();
				conn.close();
			}catch(Exception e){}
		}
	}
	
	public static void initConnection() throws Exception{
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("database.properties");
		props.load(in);
		in.close();
		drivers = props.getProperty("jdbc.drivers");
		if (drivers != null) System.setProperty("jdbc.drivers", drivers);
		url = props.getProperty("jdbc.url");
		username = props.getProperty("jdbc.username");
		password = props.getProperty("jdbc.password");
		conn = DriverManager.getConnection(url, username, password);
		exact = conn.prepareStatement(exactWordQuery);
		partial = conn.prepareStatement(partialWordQuery);

	}
	
	public static Graph<Node> buildGraph(String file) throws Exception{
		Node[][] board = readBoardTo2DArray(file);
		Graph<Node> g = new Graph<Node>();
		for(int i = 0; i < GAME_SIZE; i++){
			for(int j = 0; j < GAME_SIZE; j++){
				Node from = board[i][j];
				if(i - 1 >= 0){
					g.addEdge(from, board[i-1][j]);
					if(j - 1 >= 0){
						g.addEdge(from, board[i-1][j-1]);
					}
					if(j + 1 < GAME_SIZE){
						g.addEdge(from, board[i-1][j+1]);
					}
				}
				if(i + 1 < GAME_SIZE){
					g.addEdge(from, board[i+1][j]);
					if(j - 1 >= 0){
						g.addEdge(from, board[i+1][j-1]);
					}
					if(j + 1 < GAME_SIZE){
						g.addEdge(from, board[i+1][j+1]);
					}
				}
				if(j - 1 >= 0){
					g.addEdge(from, board[i][j-1]);
				}
				if(j + 1 < GAME_SIZE){
					g.addEdge(from, board[i][j+1]);
				}
			}
		}
		return g;
	}
	
	public static Node[][] readBoardTo2DArray(String file) throws Exception{
		Node[][] board = new Node[GAME_SIZE][GAME_SIZE];
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		int row = 0;
		while((line = br.readLine()) != null){
			String[] cline = line.split(" ");
			for(int i = 0; i < cline.length; i++){
				board[row][i] = new Node(cline[i]);
			}
			row++;
		}
		return board;
	}
	
	public static void findWords(Graph<Node> g){
		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
		for(Node l : g.getVertices())
			visited.put(l, false);
		
		for(Node l : g.getVertices()){
			buildWords(g, l, visited, l.getValue());
		}
	}
	
	public static void buildWords(Graph<Node> g, Node n, HashMap<Node, Boolean> visited, String word){
		visited.put(n, true);
		for(Node v: g.getAdjacentVertices(n)){
			String currWord = word + v.getValue();
			if(!visited.get(v)){
				if(isWord(currWord)){
					if(!words.contains(currWord)){
						words.add(currWord);
					}
						
				}
				if(wordsMatch(currWord)){
					buildWords(g, v, visited, currWord);
				}
			}
		}
		visited.put(n, false);
	}
	
	public static boolean isWord(String word){
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
	
	public static boolean wordsMatch(String word){
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
}
