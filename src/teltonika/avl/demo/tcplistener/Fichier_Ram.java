package teltonika.avl.demo.tcplistener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Fichier_Ram implements Runnable {

	private long date_current;
	private String date;
	private long timestamp;

	private static Vector<Map<String, String>> donnees;
	private static int index;

	private Jedis jedis;
	private JedisPool jedisPool;

	private String enregistrement;

	private FileWriter writer;
	private BufferedWriter bw;

	public Fichier_Ram(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
		date_current = System.currentTimeMillis();
		index = 0;
		enregistrement = "";

		donnees = new Vector<Map<String, String>>();

		// Interception de Ctr^C (Fermeture du Fichier RAM et LOG)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("CTR-C Intercepte.");

				if (bw != null) {
					System.out.println("Fermeture fichier : FichierRam");
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (Fichier_Log.oWriter != null) {
					System.out.println("Fermeture fichier : FichierLog");
					Fichier_Log.CloseLogFile();
				}
			}
		});

		// Interception de Ctr^Z (Flushing Data ...)
		Signal.handle(new Signal("TSTP"), new SignalHandler() {
			public void handle(Signal sig) {
				System.out.println("CTR-Z Intercepte Flushing Data ...");
				donnees.removeAllElements();
			}
		});
	}

	public static void writeData(HashMap<String, String> element) {
			donnees.add(index,element);
			index++;
	}

	public void run() {
		jedis = jedisPool.getResource();

		while (true) {

			if (date_current <= System.currentTimeMillis() - Listener.time) {
				try {
					timestamp = System.currentTimeMillis();
					date = new SimpleDateFormat("yyyyddMM_HHmmss")
							.format(new Date(timestamp));

					jedis.set("dernier_fichier", "machine47_data_geo_" + date);
					jedis.set("dernier_fichier_timestamp",
							String.valueOf(timestamp));

					writer = new FileWriter(
							"/media/virtuelram/machine47_data_geo_" + date,
							true);

					System.out.println("Creation du fichier RAM ("
							+ Listener.time / 1000 + "s) : "
							+ "machine47_data_geo_" + date);

					bw = new BufferedWriter(writer);

					if (donnees.size() > 0) {
						System.out.println("Insersion de " + donnees.size()
								+ " enregistrements");
						System.out.println("Insersion en cours ...");
					}

					for(int i=0;i<donnees.size();i++){
							for (Map.Entry<String, String> e : 
								donnees.get(i)
									.entrySet())
								enregistrement += e.getKey() + ":"
										+ e.getValue() + "\t";

							bw.write(enregistrement);
							bw.newLine();
						}
					donnees.removeAllElements();
					index =0;
					bw.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

				date_current = System.currentTimeMillis();
			}
		}
	}
}