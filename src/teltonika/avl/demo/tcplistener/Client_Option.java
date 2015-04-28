package teltonika.avl.demo.tcplistener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Client_Option {
 private static final Logger log = Logger.getLogger(Client_Option.class.getName());
 private String[] args = null;
 private Options options = new Options();

 public Client_Option(String[] args) {

  this.args = args;

  options.addOption("p", "port"  ,true , 	"Le port d'ecoute(4000).");
  options.addOption("g", "debug" ,true , 	"Active le mode debug(desactiver).");
  options.addOption("h", "aide"  ,false, 	"Affichier l\'aide.");
  options.addOption("d", "duree" ,true , 	"Duree de creation de chaque fichier (10s).");  
  options.addOption("n", "nombre",true , 	"Nombre de connexion sur chaque noeud(10).");

 }

 public void parse() {
  CommandLineParser parser = new BasicParser();

  CommandLine cmd = null;
  try {
   cmd = parser.parse(options, args);

   if (cmd.hasOption("h"))	help();

   if (cmd.hasOption("p"))	Listener.time  		= Integer.parseInt(cmd.getOptionValue("p"));
   
   if(cmd.hasOption("d"))	Listener.time 		= Integer.parseInt(cmd.getOptionValue("d"));
   
   if(cmd.hasOption("g"))	Listener.debug 		= Integer.parseInt(cmd.getOptionValue("g"));
   
   if(cmd.hasOption("n"))	Listener.connection = Integer.parseInt(cmd.getOptionValue("n"));
   
   
  } catch (ParseException e) {
   log.log(Level.SEVERE, "Lecture des proprietes a ete echoue. ", e);
   help();
  }
 }

 private void help() {
  // Cela affiche les options possible
  HelpFormatter formater = new HelpFormatter();

  formater.printHelp("Main", options);
  System.exit(0);
 }
}

