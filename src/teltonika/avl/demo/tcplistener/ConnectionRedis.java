package teltonika.avl.demo.tcplistener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ConnectionRedis {

	private JedisPoolConfig jedisPoolConfig;
	private JedisCluster jedisCluster;
	private Set<HostAndPort> jedisClusterNodes;

	private Map<String, JedisPool> nodeMap;

	// private List<JedisPool> nodePoolList;

	public ConnectionRedis(int connectionNumber) {

		jedisClusterNodes = new HashSet<HostAndPort>();

		System.out.println("Initialisation des nodes ...");
		// Jedis cluster va tenter de decouvrir les noeuds de cluster
		// automatiquement
		jedisClusterNodes.add(new HostAndPort("193.34.18.47", 6379));

		// Configuration du Pool de connexion
		System.out.println("Ouverture de " + connectionNumber
				+ " Connextion sur chaque node...");

		jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(connectionNumber);
		jedisPoolConfig.setMaxIdle(connectionNumber);

		jedisCluster = new JedisCluster(jedisClusterNodes, jedisPoolConfig);
		System.out.println("Actuellement "
				+ jedisCluster.getClusterNodes().size() + " nodes en cluster");

		nodeMap = jedisCluster.getClusterNodes();

		/*
		 * nodePoolList = new ArrayList<JedisPool>(nodeMap.values());
		 * Collections.shuffle(nodePoolList);
		 */

	}

	public JedisPool getPool() {
		return nodeMap.get("193.34.18.47:6379");
	}

	public void closePools() {
		nodeMap.get("193.34.18.47:6379").close();
	}

	public void destroyPool() {
		nodeMap.get("193.34.18.47:6379").destroy();
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

}