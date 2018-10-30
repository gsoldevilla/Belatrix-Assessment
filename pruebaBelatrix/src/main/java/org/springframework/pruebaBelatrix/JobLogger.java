package org.springframework.pruebaBelatrix;

import java.io.File;
import java.io.IOException;
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
	private static final int ID_LOG_MESSAGE = 1;
	private static final int ID_LOG_ERROR = 2;
	private static final int ID_LOG_WARNING = 3;

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

			generarLogs(messageText, message, warning, error);
			System.out.println("Se generaron los logs satisfactoriamente");

		} else {
			System.out.println("Error en el formato del mensaje!!!"); 
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
				conexion = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://"
						+ dbParams.get("serverName") + ":" + dbParams.get("portNumber") + "/", connectionProps);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Hubo un error en la conexion con la Base de Datos");
			}
		}
		return conexion;
	}

	public static void registrarenBD(String mensaje, int tipoMensaje) throws SQLException {
		if (getConexionBD() != null) {
			Statement stmt = getConexionBD().createStatement();
			stmt.executeUpdate("insert into Log_Values('" + mensaje + "', " + String.valueOf(tipoMensaje) + ")");
			System.out.println("Mensaje registrado en la BD");
		} else {
			System.out.println("No se pudo registrar el mensaje en la BD");
		}
	}

	public static void generarLogs(String mensaje, boolean message, boolean warning, boolean error)
			throws SecurityException, IOException, SQLException {
		String l = "";
		if (error && logError) {
			l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + mensaje;
		}

		if (warning && logWarning) {
			l = l + "warning " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + mensaje;
		}

		if (message && logMessage) {
			l = l + "message " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + mensaje;
		}

		mensaje = l;

		if (logToFile) {
			logger.addHandler(getFileHandler());
			logger.log(Level.INFO, mensaje);
		}

		if (logToConsole) {
			logger.addHandler(getConsoleHandler());
			logger.log(Level.INFO, mensaje);
		}

		if (logToDatabase) {
			if (message && logMessage) {
				registrarenBD(mensaje, ID_LOG_MESSAGE);
			}

			if (error && logError) {
				registrarenBD(mensaje, ID_LOG_ERROR);
			}

			if (warning && logWarning) {
				registrarenBD(mensaje, ID_LOG_WARNING);
			}

		}
	}

	public static FileHandler getFileHandler() throws IOException {
		File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
		return fh;
	}

	public static ConsoleHandler getConsoleHandler() throws IOException {
		ConsoleHandler ch = new ConsoleHandler();
		return ch;
	}
}
