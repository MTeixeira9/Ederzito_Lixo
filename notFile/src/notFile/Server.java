package notFile;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/**
	 * Main do Server
	 * @param args Argumentos passados na linha de comandos
	 */
	public static void main(String[] args) {

		Server server  = new Server();

		if(args.length != 1)
			System.err.println("Argumentos invalidos! Não inseriu o porto!");
		else
			server.startServer(args[0]);

	}

	/**
	 * Inicia o Servidor num dado porto
	 * @param porto Porto onde o Servidor escuta ligacoes
	 */
	@SuppressWarnings("resource")
	private void startServer(String porto) {
		
		ServerSocket socket = null;

		try {
			//fazer ligacao
			socket = new ServerSocket(Integer.parseInt(porto));
		}catch(IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		System.out.println("Servidor ligado! \n");

		while(true) {
			try {
				//para cada cliente que fizer accept nesta porta cria uma thread
				Socket inSoc = socket.accept();//espera ligacoes de clientes....
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start(); //inicia thread para c/ cliente
				}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
