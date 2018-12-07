package notFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.Scanner;

public class Client {
	
	private static final String REP_FINAL = "RepositorioLocal/";
	private File connectedClients;

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

		insereOperacoes(in, out, sc, socket, user);

	}

	/**
	 * Cliente insere a operacao desejada
	 * @param in Canal para receber informacao
	 * @param out Canal para enviar informacao 
	 * @param sc Scanner de entrada de dados
	 * @param socket Canal de ligacao entre o Cliente e o Servidor
	 * @param user Utilizador (local) que estah logado
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void insereOperacoes(ObjectInputStream in, ObjectOutputStream out, Scanner sc, Socket socket, String user) throws IOException, ClassNotFoundException {

		boolean logado = true;
		
		//recebe lista de clientes conetados
		receiveConnectedClients(in);

		while (logado) { //cliente tem operacoes p/ fazer

			System.out.println("\n Operacoes disponiveis: ");
			//System.out.println("|-a <photos>|	       |-l <userId>|	                 |-i <userId> <photo>|");
			//System.out.println("|-g <userId>|	       |-c <comment> <userId> <photo>|   |-L <userId> <photo>|");
			//System.out.println("|-D <userId> <photo>|  |-f <followUserIds>|              |-r <followUserIds> |"); 
			System.out.println("|-quit| \n");
			System.out.print("Insira uma nova operacao: ");

			String op = sc.nextLine(); //operacao do cliente
			String [] comandos = op.split(" ");

			switch (comandos[0]) {

			case "-a":

				if (comandos.length != 2) {
					System.err.println("Nao inseriu os argumentos corretamente!");
					break;
				}

				out.writeObject(comandos[0]);
				//adicionaFotos(comandos[1], out, in);
				break;	

			case "-quit":
				out.writeObject(comandos[0]);
				System.out.println(user + " fez log out.");
				logado = false;
				break;

			default:
				System.err.println("O comando que inseriu nao estah definido.");
				break;
			}
		}

		in.close();
		out.close();
		sc.close();
		socket.close();
		
	}

	private void receiveConnectedClients(ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		String users = (String) in.readObject();

		BufferedWriter bw = new BufferedWriter(new FileWriter(connectedClients, true));
		bw.write(users);
		bw.flush();
		bw.close();
		
	}

	/**
	 * Cria o repositorio local do Utilizador que estah logado
	 * @param userLocal Utilizador logado
	 * @throws IOException 
	 */
	private void criaRepLocal(String userLocal) throws IOException {

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
		
		//cria ficheiro conClients.txt sempre que o cliente se liga
		connectedClients = new File(REP_FINAL + "conClients.txt");
		connectedClients.createNewFile();
	}

}
