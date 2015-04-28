package teltonika.avl.demo.tcplistener;

import java.io.*;

public class Fichier_Log {

	public static BufferedWriter oWriter;
	public static File oFile;

	public static synchronized void appendContents(String sFileName,
			String sContent) {
		try {

			oFile = new File(sFileName);
			if (!oFile.exists()) {
				oFile.createNewFile();
			}
			if (oFile.canWrite()) {
				oWriter = new BufferedWriter(new FileWriter(sFileName, true));
				oWriter.write(sContent);
				oWriter.close();
			}

		} catch (IOException oException) {
			throw new IllegalArgumentException(
					"Error appending/File cannot be written: \n" + sFileName);
		}
	}

	public static synchronized void CloseLogFile() {

		if (oWriter != null)
			try {
				oWriter.close();

			} catch (IOException e) {

				e.printStackTrace();
			}

	}
}