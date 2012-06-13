package de.mpg.cbs.edled.core.metatree;

import com.sun.xml.xsom.XSParticle;

public class NodeConstraint {
	
	public enum AttributeUse {
		OPTIONAL,
		REQUIRED,
		PROHIBITED;
	}
	
	public static final int DEFAULT_OCCURS = 1;
	public static final int UNBOUNDED = -1;
	
	// -1 .. unspecified
	//  0 .. false/no
	//  1 .. true/yes
	private int canHaveTextContent = -1;
	private String typeName = null;
	private TypeRestriction typeRestriction = null;
	
	private int maxOccurs = NodeConstraint.DEFAULT_OCCURS;
	private int minOccurs = NodeConstraint.DEFAULT_OCCURS;
	private String defaultValue = null;
	private String fixedValue = null;
	private AttributeUse use = null;
    private String appInfo = null;
	
	public NodeConstraint() {
//		NodeConstraint.occurences.put(this, new MutableInteger(this.minOccurs));
	}
	
//	NodeConstraint(final Node node, final NodeConstraint baseConstraint, final List<Node> choicePoolNodes) {
//		
//		this.baseType = baseConstraint.getBaseType();
//		this.typeRestriction = baseConstraint.getTypeRestriction();
//		this.maxOccurs = baseConstraint.getMaxOccurs();
//		this.minOccurs = baseConstraint.getMinOccurs();
//		this.defaultValue = baseConstraint.getDefaultValue();
//		this.fixedValue = baseConstraint.getFixedValue();
//		this.choicePoolNodes = choicePoolNodes;
//	}
	
//	@Override
//	protected void finalize() throws Throwable {
//		NodeConstraint.occurences.remove(this);
//	}
//	
//	public Node getAssociatedNode() {
//		return this.node;
//	}
	
	public NodeConstraint initCanHaveTextContent(final boolean flag) {
		if (this.canHaveTextContent == -1) {
			if (flag) {
				this.canHaveTextContent = 1;
			} else {
				this.canHaveTextContent = 0;
			}
		}
		
		return this;
	}
	public boolean canHaveTextContent() {
		if (this.canHaveTextContent == 1) {
			return true;
		} 

		return false;
	}
//	public boolean validateTextContent(final String text) {
//		if (!this.hasBaseType()) {
//			return false;
//		}
//		
////		TypeValidator validator = NodeConstraint.baseTypeValidators.get(this.baseType);
//		return false;
//	}
	
	public NodeConstraint initTypeName(final String typeName) {
		if (this.typeName == null) {
			this.typeName = typeName;
		}
		
		return this;
	}
	public boolean hasTypeName() {
		if (this.typeName == null) {
			return false;
		}
		
		return true;
	}
	public String getTypeName() {
		return this.typeName;
	}
	
	public NodeConstraint initTypeRestriction(final TypeRestriction typeRestriction) {
		if (this.typeRestriction == null) {
			this.typeRestriction = typeRestriction;
		}
		
		return this;
	}
	public boolean hasTypeRestriction() {
		if (this.typeRestriction == null) {
			return false;
		} else {
			return true;
		}
	}
	public TypeRestriction getTypeRestriction() {
		return this.typeRestriction;
	}
	
	public NodeConstraint initMaxOccurs(final int maxOccurs) {
		if (this.maxOccurs == NodeConstraint.DEFAULT_OCCURS) {
			this.maxOccurs = maxOccurs;
		}
		
		return this;
	}
	public int getMaxOccurs() {
		return this.maxOccurs;
	}
	
	public NodeConstraint initMinOccurs(final int minOccurs) {
		if (this.minOccurs == NodeConstraint.DEFAULT_OCCURS) {
			this.minOccurs = minOccurs;
		}
		
		return this;
	}
	public int getMinOccurs() {
		return this.minOccurs;
	}
	
	public NodeConstraint initDefaultValue(final String defaultValue) {
		if (this.defaultValue == null) {
			this.defaultValue = defaultValue;
		}
		
		return this;
	}
	public boolean hasDefaultValue() {
		if (this.defaultValue == null) {
			return false;
		} else {
			return true;
		}
	}
	public String getDefaultValue() {
		return this.defaultValue;
	}
	
	public NodeConstraint initFixedValue(final String fixedValue) {
		if (this.fixedValue == null) {
			this.fixedValue = fixedValue;
		}
	
		return this;
	}
	public boolean hasFixedValue() {
		if (this.fixedValue == null) {
			return false;
		} else {
			return true;
		}
	}
	public String getFixedValue() {
		return this.fixedValue;
	}
	
//	public NodeConstraint initChoicePoolNodes(final List<Node> choices) {
//		if (this.choicePoolNodes == null) {
//			this.choicePoolNodes = choices;
//		}
//		
//		return this;
//	}
//	public boolean hasChoices() {
//		if (this.choicePoolNodes == null) {
//			return false;
//		} else {
//			return true;
//		}
//	}
//	public List<Node> getNodesInChoicePool() {
//		return this.choicePoolNodes;
//	}
	
	
	public NodeConstraint initAttributeUse(AttributeUse use) {
		if (this.use == null) {
			this.use = use;
		}
		
		return this;
	}
	public boolean hasAttributeUse() {
		if (this.use != null) {
			return true;
		}
		
		return false;
	}
	public AttributeUse getAttributeUse() {
		return this.use;
	}
	
	public NodeConstraint initAppInfo(String appInfo) {
		if (this.appInfo == null) {
			this.appInfo = appInfo;
		}
		
		return this;
	}
	public boolean hasAppInfo() {
		if (this.appInfo != null) {
			return true;
		}
		
		return false;
	}
	public String getAppInfo() {
		return this.appInfo;
	}
	
	@Override
	public String toString() {
		
		String maxOccursString;
		if (this.maxOccurs == XSParticle.UNBOUNDED) {
			maxOccursString = "unbounded";
		} else {
			maxOccursString = new Integer(this.maxOccurs).toString();
		}
		
		return "NodeConstraint{baseType=" + this.typeName 
            + ", typeRestriction=" + this.typeRestriction
            + ", minOccurs=" + this.minOccurs
            + ", maxOccurs=" + maxOccursString
             + "}";
	}
}
