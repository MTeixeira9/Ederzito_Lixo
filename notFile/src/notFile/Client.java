package notFile;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

	public static final String REP_FINAL = "RepositorioLocal/";
	public static File connectedClients;
	public static List<Socket> conexoes;

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
		conexoes = new ArrayList<>();
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
		receiveConnectedClients(in, out);

		while (logado) { //cliente tem operacoes p/ fazer

			System.out.println("\n Operacoes disponiveis: ");
			System.out.println("|-c <userIP> <userPort>|	       |-p <file>|	       |-s <query>|");
			System.out.println("|-quit| \n");
			System.out.print("Insira uma nova operacao: ");

			String op = sc.nextLine(); //operacao do cliente
			String [] comandos = op.split(" ");

			switch (comandos[0]) {

			case "-c":

				if (comandos.length != 3) {
					System.err.println("Nao inseriu os argumentos corretamente!");
					break;
				}

				out.writeObject(comandos[0]);
				connectTo(comandos[1], comandos[2], in, out, user);
				break;	

			case "-p":

				if (comandos.length != 2) {
					System.err.println("Nao inseriu os argumentos corretamente!");
					break;
				}

				out.writeObject(comandos[0]);
				uploadFile(comandos[1], in, out, user);
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

	private void uploadFile(String f, ObjectInputStream in, ObjectOutputStream out, String user) throws ClassNotFoundException, IOException {

		File file = new File(f);

		if (!file.exists()) { //se a foto nao existe
			out.writeObject("nExiste");
			System.err.println("O ficheiro que inseriu nao existe!");
		}
		else {
			out.writeObject("existe");
			
			String nome = f.substring(f.lastIndexOf("/")); //obter nome ficheiro

			byte [] sizeFile  = new byte [(int)file.length()];

			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);

			bis.read(sizeFile,0,sizeFile.length);
			bis.close();
			//System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
			out.writeInt(sizeFile.length); //envia tamanho do ficheiro
			out.writeObject(nome); //enviar nome ficheiro
			out.write(sizeFile,0,sizeFile.length); //envia ficheiro byte a byte
			out.flush();

			//Recebe se correu bem ou nao
			String feed = (String) in.readObject();
			
			if(feed.equals("err")) {
				System.err.println("Ficheiro de " + user + " jah existe!");
			}else {
				System.out.println("Ficheiro de " + user + " fez upload com sucesso");
			}
		}
	}

	private void receiveConnectedClients(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {

		String users = (String) in.readObject();		
		writeUsers(users);

	}

	private void writeUsers(String usersIP) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(connectedClients, true));
		bw.write(usersIP);
		bw.flush();
		bw.close();
	}

	private void connectTo(String userIP, String userPort, ObjectInputStream in, ObjectOutputStream out, String user) throws IOException, ClassNotFoundException {

		Socket socket = new Socket(userIP, Integer.parseInt(userPort)); //ligacao ao servidor
		conexoes.add(socket);

		writeUsers(userIP);

		out.writeObject(userIP);
		String result = (String) in.readObject();

		if (result.equals("ok"))
			System.out.println("Servidor de " + user + " adicionou a nova conexao");
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
