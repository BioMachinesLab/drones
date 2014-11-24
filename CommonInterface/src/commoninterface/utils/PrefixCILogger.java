package commoninterface.utils;

import commoninterface.CILogger;

/**
 * A logger able to prefix other loggers.
 * 
 * @author alc
 */
public class PrefixCILogger implements CILogger {
	private String prefix = "";
	private CILogger originalLogger;
	
	public PrefixCILogger(CILogger originalLogger, String prefix) {
		this.originalLogger = originalLogger;
		this.prefix         = prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void logMessage(String message) {
		originalLogger.logMessage(prefix + ": " + message);
	}

	@Override
	public void logError(String error) {
		originalLogger.logMessage(prefix + ": " + error);		
	}
	
	@Override
	public void stopLogging() {
		originalLogger.stopLogging();
	}

}
