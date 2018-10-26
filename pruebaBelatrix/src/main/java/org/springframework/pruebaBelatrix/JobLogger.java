package org.springframework.pruebaBelatrix;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
	private static boolean logToFile;
	private static boolean logToConsole;
	private static boolean logMessage;
	private static boolean logWarning;
	private static boolean logError;
	private static boolean logToDatabase;
	private static Map<String, String> dbParams;
	private static Logger logger;
	private static Connection conexion;

	public JobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
			boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map<String, String> dbParamsMap) {
		logger = Logger.getLogger("MyLog");
		logError = logErrorParam;
		logMessage = logMessageParam;
		logWarning = logWarningParam;
		logToDatabase = logToDatabaseParam;
		logToFile = logToFileParam;
		logToConsole = logToConsoleParam;
		dbParams = dbParamsMap;
	}

	public static void LogMessage(String messageText, boolean message, boolean warning, boolean error)
			throws Exception {
		if (validarFormatoMensaje(messageText)) {
			if (!logToConsole && !logToFile && !logToDatabase) {
				throw new Exception("Invalid configuration");
			}
			if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
				throw new Exception("Error or Warning or Message must be specified");
			}

			int t = 0;
			if (message && logMessage) {
				t = 1;
			}

			if (error && logError) {
				t = 2;
			}

			if (warning && logWarning) {
				t = 3;
			}

			

			String l = null;
			File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
			}

			FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
			ConsoleHandler ch = new ConsoleHandler();

			if (error && logError) {
				l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}

			if (warning && logWarning) {
				l = l + "warning " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}

			if (message && logMessage) {
				l = l + "message " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}

			if (logToFile) {
				logger.addHandler(fh);
				logger.log(Level.INFO, messageText);
			}

			if (logToConsole) {
				logger.addHandler(ch);
				logger.log(Level.INFO, messageText);
			}
			
			

			
		}

	}

	public static boolean validarFormatoMensaje(String mensaje) {
		if (mensaje == null) {
			return false;
		} else if (mensaje.trim().length() == 0) {
			return false;
		} else {
			return true;
		}

	}

	public static Connection getConexionBD() {

		if (conexion == null) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", dbParams.get("userName"));
			connectionProps.put("password", dbParams.get("password"));

			try {				
				conexion = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
						+ ":" + dbParams.get("portNumber") + "/", connectionProps);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Hubo un error en la conexion con la Base de Datos");				
			}			
		}
		return conexion;
	}
	
	public static void registrarenBD(String mensaje, int tipoMensaje) throws SQLException {
		if (getConexionBD()!=null) {
			Statement stmt = getConexionBD().createStatement();
			stmt.executeUpdate("insert into Log_Values('" + mensaje + "', " + String.valueOf(tipoMensaje) + ")");
			System.out.println("Mensaje registrado en la BD");
		} else {
			System.out.println("No se pudo registrar el mensaje en la BD");
		}
	}
}
