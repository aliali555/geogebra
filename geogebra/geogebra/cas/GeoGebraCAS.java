package geogebra.cas;

import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoFunction;
import geogebra.kernel.Kernel;
import geogebra.kernel.arithmetic.ExpressionValue;
import geogebra.kernel.arithmetic.Function;
import geogebra.kernel.arithmetic.ValidExpression;
import geogebra.main.Application;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class provides an interface for GeoGebra to use the computer algebra
 * systems Maxima and MathPiper.
 * 
 * @author Markus Hohenwarter
 */
public class GeoGebraCAS {
	
	// determines which CAS is being used
	final public static int CAS_MATHPIPER = 1;
	final public static int CAS_MAXIMA = 2;
	
	public static final int MAXIMA_TIMEOUT = 3;

	private StringBuilder sbPolyCoeffs;
	private Application app;
	private CASparser casParser;
	private CASgeneric cas;
	private CASmathpiper mathpiper;
	private CASmaxima maxima;

	public GeoGebraCAS(Kernel kernel) {
		app = kernel.getApplication();
		casParser = new CASparser(kernel);
		
		//setCurrentCAS(CAS_MAXIMA);
		setCurrentCAS(Application.DEFAULT_CAS);
	}
	
	public CASparser getCASparser() {
		return casParser;
	}
	
	public CASgeneric getCurrentCAS() {
		return cas;
	}
	
