package verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.jmlspecs.checker.JmlOptions;
import org.jmlspecs.checker.Main;

/**
 * This class represents a Java program with JML specifications
 * 
 * A program is defined by
 * 
 * <li> a source folder 						:	where the class should be found, e.g.: {@code src/} </li>
 * <li> a class name 							:	the full class name (including package), e.g.: {@code main.util.Pair} </li>
 * <li> the absolute path						:	the full path to the program's file, e.g.: {@code /Users/rupert/project1/src/main/util/Pair.java} </li>
 * <li> a file									:	the file which contains the java program </li>
 * 
 * @author Nazareno Matías Aguirre
 * @version 0.4
 */
public class JMLAnnotatedClass {
	
	/**
	 * System path separator
	 */
	private static final String SEPARATOR = "/"; //FIXME: improve this 
	
	/**
	 * stores the source folder of the file corresponding to the program.
	 */
	private String sourceFolder;
	
	/**
	 * stores the qualified name of the class corresponding to the program.
	 */
	private String className;
	
	/**
	 * stores the absolute path name of the file containing the class.
	 */
	private String absPath;
	
	/**
	 * stores the file corresponding to the program.
	 */
	private File program;
	
	/**
	 * stores the package of the class 
	 */
	private String classPackage;

	/**
	 * Constructor for class {@code JMLAnnotatedClass}. It creates a {@code JMLAnnotatedClass} instance from a given file name.
	 * the file will not be validated in this constructor. 
	 * @param sourceFolder	:	where the class should be found, e.g.: {@code src/}				: {@code String}
	 * @param className 	:	the qualified name of the class corresponding to the program	: {@code String}
	 * <hr>
	 * <b> note:  the class is assumed to be in a .java file </b>
	 */
	public JMLAnnotatedClass(String sourceFolder, String className) {
		if (!isReadable(sourceFolder, className)) throw new IllegalArgumentException("creating program with non existent file");
		this.sourceFolder = toPath(sourceFolder, "");
		this.className = className;
		this.program = new File(toPath(sourceFolder, className)+".java");
		this.absPath = toPath((new File(toPath(sourceFolder, ""))).getAbsolutePath(), "");		
		String fullClassName = className;
		int lastPathSeparatorIndex = fullClassName.lastIndexOf("");
		if (lastPathSeparatorIndex == -1) {
			this.classPackage = "";
		} else {
			this.classPackage = toPath(fullClassName.substring(0, lastPathSeparatorIndex), "");
		}
	}
	
	public void moveLocation(String sourceFolder) {
		if (!isReadable(sourceFolder, this.className)) throw new IllegalArgumentException("creating program with non existent file");
		this.sourceFolder = toPath(sourceFolder, "");
		this.program = new File(toPath(sourceFolder, className)+".java");
		this.absPath = toPath((new File(toPath(sourceFolder, ""))).getAbsolutePath(), "");	
	}
	
	private static String toPath(String sourceFolder, String className) {
		String path = sourceFolder;
		if (!path.endsWith(SEPARATOR)) {
			path += SEPARATOR;
		}
		path += className;
		path = path.replaceAll("\\.", SEPARATOR);
		return path;
	}
	
	/**
	 * @return the package of the class
	 */
	public String getClassPackage() {
		return this.classPackage;
	}
	
	/**
	 * @return the source folder of the file corresponding to the program : {@code String}
	 */
	public String getSourceFolder() {
		return this.sourceFolder;
	}
	
	/**
	 * @return the qualified name of the class corresponding to the program : {@code String}
	 */
	public String getClassName() {
		return this.className;
	}
	
	/**
	 * @return the qualified name of the class but with each dot replaced by /
	 */
	public String getClassNameAsPath() {
		return this.className.replaceAll("\\.", SEPARATOR);
	}
	
	/**
	 * @return the absolute path name of the file containing the class : {@code String}
	 */
	public String getAbsolutePath() {
		return this.absPath;
	}
	
	/**
	 * @return the file corresponding to the program : {@code String}
	 */
	public File getProgramFile() {
		return this.program;
	}

	/**
	 * Checks whether a given class name corresponds to an actual file in the file system.
	 * @param sourceFolder	:	the source folder where the class is located e.g.: {@code src/}	:	{@code String} 
	 * @param className 	:	the qualified name of the class	e.g.: {main.util.Pair}			:	{@code String}
	 * @return {@code true} iff the class is an actual file (it exists and is not a directory) and can be read.
	 */
	public static boolean isReadable(String sourceFolder, String className) {
		File file = new File(toPath(sourceFolder, className)+".java");
		return (file.exists() && file.canRead() && !file.isDirectory());
	}

	/** 
	 * Checks whether this instance represents a valid JML-annotated Java program.
	 * @return {@code true} iff program compiles correctly (including JML parsing).
	 * FIXME the way we are calling the compiler here is "borrowed" from JmlParser. It must be improved.
	 */
	public boolean isValid() {

		JmlOptions options = new JmlOptions("jml");


		String classPath = System.getProperty("java.class.path")+":"+this.absPath;

		options.set_classpath(classPath);
		options.set_sourcepath(this.absPath);

		// Allow generic source code (experimental)
		options.set_generic(true);

		// Parse assertions and other Java 1.4 syntax
		options.set_source("1.4");

		// Deny multi-java code
		options.set_multijava(false);

		// Type-checking configuration
		options.set_purity(true);
		options.set_assignable(true);
		options.set_Assignable(true);
		options.set_universesx("no");

		// Verbose
		options.set_verbose(false);
		options.set_Quiet(!options.verbose());
		options.set_quiet(!options.verbose());

		// Experimental options
		options.set_keepGoing(false);

		Main parser = new Main();

		String[] names = {this.program.getAbsolutePath()};
		return parser.run(names, options, null);			
	}
	
	/**
	 * @return the path to the program asociated with this instance
	 */
	public String getFilePath() {
		return this.program.getAbsolutePath();
	}
	
	/**
	 * this method checks whether a method belongs to the class represented by this instance.
	 * @param methodName	:	the name of a method to check	: {@code String}
	 * @return {@code true} iff the class represented by this instance contains a method with the name represented by {@code methodName}
	 */
	public boolean hasMethod(String methodName) {
		if (methodName==null) throw new IllegalArgumentException("method name is null");
		if (methodName.isEmpty()) throw new IllegalArgumentException("empty method name");
		String preRegExp = "(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+";
		String postRegExp =" *\\([^\\)]*\\) *(\\{?|[^;])";
		String methodDecl = preRegExp + methodName + postRegExp;
		Pattern pattern = Pattern.compile(methodDecl);
		return pattern.matcher(readFile(this.program)).find();
	}
	
	/*
	 * This method is used to read a file and returns the content of the file as a String
	 */
	private static String readFile(File f) {
        String result = null;
        FileReader fr = null;
        try {
                fr = new FileReader(f);
        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        try {
                while ((line = br.readLine()) != null) {
                        result += line;
                }
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        try {
                br.close();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        return result;
}
}