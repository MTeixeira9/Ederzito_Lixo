package notFile;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.Scanner;

public class Client {
	
	private static final String REP_FINAL = "RepositorioLocal/";

	/**
	 * Main do Cliente
	 * @param args Argumentos passados na linha de comandos
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException {

		Client cliente = new Client();

		if (args.length < 2) 
			System.err.println("Nao inseiru os argumentos necessarios!");
		else 
			cliente.startClient(args);
	}

	/**
	 * Inicia uma instancia de cada cliente
	 * @param args Argumentos passados na linha de comandos
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 */
	@SuppressWarnings("resource")
	private void startClient(String[] args) throws IOException, ClassNotFoundException, ParseException {

		String user = "";
		String pass = "";
		String[] ipPorto = null;
		Socket socket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		Scanner sc = new Scanner(System.in);

		if (args.length == 3) { 

			//args[] = [0-user | 1-password | 2-serverAddress=ip:porto]
			user = args[0];
			pass = args[1];
			ipPorto = args[2].split(":");
			socket = new Socket(ipPorto[0], Integer.parseInt(ipPorto[1])); //ligacao ao servidor

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());			

			out.writeObject(user); //user
			out.writeObject(pass); //password

			while ( !in.readObject().equals("ls") ) { //Enquanto a pw estiver errada
				System.err.print("A Password que inseriu estah errada!");
				System.out.println();
				System.out.print("Insira a password novamente: ");
				String newPass = sc.nextLine();
				out.writeObject(newPass); //enviamos a pass correta p/ o servidor
			}
		}
		System.out.println("Login de " + user + " efetuado com sucesso.");

		criaRepLocal(user); //repositorio de cada cliente fora do servidor

		//insereOperacoes(in, out, sc, socket, user);

	}

	/**
	 * Cria o repositorio local do Utilizador que estah logado
	 * @param userLocal Utilizador logado
	 */
	private void criaRepLocal(String userLocal) {

		File repLocal = new File("RepositorioLocal"); //Cria a raiz do rep local
		File repUserLocal = new File(REP_FINAL + userLocal);

		/*
		 * verifica se o repositorio raiz existe
		 */
		if(repLocal.exists()) // verifica se ja existe
			System.err.println("Repositorio Local jah existe."); 
		else {
			System.out.println("Repositorio Local criado.");
			repLocal.mkdirs(); // Cria diretoria
		}

		/*
		 * Verifica se o repositorio filho existe
		 */
		if(repUserLocal.exists()) // verifica se ja existe
			System.err.println("Repositorio Local de " + userLocal + " jah existe."); 		
		else {
			System.out.println("Repositorio Local de " + userLocal + " criado.");
			repUserLocal.mkdirs(); // Cria diretoria
		}
	}

}
