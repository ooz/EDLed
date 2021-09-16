package de.mpg.cbs.edled.core.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.mpg.cbs.edled.core.validation.EDLRuleParameter.ParameterValue;
import de.mpg.cbs.edled.xml.XMLUtility;


public class EDLRule extends Observable {
	
	private final String id;
	private final Map<String, EDLRuleParameter> parameters;
	private final List<EDLRuleLiteral> premise;
	private final List<EDLRuleLiteral> conclusion;
	private final String message;
	
	private boolean value;
	
	public EDLRule(final String id,
				   final Map<String, EDLRuleParameter> parameters,
				   final List<EDLRuleLiteral> premise,
				   final List<EDLRuleLiteral> conclusion,
				   final String message) {
		this.id = id;
		this.parameters = parameters;
		this.premise = premise;
		this.conclusion = conclusion;
		this.message = message;
		
		this.value = false;
	}
	
	public String getID() {
		return this.id;
	}
	
	public Map<String, EDLRuleParameter> getParameters() {
		return this.parameters;
	}
	public int getParameterCount() {
		return this.parameters.size();
	}
	
	public List<EDLRuleLiteral> getPremise() {
		return this.premise;
	}
	
	public List<EDLRuleLiteral> getConclusion() {
		return this.conclusion;
	}
	
	public String getMessage() {
		return this.message;
	}
	
//	public boolean isApplicable(final Document xmlDocument) {
//		return valueOfConjunctedLiterals(this.premise, xmlDocument);
//	}
	
	public List<Node> evaluate(final Document xmlDocument) {
		ParameterEvaluationResult paramEvalRslt = resolveParameters(xmlDocument);
		
		if (valueOfConjunctedLiterals(this.premise, paramEvalRslt.evaluatedParameters)) {
			this.value = valueOfConjunctedLiterals(this.conclusion, paramEvalRslt.evaluatedParameters);
		} else {
			this.value = true;
		}
		
		if (this.value) {
			setChanged();
			notifyObservers(this);
			deleteObservers();
		}
		
		return paramEvalRslt.referencedNodes;
	}
	
	public boolean isSatisfied() {
		return this.value;
	}
	
	private ParameterEvaluationResult resolveParameters(final Document xmlDocument) {
		
		List<Node> referencedNodes = new LinkedList<Node>();
		Map<String, String> evaluatedParameters = new HashMap<String, String>();

		for (String paramName : this.parameters.keySet()) {
				
			EDLRuleParameter param = this.parameters.get(paramName);
			ParameterValue paramValue = param.evaluate(xmlDocument);
			
			if (!paramValue.isNull()) {
				Node referencedNode = paramValue.getNodeList().item(0);
				referencedNodes.add(referencedNode);
				
				if (paramValue.isNodeList()) {
					evaluatedParameters.put(paramName, "");
				} else if (paramValue.isNodeValue()) {
					evaluatedParameters.put(paramName, XMLUtility.getNodeValue(referencedNode));
				}
			}
		}
		
		return new ParameterEvaluationResult(referencedNodes, evaluatedParameters);
	}
	
	private boolean valueOfConjunctedLiterals(final List<EDLRuleLiteral> literals, 
											  final Map<String, String> evaluatedParameters) {
		boolean conjunctedValue = true;
		
		for (EDLRuleLiteral literal : literals) {
			switch (literal.evaluate(evaluatedParameters)) {
			case TRUE:
				break;
			case FALSE:
			case ERROR:
				conjunctedValue = false;
				// TODO: handle error & differentiate to FALSE-case
				break;
			default:
				break;
			}
		}
		
		return conjunctedValue;
	}
	
	@Override
	public String toString() {
		return "EDLRule{id=" + this.id + ", msg=" + this.message + "}";
	}
	
	private class ParameterEvaluationResult {
		
		final List<Node> referencedNodes;
		final Map<String, String> evaluatedParameters;
		
		ParameterEvaluationResult(final List<Node> referencedNodes,
								  final Map<String, String> evaluatedParameters) {
			this.referencedNodes = referencedNodes;
			this.evaluatedParameters = evaluatedParameters;
		}
	}
	
}
