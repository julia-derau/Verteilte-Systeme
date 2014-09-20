import java.util.Scanner;


public class Translator {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int counter = 0;
		
		if(sc.hasNextInt()) {
			counter = sc.nextInt();
		}
		// 1.A ist im Relationenmodell... (c) a.(a1, a2) b.	(a1, a2, a3) c.	(a1, a2, a3) d.	(a1, a2, a3) e.	(a1, a2, a3, a4)

		while(sc.hasNextLine()) {
			
			String s = sc.nextLine();
			String sentence = s.substring(1, s.indexOf("a."));
			String correctAnswer = s.substring(sentence.lastIndexOf("(")+2, sentence.length()-1);
			sentence = sentence.substring(1, sentence.lastIndexOf("("));

			String answer1 = s.substring(s.indexOf("a.")+2, s.indexOf("b.", s.indexOf("a.")));
			String answer2 = s.substring(s.indexOf("b.", s.indexOf("a."))+2, s.indexOf("c.", s.indexOf("b.")));
			String answer3 = s.substring(s.indexOf("c.", s.indexOf("b."))+2, s.indexOf("d.", s.indexOf("c.")));
			String answer4 = s.substring(s.indexOf("d.", s.indexOf("c."))+2, s.indexOf("e.", s.indexOf("d.")));
			String answer5 = s.substring(s.indexOf("e.", s.indexOf("d."))+2, s.length());

			String output = "INSERT INTO PRUEFUNG VALUES(" + counter + ",'" + counter + ") " + sentence + "','1. "+ answer1 + "','2. " + answer2 + "','3. " + answer3 + "','4. " + answer4 + "','5. " + answer5 +"',"+ correctAnswer + ")";
			System.out.println(output);
			counter++;

		}
	}
}
