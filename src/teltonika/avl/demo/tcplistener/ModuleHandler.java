package teltonika.avl.demo.tcplistener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import teltonika.avl.demo.parser.AvlData;
import teltonika.avl.demo.parser.CodecStore;
import teltonika.avl.demo.tools.Tools;

public class ModuleHandler implements Runnable {

	private Socket moduleSocket;
	private String imei;
	private String liveData;

	private Jedis jedis;
	private JedisPool jedisPool;

	private String data;

	private long timestampExist;
	private long timestampRecut;

	private HashMap<String, String> donnees;

	private String[] champ;
	private String[] ClVal;
	private String[] CleValeur;

	public static String dernier_fichier;

	public ModuleHandler(Socket sock, JedisPool jedisPool) {

		this.jedisPool = jedisPool;
		this.moduleSocket = sock;

	}

	public void formatage(String strAvlDonnees) {

		donnees = new HashMap<String, String>(20);

		champ = strAvlDonnees.split(",");

		donnees.put("imei", imei);

		donnees.put("ip_source", moduleSocket.getInetAddress().getHostAddress());

		donnees.put("longitude", champ[0]);
		donnees.put("latitude", champ[1]);

		donnees.put("sat1", champ[2]);
		donnees.put("sat2", champ[3]);
		donnees.put("sat3", champ[4]);
		donnees.put("sat4", champ[5]);

		ClVal = champ[6].replaceAll("(\\[|\\])", "").split(" ");

		for (int i = 0; i < ClVal.length; i++) {
			CleValeur = ClVal[i].split("=");
			donnees.put(CleValeur[0], CleValeur[1]);
		}

		donnees.put("timestamp", champ[7].replaceAll("#", ""));
		donnees.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
				.format(new Date(System.currentTimeMillis())));
	}

	public void updateRedis() {

		timestampRecut = Long.parseLong(champ[7].replaceAll("#", ""), 10);

		System.out.println("Je suis le Thread : "
				+ Thread.currentThread().getId());

		if (10 - jedisPool.getNumActive() == 0
				|| 10 - jedisPool.getNumActive() == 1)
			System.out
					.println("Avertissement : Le nombre de ressource Disponible atteint 10");

		System.out
				.println("Le nombre de ressource Disponible dans le Pool est : "
						+ (10 - jedisPool.getNumActive()));
		
		try{
			timestampExist =  Long.parseLong(jedis.hget(imei, "timestamp"));
			
			if (timestampExist < timestampRecut)
				jedis.hmset(imei, donnees);
		}
		catch(NumberFormatException e){
			System.out.println("Nouveau IMEI : " + imei);
			jedis.hmset(imei, donnees);
		}
		
		/*
		if (jedis.hget(imei, "timestamp") != null)
			timestampExist = Long.parseLong(jedis.hget(imei, "timestamp"));

		if (timestampExist < timestampRecut)
			jedis.hmset(imei, donnees);
		*/
		jedisPool.returnResource(jedis);
	}

	public void publish() throws Exception {

		jedis = jedisPool.getResource();

		data = "{\'port\':\'" + moduleSocket.getPort();
		for (Map.Entry<String, String> entry : donnees.entrySet())
			data += "\',\'" + entry.getKey() + "\':\'" + entry.getValue();
		data += "}";

		jedis.publish(imei, data);

		Listener.time = Integer.parseInt(jedis.get("duree")) * 1000;
	}

	public void InsersionRam() {
		if (Listener.time < 10000)
			Listener.time = 10000;

		Fichier_Ram.writeData(donnees);
	}

	public void run() {
		try {

			System.out.println("New connection from module:" + moduleSocket);

			DataInputStream dis = new DataInputStream(
					moduleSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(
					moduleSocket.getOutputStream());

			imei = dis.readUTF();
			System.out.println("Module IMEI:" + imei);

			dos.writeBoolean(true);

			while (true) {
				byte[] packet = ByteWrapper.unwrapFromStream(dis);

				if (packet == null) {
					System.out.println("Closing connection: " + moduleSocket);

					break;
				}

				AvlData decoder = CodecStore.getInstance().getSuitableCodec(
						packet);

				if (decoder == null) {
					System.out.println("Unknown packet format: "
							+ Tools.bufferToHex(packet));
					dos.writeInt(0);

				} else {
					System.out.println("Codec found: " + decoder);

					AvlData[] decoded = decoder.decode(packet);

					System.out.println(new Date().toLocaleString()
							+ ": Received records:" + decoded.length);

					for (AvlData avlData : decoded) {

						liveData = "@" + imei + "," + avlData;

						System.out.println("@" + imei + "," + avlData);

						formatage(avlData.toString());

							publish();

								updateRedis();

									InsersionRam();

						System.out.println("Data " + liveData);

						new writeToFileClass().readData(liveData, imei);

					}

					dos.writeInt(decoded.length);

				}
			}
			if(Listener.debug == 1){
				 System.out.println("ecriture des données dans le fichier /log/serveur_d_ecoute_log ...");
				// Fichier_Log.appendContents("../log/serveur_d_ecoute_log",log);
			}
		} catch (EOFException ee) {

			System.out.println("Closed connection:" + moduleSocket);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
