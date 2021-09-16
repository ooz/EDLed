package de.mpg.cbs.edled.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XSOMParser;

import de.mpg.cbs.edled.core.metatree.MetaAttr;
import de.mpg.cbs.edled.core.metatree.MetaChoice;
import de.mpg.cbs.edled.core.metatree.MetaElem;
import de.mpg.cbs.edled.core.metatree.MetaNode;
import de.mpg.cbs.edled.core.metatree.MetaSequence;
import de.mpg.cbs.edled.core.metatree.MetaTreeBuilder;
import de.mpg.cbs.edled.core.metatree.NodeConstraint;
import de.mpg.cbs.edled.core.metatree.TypeRestriction;


/**
 * MetaTreeBuilder using the XSOM framework.
 * 
 * @author Oliver Z.
 */
public class XSOMMetaTreeBuilder implements MetaTreeBuilder {
	
	/** All XML schemas the builder got initialized with. */
	XSSchemaSet schemaset = null;
	
	/** 
	 * Initializes the MetaTreeBuilder with a XSD. 
	 *
	 * @param file The XSD file.
	 */
	public XSOMMetaTreeBuilder(final File xmlSchema) {
		loadSchemaFrom(xmlSchema);
	}

	/**
	 * Parses a XSD file.
	 * 
	 * @param file XML schema file.
	 * @return     Boolean indicating whether the parsing was successful or not.
	 */
	private boolean loadSchemaFrom(final File file) {
		
		XSOMParser parser = new XSOMParser();
		
		parser.setAnnotationParser(new AnnotationParserFactory() {
			
			@Override
			public AnnotationParser create() {
				return new AnnotationParser() {
					
					final StringBuffer buffer = new StringBuffer();
					
					@Override
					public Object getResult(Object existing) {
						return this.buffer.toString().trim();
					}
					
					@Override
					public ContentHandler getContentHandler(AnnotationContext context,
															String parentElementName, 
															ErrorHandler errorHandler, 
															EntityResolver entityResolver) {
						return new ContentHandler() {
							
							@Override
							public void startPrefixMapping(String prefix, String uri)
									throws SAXException {
							}
							
							@Override
							public void startElement(String uri, 
													 String localName, 
													 String name,
													 Attributes arg3) throws SAXException {
							}
							
							@Override
							public void startDocument() throws SAXException {
							}
							
							@Override
							public void skippedEntity(String name) throws SAXException {
							}
							
							@Override
							public void setDocumentLocator(Locator locator) {
							}
							
							@Override
							public void processingInstruction(String target, String data)
									throws SAXException {
							}
							
							@Override
							public void ignorableWhitespace(char[] str, int start, int length)
									throws SAXException {
							}
							
							@Override
							public void endPrefixMapping(String prefix) throws SAXException {
							}
							
							@Override
							public void endElement(String uri, String localName, String name)
									throws SAXException {
							}
							
							@Override
							public void endDocument() throws SAXException {
							}
							
							@Override
							public void characters(char[] str, int start, int length) throws SAXException {
								buffer.append(str, start, length);
							}
						};
					}
				};
			}
		});
		
		try {
			parser.parse(file);
			this.schemaset = parser.getResult();
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public MetaNode buildMetaXMLTree(final String elemName) {
		MetaElem documentElem = new MetaElem(elemName, new NodeConstraint());
		this.buildXMLtreeStartingAt(documentElem, null, null);
		return documentElem;
	}
	
	private void buildXMLtreeStartingAt(final MetaElem elem, XSElementDecl elemDecl, XSType elemType) {
		XSAnnotation annotation = null;
		if (elemType == null) {
			elemDecl = this.schemaset.getElementDecl("", elem.getName());
			annotation = elemDecl.getAnnotation();
			elemType = elemDecl.getType();
		}
		
		// Redefinition of type.
//		while (elemType.getRedefinedBy() != null) {
//			elemType = elemType.getRedefinedBy();
//		}
		
		NodeConstraint elemConstraint = elem.getConstraint();
		if (annotation != null) {
			elemConstraint.initAppInfo((String) annotation.getAnnotation());
		}
		
		String typeName = elemType.getName();
		if (typeName == null) {
			typeName = elemType.getBaseType().getName();
		}
		elemConstraint.initTypeName(typeName);
		
		if (elemType.isComplexType()) {
			XSContentType contentType = elemType.asComplexType().getContentType();

			XSParticle elemParticle = contentType.asParticle();
			if (elemParticle != null) {
				XSTerm term = elemParticle.getTerm();
				if (term.isModelGroup()) {
					XSModelGroup modelgroup = term.asModelGroup();
					this.buildChildElementsWithModelgroup(elem, modelgroup);
				}
			}
			
			XSSimpleType elemSimpleType = contentType.asSimpleType();
			if (elemSimpleType != null) {
				elemConstraint.initCanHaveTextContent(true);
				
				if (elemSimpleType.isRestriction()) {
					elemConstraint.initTypeRestriction(buildTypeRestriction(typeName, elemSimpleType.asRestriction()));
				}
			}
			
			this.buildAttributesFor(elem, elemType.asComplexType());
			
		} else if (elemType.isSimpleType()) {
			
			XSSimpleType elemSimpleType = elemType.asSimpleType();
			elemConstraint.initCanHaveTextContent(true);
			if (elemDecl.getDefaultValue() != null) {
				elemConstraint.initDefaultValue(elemDecl.getDefaultValue().value);
			}
			if (elemDecl.getFixedValue() != null) {
				elemConstraint.initFixedValue(elemDecl.getFixedValue().value);
			}
			
			if (elemSimpleType.isRestriction()) {
				elemConstraint.initTypeRestriction(buildTypeRestriction(typeName, elemSimpleType.asRestriction()));
			}
		}
	}
	
	private void buildChildElementsWithModelgroup(final MetaNode node, 
												  final XSModelGroup modelGroup) {
		
		XSParticle[] particles = modelGroup.getChildren();
		
		Compositor modelGroupKind = modelGroup.getCompositor();
		MetaNode metaModelGroup = null;
		// TODO: Investigate in "group compositor"
		switch(modelGroupKind) {
		case ALL:
			metaModelGroup = new MetaSequence(new NodeConstraint()); // TODO: define MetaXMLAll 
			break;
		case SEQUENCE:
			metaModelGroup = new MetaSequence(new NodeConstraint()); // TODO: get modelgroup multiplicty and put in constraint
			break;
		case CHOICE:
			metaModelGroup = new MetaChoice(new NodeConstraint());
			break;
		default:
			return; // TODO: Error&explosions.
		}
		
		node.add(metaModelGroup);
		
		for (XSParticle childParticle : particles) {
			XSTerm childTerm = childParticle.getTerm();
			if (childTerm.isElementDecl()) {
				try {
					XSElementDecl childElemDecl = childTerm.asElementDecl();
					NodeConstraint childElemConstraint = new NodeConstraint();
					if (childElemDecl.getAnnotation() != null) {
						childElemConstraint.initAppInfo((String) childElemDecl.getAnnotation().getAnnotation());
					}
					childElemConstraint.initMaxOccurs(childParticle.getMaxOccurs());
					childElemConstraint.initMinOccurs(childParticle.getMinOccurs());
					MetaElem childElem = new MetaElem(childTerm.asElementDecl().getName(), 
															childElemConstraint);
					metaModelGroup.add(childElem);
					
					this.buildXMLtreeStartingAt(childElem, childElemDecl, childElemDecl.getType());
				} catch (Exception e) {
					
				}
			} else if (childTerm.isModelGroup()) {
				this.buildChildElementsWithModelgroup(metaModelGroup, childTerm.asModelGroup());
			}
		}
	}
	
	private void buildAttributesFor(final MetaElem elem,
				                    final XSComplexType elemType) {
		
		Collection<? extends XSAttributeUse> attributes = elemType.getAttributeUses();
		Iterator<? extends XSAttributeUse> attributesIterator = attributes.iterator();
		
		while (attributesIterator.hasNext()) {
			XSAttributeUse attributeUse = attributesIterator.next();
			XSAttributeDecl attributeDecl = attributeUse.getDecl();
			
			NodeConstraint attributeConstraint = new NodeConstraint();
			attributeConstraint.initCanHaveTextContent(true);
			MetaAttr attribute = new MetaAttr(attributeDecl.getName(), attributeConstraint);
			elem.add(attribute);
			
			if (attributeUse.isRequired()) {
				attributeConstraint.initAttributeUse(NodeConstraint.AttributeUse.REQUIRED);
			} else {
				attributeConstraint.initAttributeUse(NodeConstraint.AttributeUse.OPTIONAL);
			}
			
			if (attributeDecl.getDefaultValue() != null) {
				attributeConstraint.initDefaultValue(attributeDecl.getDefaultValue().value);
			}
			if (attributeDecl.getFixedValue() != null) {
				attributeConstraint.initFixedValue(attributeDecl.getFixedValue().value);
			}
			
			XSSimpleType attributeType = attributeDecl.getType();
			
			String typeName = attributeType.getName();
			if (typeName == null) {
				typeName = attributeType.getBaseType().getName();
			}
			attributeConstraint.initTypeName(typeName);
			
			if (attributeType.isRestriction()) {
				attributeConstraint.initTypeRestriction(buildTypeRestriction(typeName, attributeType.asRestriction()));
			}
		}
	}
	
	private TypeRestriction buildTypeRestriction(final String baseTypeName, 
												 final XSRestrictionSimpleType xsRestriction) {
		
		TypeRestriction typeRestriction = new TypeRestriction(baseTypeName);
    	Iterator<? extends XSFacet> iterator = xsRestriction.getDeclaredFacets().iterator();
    	while (iterator.hasNext()) {
    		XSFacet facet = iterator.next();
    		String facetValue = facet.getValue().value;
    		if (facet.getName().compareTo(XSFacet.FACET_ENUMERATION) == 0) {
    			typeRestriction.addEnumerationValue(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_TOTALDIGITS) == 0) {
    			typeRestriction.initTotalDigits(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_FRACTIONDIGITS) == 0) {
    			typeRestriction.initFractionDigits(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_MAXEXCLUSIVE) == 0) {
    			typeRestriction.initMaxExclusive(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_MAXINCLUSIVE) == 0) {
    			typeRestriction.initMaxInclusive(facetValue);    			
    		} else if (facet.getName().compareTo(XSFacet.FACET_MINEXCLUSIVE) == 0) {
    			typeRestriction.initMinExclusive(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_MININCLUSIVE) == 0) {
    			typeRestriction.initMinInclusive(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_LENGTH) == 0) {
    			typeRestriction.initLength(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_MAXLENGTH) == 0) {
    			typeRestriction.initMaxLength(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_MINLENGTH) == 0) {
    			typeRestriction.initMinLength(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_PATTERN) == 0) {
    			typeRestriction.initPattern(facetValue);
    		} else if (facet.getName().compareTo(XSFacet.FACET_WHITESPACE) == 0) {
    			typeRestriction.initWhitespace(facetValue);
    		}
    	}
		
		return typeRestriction;
	}
	
}
