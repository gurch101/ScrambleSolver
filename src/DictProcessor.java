import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;


public class DictProcessor {

	
	public static void main (String[] args){
		BufferedReader r = null;
		BufferedWriter w = null;
		try {
			r = new BufferedReader(new FileReader("./english.3"));
			w = new BufferedWriter(new FileWriter("./english-cor3.txt"));
			String word = null;
			String corrected = "";
			while((word = r.readLine()) != null){
				word = word.toLowerCase();
				corrected = isValid(word);
				if(!corrected.equals(""))
					w.write(corrected+"\n");
			}
			r.close();
			w.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static String isValid(String word){
		if(word.length() <= 2)
			return "";
		for(int i = 0; i < word.length(); i++){
			char c = word.charAt(i);
			if ((c < 'A' || c > 'Z')&&(c < 'a' || c >'z')){
				return "";
			}
		}
		return word;
	}
}