	/**
	 * Sets the currently used CAS for evaluateGeoGebraCAS().
	 * @param CAS: use CAS_MATHPIPER or CAS_MAXIMA
	 */
	public void setCurrentCAS(int CAS) {
		try {
			switch (CAS) {
				case CAS_MAXIMA:
					cas = getMaxima();
					break;
				
				default:
					cas = getMathPiper();
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private CASmathpiper getMathPiper() {
		if (mathpiper == null)
			mathpiper = new CASmathpiper(casParser);
		return mathpiper;
	}
	
	private CASmaxima getMaxima() {
		if (maxima == null)
			maxima = new CASmaxima(casParser);
		return maxima;
	}
	
	/**
	 * Returns whether var is a defined variable.
	 */
	public boolean isVariableBound(String var) {
		return cas.isVariableBound(var);
	}
	
	/**
	 * Unbinds (deletes) variable.
	 * @param var
	 * @param isFunction
	 */
	public void unbindVariable(String var) {
		cas.unbindVariable(var);
	}
	
	/** 
	 * Evaluates an expression in GeoGebraCAS syntax with the currently active CAS.
	 * 
     * @return result string (null possible)
	 * @throws Throwable 
     */
	final public String evaluateGeoGebraCAS(String exp) throws Throwable {
		return cas.evaluateGeoGebraCAS(exp, false);
	}
	
	/** 
	 * Evaluates an expression in GeoGebraCAS syntax with the currently active CAS
	 * (MathPiper or Maxima).
     * @return result string (null possible)
     * @param useGeoGebraVariables: whether GeoGebra objects should be substituted before evaluation
	 * @throws Throwable 
     */
	final public String evaluateGeoGebraCAS(String exp, boolean useGeoGebraVariables) throws Throwable {
		return cas.evaluateGeoGebraCAS(exp, useGeoGebraVariables);
	}
	
	/**
	 * Returns the error message of the last call of evaluateGeoGebraCAS().
	 * @return null if last evaluation was successful.
	 */
	final synchronized public String getGeoGebraCASError() {
		return cas.getEvaluateGeoGebraCASerror();
	}
	
	/** 
	 * Evaluates an expression in MathPiper syntax.
     * @return result string (null possible)
     */
	final public String evaluateMathPiper(String exp) {		
		return getMathPiper().evaluateMathPiper(exp);
	}
	
	/** 
	 * Evaluates an expression in Maxima syntax.
     * @return result string (null possible)
     */
	final public String evaluateMaxima(String exp) {
		return getMaxima().evaluateMaxima(exp);
	}
	

	private HashMap getPolynomialCoeffsCache = new HashMap(50);
	private StringBuilder getPolynomialCoeffsSB = new StringBuilder();
	
	/**
	 * Expands the given MathPiper expression and tries to get its polynomial
	 * coefficients. The coefficients are returned in ascending order. If exp is
	 * not a polynomial, null is returned.
	 * 
	 * example: getPolynomialCoeffs("3*a*x^2 + b"); returns ["b", "0", "3*a"]
	 */
	final public String[] getPolynomialCoeffs(String MathPiperExp, String variable) {
		//return ggbJasymca.getPolynomialCoeffs(MathPiperExp, variable);
		
		getPolynomialCoeffsSB.setLength(0);
		getPolynomialCoeffsSB.append(MathPiperExp);
		getPolynomialCoeffsSB.append(':');
		getPolynomialCoeffsSB.append(variable);
		
		String result = (String)(getPolynomialCoeffsCache.get(getPolynomialCoeffsSB.toString()));
		
		if (result != null) {
			//Application.debug("using cached result: "+result);
			// remove { } to get "b, 0, 3*a"
			result = result.substring(1, result.length()-1);
			
			// split to get coefficients array ["b", "0", "3*a"]
			String [] coeffs = result.split(",");				    
	        return coeffs;	
		}
		
		
		if (sbPolyCoeffs == null)
			sbPolyCoeffs = new StringBuilder();
		else
			sbPolyCoeffs.setLength(0);
		
		
		/* replaced Michael Borcherds 2009-02-08
		 * doesn't seem to work properly polyCoeffsbug.ggb
		 */
		sbPolyCoeffs.append("getPolynomialCoeffs(");
		sbPolyCoeffs.append(MathPiperExp);
		sbPolyCoeffs.append(',');
		sbPolyCoeffs.append(variable);
		sbPolyCoeffs.append(')');
		

		// Expand expression and get polynomial coefficients using MathPiper:
		// Prog( Local(exp), 
		//   	 exp := ExpandBrackets( 3*a*x^2 + b ), 
		//		 Coef(exp, x, 0 .. Degree(exp, x)) 
		// )		
		//sbPolyCoeffs.append("Prog( Local(exp), exp := ExpandBrackets(");
		//sbPolyCoeffs.append(MathPiperExp);
		//sbPolyCoeffs.append("), Coef(exp, x, 0 .. Degree(exp, x)))");
			
		try {
			// expand expression and get coefficients of
			// "3*a*x^2 + b" in form "{ b, 0, 3*a }" 
			result = evaluateMathPiper(sbPolyCoeffs.toString());
			
			// empty list of coefficients -> return null
			if ("{}".equals(result) || "".equals(result) || result == null) 
				return null;
			
			// cache result
			//Application.debug("caching result: "+result);		
			getPolynomialCoeffsCache.put(getPolynomialCoeffsSB.toString(), result);

			//Application.debug(sbPolyCoeffs+"");
			//Application.debug(result+"");
			
			// remove { } to get "b, 0, 3*a"
			result = result.substring(1, result.length()-1);
			
			// split to get coefficients array ["b", "0", "3*a"]
			String [] coeffs = result.split(",");				    
            return coeffs;						
		} 
		catch(Throwable e) {
			Application.debug("GeoGebraCAS.getPolynomialCoeffs(): " + e.getMessage());
			//e.printStackTrace();
		}
		
		return null;
	}


	

	
	/**
	 * Tries to convert an expression in GeoGebra syntax into a LaTeX string.
	 * 
	 * @return null if something went wrong or the resulting String doesn't contain
	 * any LaTeX commands (i.e. no \).
	 */
	public synchronized String convertGeoGebraToLaTeXString(String ggbExp) {
		if (ggbExp == null)
			return null;
		
		try {
			// parse input
			ValidExpression ve = casParser.parseGeoGebraCASInput(ggbExp);
			String latex = ve.toLaTeXString(true);
						
			for (int i=0; i < latex.length(); i++) {
				char ch = latex.charAt(i);
				switch (ch) {
					case '\\':
					case '^':
						return latex;
				}
			}
		} catch (Throwable e) {
			//e.printStackTrace();			
		}		
		
		// no real latex string: return null
		return null;
	}
	
	/**
	 * Returns a function from GeoGebra as a MathPiper string. For example f(x) = a x^2 is
	 * returned as "f(x) := a * x^2"
	 */
	public String toCASString(GeoElement geo) {
		if (!geo.isDefined()) return null;
		
		StringBuilder sb = new StringBuilder();

		// function, e.g. f(x) := 2*x
		if (geo.isGeoFunction()) {
			Function fun = ((GeoFunction)geo).getFunction();
			sb.append(geo.getLabel());
			sb.append("(" );
			sb.append(fun.getFunctionVariable());
			sb.append(") := ");
			sb.append(fun.getExpression().getCASstring(true));
		}
		else {
			sb.append(geo.getLabel());
			sb.append(" := ");
			sb.append(geo.toValueString());
		}

		return sb.toString();
	}
	
	
	
	/**
	 * Returns the CAS command for the currently set CAS using the given key and command arguments. 
	 * For example, getCASCommand("Expand.0", {"3*(a+b)"}) returns "ExpandBrackets( 3*(a+b) )" when
	 * MathPiper is the currently used CAS.
	 */
	final synchronized public String getCASCommand(String name, ArrayList args, boolean symbolic) {
		StringBuilder sbCASCommand = new StringBuilder(80);
				
		// build command key as name + "." + args.size()
		sbCASCommand.setLength(0);
		sbCASCommand.append(name);
		sbCASCommand.append('.');
		sbCASCommand.append(args.size());
		
		// get translation ggb -> MathPiper/Maxima
		String translation = cas.getTranslatedCASCommand(sbCASCommand.toString());
		sbCASCommand.setLength(0);		
		
		// no translation found: 
		// use key as command name
		if (translation == null) {			
			sbCASCommand.append(name);
			sbCASCommand.append('(');
			for (int i=0; i < args.size(); i++) {
				ExpressionValue ev = (ExpressionValue) args.get(i);				
				if (symbolic)
					sbCASCommand.append(ev.toString());
				else
					sbCASCommand.append(ev.toValueString());
				sbCASCommand.append(',');
			}
			sbCASCommand.setCharAt(sbCASCommand.length()-1, ')');
		}
		
		// translation found: 
		// replace %0, %1, etc. in translation by command arguments
		else {
			for (int i = 0; i < translation.length(); i++) {
				char ch = translation.charAt(i);
				if (ch == '%') {
					// get number after %
					i++;
					int pos = translation.charAt(i) - '0';
					if (pos >= 0 && pos < args.size()) {
						// success: insert argument(pos)
						ExpressionValue ev = (ExpressionValue) args.get(pos);				
						if (symbolic)
							sbCASCommand.append(ev.toString());
						else
							sbCASCommand.append(ev.toValueString());
					} else {
						// failed
						sbCASCommand.append(ch);
					}
				} else {
					sbCASCommand.append(ch);
				}
			}
		}

		return sbCASCommand.toString();
	}
	
	
	
	
	
	/**
	 * Returns true if the two input expressions are structurally equal. 
	 * For example "2 + 2/3" is structurally equal to "2 + (2/3)"
	 * but unequal to "(2 + 2)/3"
	 */
	public boolean isStructurallyEqual(String input1, String input2) {
		try {
			// parse both input expressions
			ValidExpression ve1 = casParser.parseGeoGebraCASInput(input1);
			ValidExpression ve2 = casParser.parseGeoGebraCASInput(input2);
			
			// compare if the parsed expressions are equal
			return ve1.toString().equals(ve2.toString());
		} catch (Throwable th) {
		}
		
		return false;
	}
	
}