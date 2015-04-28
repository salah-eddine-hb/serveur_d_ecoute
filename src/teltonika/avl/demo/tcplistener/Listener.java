package teltonika.avl.demo.tcplistener;

import java.net.ServerSocket;
import java.net.Socket;

import teltonika.avl.demo.parser.AvlData;
import teltonika.avl.demo.parser.AvlDataFM4;
import teltonika.avl.demo.parser.AvlDataGH;
import teltonika.avl.demo.parser.CodecStore;

public class Listener {

	public static int time = 10000;
	public static int port = 4000;
	public static int debug = 0;
	public static int connection = 10;

	public static void main(String[] args) throws Exception {

		new Client_Option(args).parse();

		// register supported codecs
		CodecStore.getInstance().register(AvlData.getCodec());
		CodecStore.getInstance().register(AvlDataFM4.getCodec());
		CodecStore.getInstance().register(AvlDataGH.getCodec());

		// Intialisation du Pool de Connextion Redis (Je vais ouvrir 10
		// Connexion sur chaque Node)
		ConnectionRedis connextionRedis = new ConnectionRedis(connection);

		// Thread de Creation Fichier RAM chaque 10s
		new Thread(new Fichier_Ram(connextionRedis.getPool())).start();

		// Socket d'ecoute des donnees
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Listening on TCP port " + port);

		while (true) {
			Socket sock = serverSocket.accept();
			new Thread(new ModuleHandler(sock, connextionRedis.getPool())).start();

			System.out.println("Thread est Lancé ..");
		}
	}
}
