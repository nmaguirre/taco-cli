package verifier;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.multijava.mjc.JCompilationUnitType;

import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.taco.TacoAnalysisResult;
import ar.edu.taco.TacoMain;
import ar.edu.taco.TacoNotImplementedYetException;
import ar.edu.taco.engine.SnapshotStage;
import ar.edu.taco.jml.parser.JmlParser;
import ar.edu.taco.junit.RecoveredInformation;



/**
 * BasicProgramRepairer is a command line application that calls Stryker on a given class and method, and performs the
 * intra statement mutation-based repair, without any pruning.
 * @author Nazareno Aguirre
 * @version 0.4
 */
public class BasicProgramVerifier {
	
	
	/**
	 * jml annotated class to repair
	 */
	private JMLAnnotatedClass subjectClass;
	
	/**
	 * stores the class to repair and all its dependencies (only java classes)
	 */
	private String[] relevantClasses;
	
	/**
	 * method to repair within {@code subjectClass}
	 */
	private String subjectMethod;
	
	/**
	 * Result of the analysis; null before calling verify()
	 */
	private TacoAnalysisResult result = null;
		
	/**
	 * Constructor of class ProgramRepair. It sets the subject of the repair process
	 * with the provided parameter.
	 * @param subjectClass is the class containing the method to be repaired.
	 * @param subjectMethod is the method to be repaired.
	 */
	public BasicProgramVerifier(JMLAnnotatedClass subjectClass, String subjectMethod) {
		if (subjectClass==null) throw new IllegalArgumentException("program is null");
		if (subjectMethod==null) throw new IllegalArgumentException("method is null");
		if (!subjectClass.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subjectClass;
		this.subjectMethod = subjectMethod;
		this.relevantClasses = new String[] {subjectClass.getClassName()};
	}
	
	/**
	 * Constructor of class ProgramVerifier. It sets the subject of the verification process
	 * with the provided parameters.
	 * @param subjectClass	:	the class containing the method to be repaired.						:	{@code JMLAnnotatedClass}
	 * @param subjectMethod :	the method to be verified.											:	{@code String}
	 * @param dependencies	:	the class to verify and all its dependencies (only java classes)	:	{@code String[]}
	 */
	public BasicProgramVerifier(JMLAnnotatedClass subjectClass, String subjectMethod, String[] dependencies) {
		this(subjectClass, subjectMethod);
		String[] mergedDependencies = new String[this.relevantClasses.length + dependencies.length];
		System.arraycopy(this.relevantClasses, 0, mergedDependencies, 0, this.relevantClasses.length);
		System.arraycopy(dependencies, 0, mergedDependencies, this.relevantClasses.length, dependencies.length);
		this.relevantClasses = mergedDependencies;
	}
	
	
	/**
	 * setProgram: it sets the subject of the repair process with the provided parameter.
	 * @param subject is the program that the repair process will be applied to.
	 */
	public void setProgram(JMLAnnotatedClass subject) {
		if (subject==null) throw new IllegalArgumentException("program is null");
		if (!subject.isValid()) throw new IllegalArgumentException("program does not compile");
		this.subjectClass = subject;		
	}
		
	/**
	 * @return the class to verify and all its dependencies (only java classes)
	 */
	public String[] getClassesDependencies() {
		return this.relevantClasses;
	}
	
	/**
	 * Initiates the verification of the subject.
	 * @return true iff the subject is correct wrt to its JML specification, within
	 * provided bounds.
	 */
	public boolean verify() {
		this.result = null; // reset the analysis result, in case verify() was called before
		if (subjectClass==null || subjectMethod==null) throw new IllegalStateException("program or method is null");
		if (!subjectClass.isValid()) throw new IllegalStateException("program does not compile");
						
		if (!subjectClass.isValid()) return false;
		TacoMain taco = new TacoMain(null);
		Properties overridingProperties = new Properties();
		overridingProperties.put("classToCheck",subjectClass.getClassNameAsPath());
		overridingProperties.put("relevantClasses",mergedRelevantClasses());
		overridingProperties.put("methodToCheck", subjectMethod+"_0");
		overridingProperties.put("jmlParser.sourcePathStr", subjectClass.getSourceFolder());
		
		overridingProperties.put("relevancyAnalysis", true);
		overridingProperties.put("checkNullDereference", true);
		overridingProperties.put("useJavaArithmetic", false);
		overridingProperties.put("checkArithmeticException", false);
		overridingProperties.put("inferScope", true);
		overridingProperties.put("objectScope", 3);
		overridingProperties.put("loopUnroll", 3);
		overridingProperties.put("skolemizeInstanceInvariant", true);
		overridingProperties.put("skolemizeInstanceAbstraction", true);
		overridingProperties.put("generateUnitTestCase", true);
		overridingProperties.put("attemptToCorrectBug", false);
		overridingProperties.put("maxStrykerMethodsPerFile", 1);
		overridingProperties.put("removeQuantifiers", true);
		overridingProperties.put("useJavaSBP", false);
		overridingProperties.put("useTightUpperBounds", false);
		if (this.typeScope!=null) {
			overridingProperties.put("typeScopes", this.typeScope);
		}
		
		this.result = null;
		try {
	
			this.result = taco.run("genericTest.properties", overridingProperties);
		}
		catch (TacoNotImplementedYetException e) {
			// candidate is well formed JML but TACO does not support syntax.
			// considering that verification failed, for the moment.
			return false;
		}
		catch (JDynAlloySemanticException e) {
			// candidate is syntactically well formed but JML detects it as 
			// semantically invalid. Considering verification failed.
			return false;
		}
		
		if (result.get_alloy_analysis_result().isSAT()) {
			try {
				PrintWriter writer = new PrintWriter("verification-result.txt", "UTF-8");
				writer.println("VERIFICATION FAILED: Program does not satisfy its contract in the following situation:");
				writer.println(getCounterexample());
				writer.close();
			}
			catch (Exception e) {
				System.out.println("An error has occurred building the verification output.");
			}
		}
		else { 
			try {
				PrintWriter writer = new PrintWriter("verification-result.txt", "UTF-8");
				System.out.println("VERIFICATION SUCCEEDED: No errors found.");
				writer.close();
			}
			catch (Exception e) {
				System.out.println("An error has occurred building the verification output.");
			}

		}
		return result.get_alloy_analysis_result().isUNSAT();
	}
	
	public String getCounterexample() {
		if (this.result == null)
			throw new IllegalStateException("verification must be performed to obtain a counterexample");
		if (this.result.get_alloy_analysis_result().isUNSAT())
			throw new IllegalStateException("verification must fail to obtain a counterexample");
		
		List<JCompilationUnitType> compilation_units = JmlParser.getInstance().getCompilationUnits();
		
		SnapshotStage snapshotStage = new SnapshotStage(compilation_units, result, subjectClass.getClassName(), subjectMethod+"_0");
		
		snapshotStage.execute();			
		RecoveredInformation recoveredInformation = snapshotStage.getRecoveredInformation();
		
		String counterexample = "";
		// first print thiz, if subject method not static
		if (recoveredInformation.getSnapshot().containsKey("thiz_0")) {
			counterexample += "this = " + Dumper.dump(recoveredInformation.getSnapshot().get("thiz_0"), 5, 5, null)+"\n";
		}

		// then print the method arguments and static fields
		for (String k: recoveredInformation.getSnapshot().keySet()) {
			if (k!="thiz_0") {
				counterexample += k.substring(0, k.lastIndexOf("_")) + " = " + recoveredInformation.getSnapshot().get(k) + "\n";
			}
		}
		return counterexample;
		
	}
	
	/**
	 * Sets the scope, provided as a string.
	 * FIXME This is not the best way to pass the scope (we are passing it through too many classes). Let's use
	 * configuration as a singleton.
	 * @param typeScope is the scope to be used in the verification process.
	 */
	public void setScope(String typeScope) {
		this.typeScope = typeScope;
	}
	
	private String typeScope = null;
	
	/**
	 * @return a {@code String} representation of the relevant classes : {@code String}
	 */
	public String mergedRelevantClasses() {
		String mrc = "";
		for (int d = 0; d < this.relevantClasses.length; d++) {
			mrc += this.relevantClasses[d];
			if (d + 1 < this.relevantClasses.length) {
				mrc += ",";
			}
		}
		return mrc;
	}
	


	
}