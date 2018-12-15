package notFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {

	private Socket socket;
	private static final String REP_FINAL = "notFileRepositorio/";
	private String ipUser;
	//private String portUser;
	private File users;
	private File connectedClients;
	private File rep;

	/**
	 * Construtor de ServerThread
	 * @param inSoc Socket onde o Cliente se conectou
	 */
	public ServerThread(Socket inSoc) {
		socket = inSoc;
		//obter IP do cliente que se ligou
		ipUser = inSoc.getInetAddress().getHostAddress();
		//portUser = "" + inSoc.getPort();
	}

	/**
	 * Run
	 */
	@SuppressWarnings("resource")
	public void run() {

		try {

			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

			criaRepositorio();

			String user = "";
			String pass = "";

			try {

				String s1 = (String)in.readObject();
				String s2 = (String)in.readObject();

				user = s1;
				pass = s2;

				if (s1.equals("-s")){
					subscribeSV(s2, out, in);
				}
				else {

					boolean verificou = verificaUserPass(user, pass);

					while (!verificou) { //enquanto password estiver errada
						System.err.println("O Utilizador " + user + " inseriu a password errada!");
						out.writeObject("pe"); //PE = password errada
						pass = (String)in.readObject(); //fica ah espera da pass correta
						verificou = verificaUserPass(user, pass); 
					}

					out.writeObject("ls"); //LS = login com sucesso
				}
				pedeOperacoes(in, out, user, pass);

			} catch(ClassNotFoundException e1) {
				e1.printStackTrace();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void pedeOperacoes(ObjectInputStream in, ObjectOutputStream out, String user, String pass) throws ClassNotFoundException, IOException {

		boolean logado = true;

		//regista o cliente como connetado
		registNewConnectedUser(ipUser);

		//envia ao cliente a lista de atuais clientes conetados
		sendConnectedUsers(out);

		while (logado) { //cliente tem operacoes p/ fazer

			switch ((String)in.readObject()) {

			case "-s":
				System.out.println( "\n" + user + " recebeu uma ligacao:");
				String tema = (String) in.readObject();
				subscribeSV(tema, out, in);
				break;

			case "-n":
				System.out.println( "\n" + user + " recebeu uma ligacao:");
				String userIP = (String) in.readObject();
				newConnectionSV(userIP);
				break;

			case "-p":
				System.out.println( "\n" + user + " quer fazer upload de um ficheiro:");
				uploadFileSV(in, out, user);
				break;

			case "-c":
				System.out.println( "\n" + user + " quer fazer uma ligacao:");
				connectToSV(in, out, user);
				break;

			case "-quit":
				System.out.println("\n" + user + " fez log out.");
				logado = false;
				removeUser(user);
				break;

			default:
				System.err.println("\n O comando inserido pelo cliente " + user + "  nao estah definido.");
				break;
			}
		}

		in.close();
		out.close();
		socket.close();

	}

	private void subscribeSV(String tema, ObjectOutputStream out, ObjectInputStream in) throws ClassNotFoundException, IOException {

		File ficheirosTema = new File(rep + "/" + tema);
		File[] ficheiros = ficheirosTema.listFiles();
		int nFicheiros = ficheiros.length;

		if(nFicheiros == 0){
			out.writeObject("nExiste");
		}
		else {
			out.writeObject("existe");

			for (File file : ficheiros) {
				/*
				 *ENVIAR FICHEIRO 
				 */			
				String f = file.getAbsolutePath();
				String nome = f.substring(f.lastIndexOf("/")); //obter nome ficheiro

				byte [] sizeFile  = new byte [(int)file.length()];

				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);

				bis.read(sizeFile,0,sizeFile.length);
				bis.close();
				out.writeInt(sizeFile.length); //envia tamanho do ficheiro
				out.writeObject(nome); //enviar nome ficheiro
				out.write(sizeFile,0,sizeFile.length); //envia ficheiro byte a byte
				out.flush();

				//Recebe se correu bem ou nao
				String feed = (String) in.readObject();

				if(feed.equals("err")) {
					System.err.println("Ocorreu um erro a enviar ficheiro!");
				}else {
					System.out.println("Ficheiro enviado com sucesso");
				}
			}
		}

	}

	private void uploadFileSV(ObjectInputStream in, ObjectOutputStream out, String user) throws ClassNotFoundException, IOException {

		String res = (String) in.readObject();

		if (res.equals("existe")) {

			/*
			 * Receber ficheiro
			 */
			int tamanhoFile = in.readInt(); //recebe tamanho do ficheiro
			/*
			 * TEMA
			 */
			String tema = (String) in.readObject(); //recebe tema do ficheiro
			File temaF = new File (rep + "/" + tema);
			if (!temaF.exists())
				temaF.mkdirs();

			String nome = (String) in.readObject(); //recebe nome do ficheiro
			String pathF = temaF + "/" + nome;

			byte[] myByteArray = new byte[tamanhoFile];
			FileOutputStream fos = new FileOutputStream(pathF);
			@SuppressWarnings("resource")
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int bytesRead = in.read(myByteArray,0,myByteArray.length);
			int current = bytesRead;

			bos.write(myByteArray, 0, current);
			bos.flush();

			//acabar operacao com sucesso
			out.writeObject("ok");
			System.out.println("Ficheiro fez upload com sucesso");

		}else {
			System.err.println("Ocorreu um erro. " + user + " já fez upload deste ficheiro!");
		}

	}

	private void newConnectionSV(String userIP) throws IOException {

		registNewConnectedUser(userIP);
		registNewConnectedUserInClientFile(userIP);

	}

	private void connectToSV(ObjectInputStream in, ObjectOutputStream out, String user) throws ClassNotFoundException, IOException {

		String usersIP = (String) in.readObject();
		registNewConnectedUser(usersIP);
		out.writeObject("ok");

	}

	private void sendConnectedUsers(ObjectOutputStream out) throws IOException {

		try {

			BufferedReader br = new BufferedReader(new FileReader(connectedClients));
			StringBuilder sb = new StringBuilder();
			String ln;

			while ((ln = br.readLine()) != null) {
				sb.append(ln + '\n');
			}

			br.close();

			out.writeObject(sb.toString());

		}catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}

	}

	private void removeUser(String user) throws IOException {

		try {

			File temp = new File (connectedClients.getAbsolutePath() + ".tmp");
			BufferedReader br = new BufferedReader(new FileReader(connectedClients));
			PrintWriter pw =  new PrintWriter(new FileWriter(temp));
			String ln;

			while ((ln = br.readLine()) != null) {

				if (!ln.trim().startsWith(user)) {
					pw.println(ln);
					pw.flush();
				}
			}

			pw.close();
			br.close();

			connectedClients.delete();
			temp.renameTo(connectedClients);

		}catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}

	}

	private void registNewConnectedUser(String ipUser) throws IOException {

		//LIMPAR FICHEIRO
		PrintWriter writer = new PrintWriter(connectedClients);
		writer.print("");
		writer.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(connectedClients, true));
		bw.write(ipUser);
		bw.newLine();
		bw.flush();
		bw.close();

	}

	private void registNewConnectedUserInClientFile(String userIP) throws IOException {


		BufferedWriter bw = new BufferedWriter(new FileWriter(Client.connectedClients, true));
		bw.write(ipUser);
		bw.newLine();
		bw.flush();
		bw.close();

	}

	/**
	 * Verifica se um dado utilizador coincide com uma dada password
	 * @param user utilizador logado
	 * @param pass palavra passe
	 * @return true se coincidem, false caso contrario
	 * @throws IOException
	 */
	private boolean verificaUserPass(String user, String pass) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(users));
		String ln = null;

		while((ln = br.readLine()) != null) {
			String [] uP = ln.split(":"); 

			//uP[0] = user e uP[1] = pass
			if(uP[0].equals(user)) {
				if(uP[1].equals(pass)) {
					System.out.println("Login de " + user + " com sucesso! \n");
					br.close();
					return true;
				}else {
					System.err.println("Password errada!");
					br.close();
					return false;
				}
			}
		}

		br.close();

		System.err.println("O utilizador "+ user + " nao estah registado.");

		BufferedWriter bw = new BufferedWriter(new FileWriter(users, true));
		bw.write(user + ":" + pass);
		bw.newLine();
		bw.flush();
		bw.close();		
		System.out.println("Registo feito com sucesso! \n");

		return true;

	}

	/**
	 * Cria repositorio
	 */
	private void criaRepositorio() {

		try {
			File repositorio = new File("notFileRepositorio"); //Cria nome do repositorio

			if(repositorio.exists()) // verifica se ja existe
				System.err.println("Repositorio jah existe."); 
			else {
				System.out.println("Repositorio criado.");
				repositorio.mkdirs(); // Cria diretoria
			}

			//cria ficheiro users.txt
			users = new File(REP_FINAL + "users.txt"); 

			if(!users.exists())
				users.createNewFile();

			//repositorio de ficheiros
			rep = new File(REP_FINAL + "Ficheiros");
			if(!rep.exists())
				rep.mkdir();

			//cria ficheiro users.txt
			connectedClients = new File(REP_FINAL + "conClients.txt");
			if(!connectedClients.exists())
				connectedClients.createNewFile();

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
