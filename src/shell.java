//Michael Lim HW1

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;


class ShellThread extends Thread{ //Used this for help with Threads http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html
	
	Scanner scanner = new Scanner(System.in); //Used to read in commands from the user
	String input;
	
	ShellThread(String i){ //Take a command as a parameter
		this.input = i;
	}
	
	public void run(){ //On run, simply execute a command as usual, but now wrapped in a thread
		Shell.runCommand(scanner, input); //Use the shell methods to do the actual running
	}
}

class Shell {

	public static void main(String[] args) { //Sets up a scanner, and begins to take input
		boolean running = true;

		Scanner scanner = new Scanner(System.in); //Used to read in commands from the user
		String input = "";

		while (running){ //Keep running until user decides to exit
			System.out.print("JShell >"); //Print out the prompt

			input = scanner.nextLine(); //Read in a command from the user

			if(input.contains("&")){ //If the command has an &, we'll need to split it up and run several processes

				String[] multipleCommands = input.split("&"); //Split on the & character, and have one string for each process
				ShellThread[] threads = new ShellThread[multipleCommands.length]; //Set up an array for our threads
				for(int i = 0; i < multipleCommands.length; i++){ //Iterate through the separate commands
					String thisCmd = multipleCommands[i]; //Get the current command
					if(multipleCommands[i].length() >=1){ //Don't run any commands that are empty strings
						threads[i] = new ShellThread(thisCmd); //Create a new thread, and give it its command
						threads[i].start(); //Start the thread
					}
				}
				for(int i = 0; i < threads.length; i++){ //Once we've created all our threads
					try {
						threads[i].join(); //Wait for them all to finish
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			else{ //No &s, so we can just run the command normally
				Shell.runCommand(scanner, input);
			}
		}
		scanner.close();
	}

	public static void runCommand(Scanner scanner, String input){ //Encapsulates all the parts we need to run a command

		if(input.equals("exit")){ //If the user simply enters 'exit' we will exit
			System.out.println("Goodbye!");
			scanner.close();
			System.exit(0);
		}
		else if(input.equals("help")){//If the user simply enters 'help' we will simply print a dummy help message
			System.out.println("Try typing a command such as 'ls.' Type 'exit' to quit");
		}
		else{//Otherwise, we assume the user has entered a 'normal' command, so we'll run it using Runtime.exec()

			if(input.contains(">")){//If the command contains a '>' we're going to redirect its output to a file
				String leftInput = input.substring(0,input.indexOf(">"));//Cut off the string before the '>' we'll deal with this separately
				String rightInput = input.substring(input.indexOf(">")+1).trim();//Grab the part after the '>' this will be our file destination

				Process p = Shell.execute(leftInput); //Pass our string as input, and get a Process back

				if(p != null){ //If the process we were given is null, do nothing
					Shell.readStreamsToFile(p, leftInput, rightInput);//Read streams from the Process, and write to the file designated in our original command
				}
			}
			else{//Otherwise we'll just print to the console
				Process p = Shell.execute(input); //Pass our string as input, and get a Process back
				if(p != null){ //If the process we were given is null, do nothing
					Shell.readStreams(p,input); //Read the text streams from the Process
				}
			}
		}
	}

	public static Process execute(String input){// Given a string, run a command using Runtime.exec()
		input = input.trim();
		String[] cmds = input.split(" "); //Split input on spaces, and turn in it into an array (This allows flags and parameters to work with exec() )
		Runtime runtime = Runtime.getRuntime(); //Get the runtime

		try {
			return runtime.exec(cmds); //Run the command, and return the Process if successful
		} catch (IOException e) {
			System.out.println("Bad command: '"+input+"'");	//If we get an exception, inform the user that the command was invalid
		}
		return null; //Return null if command was unsuccessful
	}

	public static void readStreams(Process p, String input){ //Simply read from a Process input streams
		
		//Used this to help figure out how to read from an InputStream http://stackoverflow.com/questions/5200187/convert-inputstream-to-bufferedreader
		BufferedReader standardOut = new BufferedReader(new InputStreamReader(p.getInputStream())); //Set up readers for standard output and standard error
		BufferedReader standardError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		String err = "";
		String temp = null;
		try {
			temp = standardOut.readLine(); //Read output line by line
			while (temp!= null) { //While we have more output

				System.out.println(temp); //Print output one line at a time
				temp = standardOut.readLine(); //Get the next line
			}

			temp = standardError.readLine(); //Now start reading error
			while (temp!= null) { //While there is more output to print
				err += temp; //Concatenates the current line
				temp = standardError.readLine(); //Get next error line
			}
			if (err.length()>0){ //If there was error in this stream
				System.out.println("E: "+ err); //Print all the error at once
			}

		} catch (IOException e) {//If we got an error, inform the user that the command was invalid
			System.out.println("Bad command: '"+input+"'");	
		}
	}

	public static void readStreamsToFile(Process p, String input, String dest){ //Read from a Process input streams, but write standard output to a file
		
		//Used this to help figure out how to read from an InputStream http://stackoverflow.com/questions/5200187/convert-inputstream-to-bufferedreader
		BufferedReader standardOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader standardError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		String err = "";
		String temp = null;
		ArrayList<String> output = new ArrayList<String>(); //Setup an arraylist to store our output 

		try {
			temp = standardOut.readLine(); //Start reading output
			while (temp!= null) {//While we still have output
				output.add(temp+"\n");//Add it to our arraylist and add the new line back on
				temp = standardOut.readLine(); //Get the next line
			}
			temp = standardError.readLine(); //Now start reading error
			while (temp!= null) { //While there is more error to be read
				err += temp; //Concatenate the current line
				temp = standardError.readLine(); //Get the next line
			}
			if (err.length()>0){ //If there was any error
				System.out.println("E: "+ err); //Print it all out
			}

		} catch (IOException e) { //If we got an error, inform the user they gave a bad command
			System.out.println("Bad command: '"+input+"'");	
		}
		try { //Used this to help figure out how to write to a file http://stackoverflow.com/questions/2885173/java-how-to-create-and-write-to-a-file
			File file = new File(dest); //Set up a new file with the supplied file name
			BufferedWriter writer = new BufferedWriter(new FileWriter(file)); //Create a writer using our file

			for(int i = 0; i < output.size(); i ++){ //For each line of output we saved
				writer.write(output.get(i)); //Write that line to our file
			}

			writer.close(); //Close the file
		} 
		catch ( IOException e ) { //If we got an error, let the user know we couldn't write to the file
			System.out.println("Could not write to file");
			for(int i = 0; i < output.size(); i ++){ //Print the output to console instead
				System.out.println(output.get(i));
			}
		}
	}
}

