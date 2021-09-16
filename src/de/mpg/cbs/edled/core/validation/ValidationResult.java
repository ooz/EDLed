package de.mpg.cbs.edled.core.validation;

import java.util.LinkedList;
import java.util.List;

public class ValidationResult {
	
	private final boolean valueHasCorrectType;
	private final List<EDLRule> relevantRules;
	private List<EDLRule> violatedRules;
	
	public ValidationResult(final boolean valid) {
		this.valueHasCorrectType = valid;
		this.relevantRules = new LinkedList<EDLRule>();
		this.violatedRules = new LinkedList<EDLRule>();
	}
	
	public ValidationResult(final boolean correctType, 
			                final List<EDLRule> relevantRules) {
		this.valueHasCorrectType = correctType;
		
		if (relevantRules == null) {
			this.relevantRules = new LinkedList<EDLRule>();
		} else {
			this.relevantRules = relevantRules;
		}
		
		this.violatedRules = new LinkedList<EDLRule>();
	}
	
	public boolean isValid() {
		this.violatedRules = new LinkedList<EDLRule>();
		
		if (this.valueHasCorrectType) {
			for (EDLRule rule : this.relevantRules) {
				if (!rule.isSatisfied()) {
					this.violatedRules.add(rule);
				}
			}
			
			if (this.violatedRules.isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<EDLRule> getViolatedRules() {
		return this.violatedRules;
	}
}
