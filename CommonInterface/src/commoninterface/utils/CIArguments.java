package commoninterface.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursive configuration argument of the form
 * "--argument identifier1=value1,identifier2=(id21=value21,id...)" The
 * Arguments class is can parse a string of arguments and allows for querying
 * arguments and their values. Furthermore, checking mechanisms are in place to
 * ensure that all arguments have been queried. Examples of argument string and
 * how to parse them:
 * <p>
 * Non-case sensitive,parses parenthesis correctly
 * 
 * @author alc
 * 
 */

public class CIArguments implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME_TAG = "classname";

	/**
	 * Complete unparsed argument string
	 */
	protected String unparsedArgumentString;

	/**
	 * Keeps track of which arguments have been queried and which have not.
	 */
	protected Vector<Boolean> argumentQueried = new Vector<Boolean>();
	/**
	 * Vector of the arguments their corresponding values are found in "values".
	 */
	protected Vector<String> arguments = new Vector<String>();
	/**
	 * Vector of the values for the arguments in "arguments".
	 */
	protected Vector<String> values = new Vector<String>();
	
	protected String loadedFile = "";

	/**
	 * Initializes a new arguments instance and sets all arguments to
	 * non-queried.
	 * 
	 * @param unparsedArgumentString
	 *            Raw, unparsed argument string.
	 * @throws MultipleClassDefinitionException
	 * @throws ClassNotFoundException
	 */
	public CIArguments(String unparsedArgumentString) {
		parseString(unparsedArgumentString);
		removeRepeated();
	}
	
	public CIArguments(String unparsedArgumentString, boolean translateClasses) {
		if(translateClasses) {
			unparsedArgumentString = translateClasses(unparsedArgumentString);
//			System.out.println(unparsedArgumentString);
		}
		parseString(unparsedArgumentString);
		removeRepeated();
	}

	private void removeRepeated() {
		Iterator<String> iArgs = arguments.iterator();
		Iterator<String> iValues = values.iterator();
		
		ArrayList<String> found = new ArrayList<String>();
		
		while(iArgs.hasNext()) {
			String current = iArgs.next();
			iValues.next();
			if(found.contains(current)) {
				iArgs.remove();
				iValues.remove();
			}else {
				found.add(current);
			}
		}
	}
	
	public static String replaceAndGetArguments(String argName, String arg ,String by,List<String> removedStrings){
		Pattern p = Pattern.compile(argName+"=\\w*(\\.\\w*)*");
	 	Matcher m = p.matcher(arg);

		while(m.find()) {
			String found = m.group();
			String[] split = found.split("=");
			removedStrings.add(split[1]);
			arg = m.replaceFirst(by);
			m = p.matcher(arg);
	 	}
		return arg;
	}
	
	public static String repleceTagByStrings(String argName, String arg, String tag, List<String> newStrings){
		for(String s : newStrings)
			arg = arg.replaceFirst(tag, argName+"="+s);
		return arg;
	}

	/**
	 * Parses a string of arguments.
	 * 
	 * @param unparsedArgumentString
	 *            the raw, unparsed argument string.
	 */
	protected void parseString(String unparsedArgumentString) {
		this.unparsedArgumentString = unparsedArgumentString;

		int stringIndex = 0;
		boolean currentlyValue = false;
		int parenthesisLevel = 0;

		StringBuffer currentArgument = new StringBuffer();
		StringBuffer currentValue = new StringBuffer();

		while (stringIndex < unparsedArgumentString.length()) {
			char currentChar = unparsedArgumentString.charAt(stringIndex++);
			if (currentChar == '(') {
				if (parenthesisLevel > 0 && currentlyValue)
					currentValue.append(currentChar);

				parenthesisLevel++;

			} else if (currentChar == ')') {
				parenthesisLevel--;

				if (parenthesisLevel > 0 && currentlyValue)
					currentValue.append(currentChar);

			} else if (currentChar == '=' && parenthesisLevel == 0) {
				if (currentArgument.length() == 0)
					throw new java.lang.RuntimeException(
							"Something is wrong. Argument starts with a = "
									+ unparsedArgumentString);

				currentlyValue = true;
			} else if (currentChar == ',' && parenthesisLevel == 0) {
				if (currentArgument.length() > 0) {
					argumentQueried.add(new Boolean(false));
					arguments.add(currentArgument.toString());
					values.add(currentValue.toString());
					currentArgument = new StringBuffer();
					currentValue = new StringBuffer();
				}
				currentlyValue = false;
			} else if (!currentlyValue) {
				currentArgument.append(currentChar);
			} else
				currentValue.append(currentChar);
		}

		if (currentArgument.length() > 0) {
			if (!getArgumentIsDefined(currentArgument.toString())) {
				argumentQueried.add(new Boolean(false));
				arguments.add(currentArgument.toString());
				values.add(currentValue.toString());
			}
		}
		
	}

	/**
	 * Get the String value of an argument.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @return the value of the argument or null if the argument does not exist
	 */
	public String getArgumentValue(String argument) {
		int index = 0;
		boolean found = false;

		while (index < arguments.size() && !found) {
			if (arguments.elementAt(index).equalsIgnoreCase(argument))
				found = true;
			else
				index++;
		}

		if (!found)
			return null;
		else {
			argumentQueried.setElementAt(Boolean.TRUE, index);
			return values.elementAt(index);
		}
	}

	/**
	 * Query an argument to discover if it is defined.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @return true if the argument was defined, false otherwise.
	 */
	public boolean getArgumentIsDefined(String argument) {
		if (getArgumentValue(argument) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the int value of an argument. Use {@link #getArgumentIsDefined} to
	 * query if the argument is defined before calling this method.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @return the int value of the argument or 0 if the argument does not
	 *         exist.
	 */
	public int getArgumentAsInt(String argument) {
		return getArgumentAsIntOrSetDefault(argument, 0);
	}

	/**
	 * Get the double value of an argument or return the default value in case
	 * the argument does not exist.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @param defaultValue
	 *            default value returned in case the argument isn't defined.
	 * @return the double value of the argument or the default value if it does
	 *         not exist.
	 */

	public int getArgumentAsIntOrSetDefault(String argument, int defaultValue) {
		String value = getArgumentValue(argument);
		if (value == null) {
			return defaultValue;
		}

		int result = defaultValue;
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Trying to parse the argument \""
					+ argument + "\" as an int, but the specified value \""
					+ value + "\" isn't an int");
		}
		return result;

	}

	/**
	 * Set the value of an argument. If the argument is not define, a new one
	 * will be added.
	 * 
	 * @param argument
	 *            the argument for which a new value should be set
	 * @value the new value for the argument
	 */
	public void setArgument(String argument, String value) {
		if (getArgumentIsDefined(argument)) {
			int index = 0;
			boolean found = false;

			while (index < arguments.size() && !found) {
				if (arguments.elementAt(index).equalsIgnoreCase(argument))
					found = true;
				else
					index++;
			}
			arguments.set(index, argument);
			values.set(index, value);
		} else {
			arguments.add(argument);
			values.add(value);
			argumentQueried.add(Boolean.FALSE);
		}
	}

	/**
	 * Set the value of an argument. If the argument is not define, a new one
	 * will be added.
	 * 
	 * @param argument
	 *            the argument for which a new value should be set
	 * @value the new value for the argument
	 */
	public void setArgument(String argument, int value) {
		setArgument(argument, "" + value);
	}

	/**
	 * Set the value of an argument. If the argument is not define, a new one
	 * will be added.
	 * 
	 * @param argument
	 *            the argument for which a new value should be set
	 * @value the new value for the argument
	 */
	public void setArgument(String argument, double value) {
		setArgument(argument, "" + value);
	}

	/**
	 * Get the double value of an argument. Use {@link #getArgumentIsDefined} to
	 * query if the argument is defined before calling this method.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @return the double value of the argument or 0 if the argument does not
	 *         exist.
	 */
	public double getArgumentAsDouble(String argument) {
		return getArgumentAsDoubleOrSetDefault(argument, 0.0);
	}

	/**
	 * Get the double value of an argument or return the default value in case
	 * the argument does not exist.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @param defaultValue
	 *            default value returned in case the argument isn't defined.
	 * @return the double value of the argument or the default value if it does
	 *         not exist.
	 */
	public double getArgumentAsDoubleOrSetDefault(String argument,
			double defaultValue) {
		String value = getArgumentValue(argument);
		if (value == null) {
			return defaultValue;
		}

		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Trying to parse the argument \""
					+ argument + "\" as a double, but the specified value \""
					+ value + "\" isn't a double");
		}
	}

	/**
	 * Get the String value of an argument. Use {@link #getArgumentIsDefined} to
	 * query if the argument is defined before calling this method.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @return the value of the argument or null if the argument does not exist.
	 */
	public String getArgumentAsString(String argument) {
		return getArgumentValue(argument);
	}

	/**
	 * Get the String value of an argument or return the default value in case
	 * the argument does not exist.
	 * 
	 * @param argument
	 *            case insensitive name of the argument
	 * @param defaultValue
	 *            default value returned in case the argument is not defined.
	 * @return the value of the argument or null if the argument does not exist.
	 */
	public String getArgumentAsStringOrSetDefault(String argument,
			String defaultValue) {
		String value = getArgumentValue(argument);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Get the complete argument String. Any modifications made after the
	 * arguments were parsed are included.
	 * 
	 * @return the unparsed string containing all arguments and values
	 */
	public String getCompleteArgumentString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arguments.size(); i++) {
			if (sb.length() > 0)
				sb.append(",");
			
			sb.append(arguments.get(i));
			if (values.get(i).length() > 0) {
				if(values.get(i).contains("=") || values.get(i).contains(","))
					sb.append("=(" + values.get(i)+")");
				else
					sb.append("=" + values.get(i));
			}
		}
		return sb.toString();
	}

	/**
	 * Get the number of arguments in the rare string.
	 * 
	 * @return the number of arguments in the raw, unparsed argument string
	 */
	public int getNumberOfArguments() {
		return arguments.size();
	}

	/**
	 * Check if all arguments have been queried. This should be called after
	 * anyone interested in querying the arguments have had the chance. In case
	 * not all of the arguments have been queried, the method
	 * {@link #getUnqueiredArgument} can be used to figure out which arguments
	 * have not been queried.
	 * 
	 * @return true if all the arguments have been queried, false otherwise.
	 */
	public boolean checkIfAllArgumentsHaveBeenQueried() {
		int i = 0;
		boolean allQueriedSoFar = true;

		while (i < argumentQueried.size() && allQueriedSoFar) {
			allQueriedSoFar = argumentQueried.elementAt(i++).booleanValue();
		}

		return allQueriedSoFar;
	}

	/**
	 * Check if all arguments have been queried. This should be called after
	 * anyone interested in querying the arguments have had the chance.
	 * 
	 * @return true if all the arguments have been queried, false otherwise.
	 */
	public String getUnqueriedArgument() {
		int i = 0;
		boolean allQueriedSoFar = true;
		String result = null;

		while (i < argumentQueried.size() && allQueriedSoFar) {
			allQueriedSoFar = argumentQueried.elementAt(i).booleanValue();
			if (allQueriedSoFar)
				i++;
			else {
				result = arguments.elementAt(i) + "=" + values.elementAt(i);
			}
		}
		return result;
	}

	/**
	 * Get the argument at a specific index.
	 * 
	 * @param index
	 *            index of the argument (the argument name and not its value!)
	 * @return name of the argument at the specified index.
	 */
	public String getArgumentAt(int index) {
		return arguments.elementAt(index);
	}

	/**
	 * Get the value at a specific index.
	 * 
	 * @param index
	 *            index of the value
	 * @return value argument at the specified index.
	 */
	public String getValueAt(int index) {
		return values.elementAt(index);
	}

	/**
	 * Query if a argument interpreted as a flag is set to true. A flag is on if
	 * the flag appears in argument list and if is has a value of one of (yes,
	 * on, 1, enable, enabled, true) otherwise this method will return false.
	 * 
	 * @see #getFlagIsFalse(String)
	 * @param argument
	 *            case insensitive name of the argument
	 * @return true if the flag is defined and on.
	 */
	public boolean getFlagIsTrue(String argument) {
		String value = getArgumentValue(argument);
		if (value == null) {
			return false;
		} else if (value.equalsIgnoreCase("true")
				|| value.equalsIgnoreCase("on")
				|| value.equalsIgnoreCase("enable")
				|| value.equalsIgnoreCase("enabled")
				|| value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Query if a argument interpreted as a flag is set to false. A flag is off
	 * if the flag appears in argument list and if is has a value of one of (no,
	 * off, 0, disable, disabled, false) otherwise this method will return
	 * false. Note, that a flag is not returned as being false if it not
	 * defined.
	 * 
	 * @see #getFlagIsTrue(String)
	 * @param argument
	 *            case insensitive name of the argument
	 * @return true if the flag is defined and off (although this may be
	 *         counter-intuitive, the opposite would have been ambiguous).
	 */
	public boolean getFlagIsFalse(String argument) {
		String value = getArgumentValue(argument);
		if (value == null) {
			return false;
		} else if (value.equalsIgnoreCase("false")
				|| value.equalsIgnoreCase("off")
				|| value.equalsIgnoreCase("disable")
				|| value.equalsIgnoreCase("disabled")
				|| value.equalsIgnoreCase("no") || value.equalsIgnoreCase("0")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String[] readOptionsFromFile(String filename)
			throws IOException {
		String oldString = readContentFromFile(filename);
		return readOptionsFromString(oldString);
	}

	public static String readContentFromFile(String filename)
			throws IOException {
		BufferedReader bufferedReader;
		StringBuffer sb = new StringBuffer();
		String nextLine;
		
		bufferedReader = new BufferedReader(new InputStreamReader(
				new DataInputStream(new FileInputStream(filename))));

		while ((nextLine = bufferedReader.readLine()) != null) {
			int index = nextLine.indexOf('#');
			if (index > -1) {
				if (index == 0)
					nextLine = "";
				else
					nextLine = nextLine.substring(0, index - 1);
			}

			sb.append(nextLine + " ");
		}

		return sb.toString();
	}

	public static String[] readOptionsFromString(String oldString) {

		String newString = oldString;
		do {
			oldString = newString;
			newString = oldString.replace("  ", " ");
			newString = newString.replace("\t", " ");
			newString = newString.replace("\n", " ");
			newString = newString.replace(", ", ",");
			newString = newString.replace("( ", "(");
			newString = newString.replace(" )", ")");
		} while (!newString.equals(oldString));

		return newString.trim().split(" ");
	}
	
	public static CIArguments createOrPrependArguments(CIArguments previous, String newArgumentString) {
		try {
			return createOrPrependArguments(previous, newArgumentString,false);
		} catch(Exception e){e.printStackTrace();}
		return null;
	}

	private static CIArguments createOrPrependArguments(CIArguments previous,String newArgumentString, boolean translateArguments) throws ClassNotFoundException {
		if (newArgumentString.charAt(0) == '+') {
			if (previous != null) {
				return new CIArguments(newArgumentString.substring(1,
						newArgumentString.length())
						+ ","
						+ previous.getCompleteArgumentString());
			} else {
				return new CIArguments(newArgumentString.substring(1,
						newArgumentString.length()));
			}
		} else {
			return new CIArguments(newArgumentString);
		}
	}

	public String toString() {
		return getCompleteArgumentString();
	}
	
	public Vector<String> getArguments() {
		return arguments;
	}
	
	public Vector<String> getValues() {
		return values;
	}
	
	public static String beautifyString(String s) {
		
		int nParenthesis = 0;
		String newString = "\t";
		
		for(int i = 0 ; i < s.length(); i++) {
			char c = s.charAt(i);
			
			switch(c) {
				case ',':
					newString+=",\n";
					newString+=repeatString("\t", nParenthesis+1);
					break;
				case '(':
					nParenthesis++;
					newString+="(\n";
					newString+=repeatString("\t", nParenthesis+1);
					break;
				case ')':
					nParenthesis--;
					newString+="\n";
					newString+=repeatString("\t", nParenthesis+1);
					newString+=")";
					break;
				default:
					newString+=c;
			}
		}
		return newString;
	}
	
	public static String repeatString(String s, int n) {
		String newString = "";
		
		for(int i = 0 ; i < n ; i++)
			newString+=s;
		
		return newString;
	}
	
	public void removeArgument(String key) {
		int index = arguments.indexOf(key);
		
		if(index >= 0) {
			arguments.remove(index);
			values.remove(index);
		}
	}
	
	private String translateClasses(String arg) {
		Pattern p = Pattern.compile("classname=\\w*(\\.\\w*)*");
	 	Matcher m = p.matcher(arg);
	 	
	 	LinkedList<String> strings = new LinkedList<String>();
		while(m.find()) {
			String found = m.group();
			String[] split = found.split("=");
			strings.add(split[1]);
			arg = m.replaceFirst("__PLACEHOLDER__");
			m = p.matcher(arg);
	 	}
		
		for(int i = 0 ; i < strings.size() ; i++) {
			List<String> names = ClassSearchUtils.searchFullNameInPath(strings.get(i));
			if (names.size() == 0) {
				String[] split = strings.get(i).split("\\.");
				names = ClassSearchUtils.searchFullNameInPath(split[split.length-1]);
				
				if (names.size() == 0) {
					
					//We're really sorry for this crappy code, but we needed this for legacy support :( authors: Miguel and Tiago
					if(strings.get(i).endsWith("NNInput")) {
						List<String> sensorinputnames = ClassSearchUtils.searchFullNameInPath("SensorNNInput");
						strings.set(i, sensorinputnames.get(0));
						continue;
					} if(strings.get(i).contains("Simple") && strings.get(i).contains("Sensor")) {
						String currentString = strings.get(i);
						currentString = currentString.replace("Simple", "");
						List<String> sensorinputnames = ClassSearchUtils.searchFullNameInPath(currentString);
						strings.set(i, sensorinputnames.get(0));
						continue;
					} else {
						throw new RuntimeException("Class not found "+ strings.get(i));
					}
				}
			} else if (names.size() > 1) {
				throw new RuntimeException("Multiple implementations of class: "+ strings.get(i) + " - " + names);
			}
			strings.set(i, names.get(0));
		}
		
		for(String s : strings)
			arg = arg.replaceFirst("__PLACEHOLDER__", CLASS_NAME_TAG+"="+s);
		return arg;
	}
	
	public static HashMap<String, CIArguments> parseArgs(String[] args)
			throws IOException, ClassNotFoundException {
		String optionsFilename = null;
		
//		 for(String s : args)
//			 System.out.println(s);

		int currentIndex = 0;

		// if (args.length == 1) {
		// if (args[currentIndex].equalsIgnoreCase("-h")
		// || args[currentIndex].equalsIgnoreCase("--help")
		// || args[currentIndex].equalsIgnoreCase("help")) {
		// System.out.println(Util.usageToString());
		// System.exit(0);
		// }
		
		if (args[0].charAt(0) != '-') {
			optionsFilename = args[0];
			String[] argsFromFile = readOptionsFromFile(optionsFilename);
			String[] argsFromCommandline = args;
			String[] newArgs = new String[argsFromFile.length + args.length - 1];

			// System.out.println("file: " + argsFromFile.length + " cmd: " +
			// args.length + ", new: " + newArgs.length);

			for (int i = 0; i < argsFromFile.length; i++) {
				newArgs[i] = argsFromFile[i];
			}

			for (int i = 1; i < argsFromCommandline.length; i++) {
				newArgs[argsFromFile.length + i - 1] = argsFromCommandline[i];
			}

			args = newArgs;
		}

		HashMap<String, CIArguments> result = new HashMap<String, CIArguments>();

		while (currentIndex < args.length) {
			if (currentIndex + 1 == args.length) {
				throw new RuntimeException(("Error: " + args[currentIndex]
						+ " misses an argument"));
			}

			if (!args[currentIndex].equalsIgnoreCase("--random-seed")
					&& args[currentIndex + 1].charAt(0) == '-') {
				throw new RuntimeException("Error: Argument for " + args[currentIndex]
				                                 						+ " cannot start with a '-' (and therefore cannot be "
				                                						+ args[currentIndex + 1] + ")");
			}

			String key = args[currentIndex].toLowerCase();
			// this replaces the big ol' if then
			// System.out.println(args[currentIndex]+" # "+args[currentIndex+1]);
			result.put(
					key,
					createOrPrependArguments(result.get(key),
							args[currentIndex + 1],true));

			currentIndex += 2;
		}

		String commandLine = "";

		for (String s : args) {
			if (s.startsWith("--"))
				commandLine += "\n";
			commandLine += s + " ";
		}
		
//		Arguments commandLineArguments = new Arguments(commandLine.trim(),true);
//		result.put("commandline",result);
		
		if(optionsFilename != null && result.get("--output") != null) {
			String pop = null;
			
			if(result.containsKey("--population")){
				pop = result.get("--population").getCompleteArgumentString();
			}else{
				pop = result.get("--populationa").getCompleteArgumentString();
			}
			
			if(pop != null && pop.contains("load")) {
				String output = result.get("--output").getCompleteArgumentString();
				
				String parent = new File(optionsFilename).getParent();
				
				if(!parent.equals(output)) {
					result.put("--output", new CIArguments(parent));
				}
			}
		}
		
		return result;
	}
	
}