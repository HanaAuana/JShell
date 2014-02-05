import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;


class Shell {


	public Shell() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean running = true;

		Scanner scanner = new Scanner(System.in);


		while (running){
			System.out.print(">");

			String input = scanner.nextLine();

			if(input.equals("exit")){
				System.out.println("Exiting");
				System.exit(0);
			}
			else if(input.equals("help")){
				System.out.println("Try typing a command");
			}
			else{

				//System.out.println("Input: "+input);
				Process p = Shell.runCommand(input);

				BufferedReader standardOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader standardError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String o = "";
				String err = "";
				String temp = null;
				try {
					while (( temp = standardOut.readLine()) != null) {
						o += temp;
						System.out.println(temp);
						temp = null;
					}
					//System.out.println("O: "+o);
					while ((temp =standardError.readLine()) != null) {
						err += temp;
					}
					if (err.contains("not recognized as an internal or external command")){
						System.out.println("Bad command: "+input);
					}
					else{
						if (err.length()>0){
							System.out.println("E: "+ err);
						}
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println(p.getInputStream());
			}
		}
	}

	public static Process runCommand(String input){
		Runtime runtime = Runtime.getRuntime();

		try {
			return runtime.exec(new String[]{"cmd.exe", "/C", input});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
