package de.mpg.cbs.edled.core.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class EDLRuleLiteral {
	
	public enum LiteralValue {
		ERROR,
		FALSE,
		TRUE;
	}
	
	private final String literal;
	private LiteralValue value;
	
	private List<LiteralToken> tokens;
	private List<LiteralToken> tokensCopy;
	
	public EDLRuleLiteral(final String literal) {
		this.literal = literal;
		this.value = null;
		
		this.tokens = null;
		this.tokensCopy = null;
	}
	
	public LiteralValue evaluate(final Map<String, String> parameters) {
		
		this.value = null;
		
		// Tokenize if necessary.
		if (this.tokens == null) {
			this.tokens = new LinkedList<LiteralToken>();
			int position = 0;
			while (position < this.literal.length() && this.value != LiteralValue.ERROR) {
				position = tokenize(position);
			}
		}
		
		// Resolve parameters and evaluate tokens.
		this.tokensCopy = new LinkedList<LiteralToken>(this.tokens);
		if (this.tokensCopy.size() > 0 /* && this.value != LiteralValue.ERROR */) {
			resolveParameters(parameters);
			
			Range evalRange = new Range(0, this.tokensCopy.size());
			evaluateTokensOver(evalRange);
		} else {
			this.value = LiteralValue.ERROR;
		}
		
		// Get value from last token that is left after evaluation.
		if (this.tokensCopy.size() == 1 && this.value != LiteralValue.ERROR) {
			LiteralToken resultToken = this.tokensCopy.get(0);
			if (resultToken.kind == LiteralTokenKind.BOOLEAN_TOKEN) {
				if (resultToken.value.compareTo("TRUE") == 0) {
					this.value = LiteralValue.TRUE;
				} else {
					this.value = LiteralValue.FALSE;
				}
			} else {
				this.value = LiteralValue.ERROR;
			}
		} else {
			this.value = LiteralValue.ERROR;
		}
		
		return this.value;
	}
	
	private int tokenize(final int currentIndex) {
		int position = currentIndex;
		char c = this.literal.charAt(position);
		
		if (isBeginOfSymbol(c)) {
			position = parseSymbol(position);
	        position++;
		} else if (isBeginOfWord(c)) {
			position = parseWord(position);
		} else if (isBeginOfString(c)) {
			position = parseString(position);
		} else if (isDigit(c)) {
			position = parseNumber(position);
		} else {
			position++;
		}
		
		return position;
	}
	private boolean isBeginOfSymbol(final char c) {
		switch (c) {
		case '+':
		case '-':
		case '/':
		case '*':
		case '(':
		case ')':
		case ',':
		case '=':
			return true;
		default:
			return false;
		}
	}
	private boolean isBeginOfWord(final char c) {
		// Character c matches [A-Za-z]
		if ((c >= 65 && c <= 90)
			|| (c >= 97 && c <= 122)) {
			return true;
		}
		
		return false;
	}
	private boolean isBeginOfString(final char c) {
		// Character c is '
		if (c == 39) {
			return true;
		}
		
		return false;
	}
	private boolean isDigit(final char c) {
		// Character c matches [0-9]
		if (c >= 48 && c <= 57) {
			return true;
		}
		
		return false;
	}
	
	private int parseSymbol(final int currentIndex) {
		int position = currentIndex;
		LiteralToken token = null;
		
		switch (this.literal.charAt(position)) {
		case '+':
		case '-':
		case '/':
		case '*':
		case '(':
		case ')':
		case ',':
			token = new LiteralToken(LiteralTokenKind.SYMBOL_TOKEN, 
									 Character.toString(this.literal.charAt(position)));
			break;
		case '=':
			try {
				if ('=' == this.literal.charAt(position + 1)) {
					token = new LiteralToken(LiteralTokenKind.SYMBOL_TOKEN, "==");
					position++;
				} else {
					this.value = LiteralValue.ERROR;
				}
			} catch (IndexOutOfBoundsException e) {
				this.value = LiteralValue.ERROR;
			}
			break;
		default:
			this.value = LiteralValue.ERROR;
			break;
		}
		
		this.tokens.add(token);
		return position;
	}
	private int parseWord(final int currentIndex) {
		int position = currentIndex;
		
		StringBuffer buffer = new StringBuffer();
		char c = this.literal.charAt(position);
		
		/* While cur is in the boundaries of literalString and the
	     * character is either a letter, a number or the underscore
	     * fill buffer.                                             */
		while ((isDigit(c)
				|| isBeginOfWord(c)
				|| c == '_')
				&& (position < this.literal.length())) {
			buffer.append(c);
			position++;
			
			if (position < this.literal.length()) {
				c = this.literal.charAt(position);
			}
		}
		
		this.tokens.add(new LiteralToken(LiteralTokenKind.WORD_TOKEN, buffer.toString()));
		
		return position;
	}
	private int parseString(final int currentIndex) {
		int position = currentIndex;
		
		if (position >= this.literal.length()) {
			this.value = LiteralValue.ERROR;
			return position;
		}
		
		position++; // Skip string beginning.
		
		StringBuffer buffer = new StringBuffer();
		char c;
		boolean stringEndFound = false;
		
		while (!stringEndFound
			   && (position < this.literal.length())
			   && this.value != LiteralValue.ERROR) {
			
			c = this.literal.charAt(position);
			
			/* Check for an escaped ' (string terminator), other escapes are copied
	         * character by character. 												*/
			if (c == '\\') {
				try {
					c = this.literal.charAt(position + 1);
					
					if (c == '\'') {
						buffer.append("'");
					} else {
						buffer.append("\\");
						buffer.append(c);
					}
					
					position++;
					
				} catch (IndexOutOfBoundsException e) {
					this.value = LiteralValue.ERROR;
				}
			} else if (c == '\'') {
				// End of string...
				stringEndFound = true;
			} else {
				buffer.append(c);
			}
			
			position++;
		}
		
		if (!stringEndFound || this.value == LiteralValue.ERROR) {
			this.value = LiteralValue.ERROR;
		} else {
			this.tokens.add(new LiteralToken(LiteralTokenKind.STRING_TOKEN, buffer.toString()));
		}
		
		return position;
	}
	/**
	 *  Numbers have to match the reg. expr.: [0-9]{1,*}[[\.[0-9]{1,*}]?    
	 *                                 e.g.: 2, 90.02, 100.100, 5.0 
	 */
	private int parseNumber(final int currentIndex) {
		int position = currentIndex;
		
		StringBuffer buffer = new StringBuffer();
		char c = this.literal.charAt(position);
		
		// Pre decimal point or normal integer.
		while (isDigit(c)
			   && (position < this.literal.length())) {
			
			buffer.append(c);
			position++;
			
			if (position < this.literal.length()) {
				c = this.literal.charAt(position);
			}
		}
		
		// Post decimal point.
		if (c == '.') {
			if (position + 1 < this.literal.length()) {
				buffer.append(".");
				position++;
				c = this.literal.charAt(position);
				
				if (isDigit(c)) {
					while (isDigit(c)
						   && (position < this.literal.length())) {
						buffer.append(c);
						position++;
						
						if (position < this.literal.length()) {
							c = this.literal.charAt(position);
						}
					}
				} else {
					this.value = LiteralValue.ERROR;
				}
			} else {
				this.value = LiteralValue.ERROR;
			}
		}
		
		if (this.value != LiteralValue.ERROR) {
			this.tokens.add(new LiteralToken(LiteralTokenKind.NUMBER_TOKEN, buffer.toString()));
		}
		
		return position;
	}
	
	private void resolveParameters(final Map<String, String> parameters) {
		
		List<LiteralToken> wordTokensToRemove = new LinkedList<LiteralToken>();
		Map<LiteralToken, LiteralToken> wordTokensToReplace = new HashMap<LiteralToken, LiteralToken>();
		
		for (LiteralToken token : this.tokensCopy) {
			if (token.kind == LiteralTokenKind.WORD_TOKEN
				 && !isPredefinedEDLFunction(token.value)) {
				
				String paramValue = parameters.get(token.value);
				
				if (paramValue == null) {
					wordTokensToRemove.add(token);
				} else {
					wordTokensToReplace.put(token, new LiteralToken(LiteralTokenKind.PARAMETER_TOKEN, paramValue));
				}
			}
		}
		
		this.tokensCopy.removeAll(wordTokensToRemove);
		for (LiteralToken toReplace : wordTokensToReplace.keySet()) {
			int toReplaceIndex = this.tokensCopy.indexOf(toReplace);
			this.tokensCopy.set(toReplaceIndex, wordTokensToReplace.get(toReplace));
		}
	}
	
	private void evaluateTokensOver(final Range range) {
		
//		for (LiteralToken token : this.tokensCopy) {
//			System.out.println(token);
//		}
		
		evaluateBracketsOver(range);
		evaluateArithmeticTermOver(range);
	}
	private void evaluateBracketsOver(final Range range) {
		if (this.value == LiteralValue.ERROR) {
			return;
		}
		
		Range bracketRange = rangeOfInnermostBracketPairIn(range);
		if (bracketRange.count != 0) {
			int openingBracketIndex = bracketRange.start;
			int closingBracketIndex = bracketRange.start + bracketRange.count - 1;
			int seperatorIndex = indexOfSymbol(",", bracketRange);
			
			if (isFunctionOpeningBracket(openingBracketIndex)) {
				if (seperatorIndex < this.tokensCopy.size()) {
					// Binary function.
					removePair(openingBracketIndex, closingBracketIndex);
					this.tokensCopy.remove(seperatorIndex - 1);
					
					Range leftArgRange = new Range(openingBracketIndex, 
												   seperatorIndex - openingBracketIndex - 1);
					evaluateTokensOver(leftArgRange);
					
					Range rightArgRange = new Range(openingBracketIndex + 1,
													closingBracketIndex - seperatorIndex -1);
					evaluateTokensOver(rightArgRange);
					
					if (leftArgRange.count != 0
						&& rightArgRange.count != 0) {
						leftArgRange.count  = 1;
						rightArgRange.count = 1;
					} else {
						leftArgRange.count  = 0;
						rightArgRange.count = 0;
					}
					evaluateBinaryFunctionWith(leftArgRange, rightArgRange);
				} else {
					// Unary function.
					removePair(openingBracketIndex, closingBracketIndex);
					
					Range argumentRange = new Range(openingBracketIndex, 
												    closingBracketIndex - openingBracketIndex - 1);
					
					evaluateUnaryFunctionWith(argumentRange);
				}
			} else {
				if (seperatorIndex == this.tokensCopy.size()) {
					// Encapsulation.
					removePair(openingBracketIndex, closingBracketIndex);
					Range evalRange = new Range(bracketRange.start, bracketRange.count - 2);
					evaluateTokensOver(evalRange);
				} else {
					this.value = LiteralValue.ERROR;
				}
			}
			Range recursionRange = new Range(range.start, range.count - bracketRange.count + 1);
			evaluateBracketsOver(recursionRange);
		}
	}
	private void evaluateArithmeticTermOver(final Range range) {
		if (this.value == LiteralValue.ERROR) {
			return;
		}
		
		// Unary minus.
		int symbolIndex = indexOfSymbol("-", range);
		if (symbolIndex < this.tokensCopy.size()) {
			if (isUnaryMinus(symbolIndex, range)) {
				evaluateUnaryMinus(symbolIndex);
				Range newRange = new Range(range.start, range.count - 1);
				evaluateArithmeticTermOver(newRange);
				return;
			}
		}
		
		// Multiplication, division.
		symbolIndex = indexOfSymbol("*", range);
		int altSymbolIndex = indexOfSymbol("/", range);
		if (symbolIndex != altSymbolIndex) {
			if (symbolIndex < altSymbolIndex) {
				evaluateBinaryOperation(symbolIndex, LiteralArithmeticOperation.MULTIPLICATION);
			} else if (symbolIndex > altSymbolIndex) {
				evaluateBinaryOperation(altSymbolIndex, LiteralArithmeticOperation.DIVISION);
			}
			Range newRange = new Range(range.start, range.count - 2);
			evaluateArithmeticTermOver(newRange);
			return;
		}
		
		// Addition, subtraction.
		symbolIndex = indexOfSymbol("+", range);
		altSymbolIndex = indexOfSymbol("-", range);
		if (symbolIndex != altSymbolIndex) {
			if (symbolIndex < altSymbolIndex) {
				evaluateBinaryOperation(symbolIndex, LiteralArithmeticOperation.ADDITION);
			} else if (symbolIndex > altSymbolIndex) {
				evaluateBinaryOperation(altSymbolIndex, LiteralArithmeticOperation.SUBTRACTION);
			}
			Range newRange = new Range(range.start, range.count - 2);
			evaluateArithmeticTermOver(newRange);
			return;
		}
		
		// Equals.
		symbolIndex = indexOfSymbol("==", range);
		if (symbolIndex < this.tokensCopy.size()) {
			evaluateEquals(symbolIndex);
		}
	}
	
	private void removePair(final int leftIndex, final int rightIndex) {
		this.tokensCopy.remove(leftIndex);
		this.tokensCopy.remove(rightIndex - 1);
	}
	private int indexOfSymbol(final String symbol, final Range range) {
		int i = 0;
		while (i < range.count
			   && (i + range.start) < this.tokensCopy.size()) {
			LiteralToken token = this.tokensCopy.get(range.start + i);
			if (token.kind == LiteralTokenKind.SYMBOL_TOKEN
				&& token.value.compareTo(symbol) == 0) {
				return range.start + i;
			}
			i++;
		}
		
		return this.tokensCopy.size();
	}
	private Range rangeOfInnermostBracketPairIn(final Range range) {
		
		int openingBracketIndex = 0;
		int closingBracketIndex = 0;
		int bracketPairNr = 0;
		int maxBracketPairNr = 0;
		boolean openingBracketFound = false;
		boolean closingBracketFound = false;
		
		int lookupIndex = range.start;
		while (!closingBracketFound
			   && (lookupIndex < (range.start + range.count))
			   && (lookupIndex < this.tokensCopy.size())) {
			
			LiteralToken lookupToken = this.tokensCopy.get(lookupIndex);
			
			if (lookupToken.kind == LiteralTokenKind.SYMBOL_TOKEN) {
				String lookupTokenValue = lookupToken.value;
				
				if (lookupTokenValue.compareTo("(") == 0) {
					bracketPairNr++;
					if (bracketPairNr > maxBracketPairNr) {
						maxBracketPairNr = bracketPairNr;
						openingBracketIndex = lookupIndex;
						openingBracketFound = true;
					}
				} else if (lookupTokenValue.compareTo(")") == 0) {
					bracketPairNr--;
					if (bracketPairNr == maxBracketPairNr - 1
						|| bracketPairNr == 0) {
						closingBracketIndex = lookupIndex;
						closingBracketFound = true;
					}
				}
			}
			lookupIndex++;
		}
		
		Range bracketPairRange;
		if (closingBracketFound && openingBracketFound) {
			bracketPairRange = new Range(openingBracketIndex, closingBracketIndex - openingBracketIndex + 1);
		} else {
			bracketPairRange = new Range(0, 0);
		}
		return bracketPairRange;
	}
	private boolean isFunctionOpeningBracket(final int bracketIndex) {
		if (bracketIndex > 0) {
			LiteralToken possibleWordToken = this.tokensCopy.get(bracketIndex - 1);
			if (possibleWordToken.kind == LiteralTokenKind.WORD_TOKEN) {
				return isPredefinedEDLFunction(possibleWordToken.value);
			}
		}
		
		return false;
	}
	private boolean isUnaryMinus(final int minusIndex, final Range range) {
		if (minusIndex == range.start) {
			return true;
		} else if (minusIndex > range.start
				   && minusIndex < (range.start + range.count - 1)
				   && minusIndex < (this.tokensCopy.size() - 1)) {
			LiteralTokenKind possibleLeftOperandKind = this.tokensCopy.get(minusIndex - 1).kind;
			LiteralTokenKind rightOperandKind = this.tokensCopy.get(minusIndex + 1).kind;
			
			// Left operand is no number/parameter, right operand is number/parameter.
			if ((possibleLeftOperandKind != LiteralTokenKind.NUMBER_TOKEN
				 && possibleLeftOperandKind != LiteralTokenKind.PARAMETER_TOKEN)
				&& (rightOperandKind == LiteralTokenKind.NUMBER_TOKEN 
					|| rightOperandKind == LiteralTokenKind.PARAMETER_TOKEN)) {
				return true;
			}
		}
		
		return false;
	}
	private boolean isPredefinedEDLFunction(final String word) {
		if (word.equals("edlValidation_biggerThan")
			|| word.equals("edlValidation_lowerThan")
			|| word.equals("edlValidation_equalOrBiggerThan")
			|| word.equals("edlValidation_equalOrLowerThan")
			|| word.equals("edlValidation_exists")
			|| word.equals("edlValidation_strIsEqual")) {
			
			return true;
		}
		
		return false;
	}
	
	private void evaluateUnaryFunctionWith(final Range argumentRange) {
		if (this.tokensCopy.get(argumentRange.start - 1).value.compareTo("edlValidation_exists") == 0) {
			evaluateEDLValidationExistsWith(argumentRange);
		}
	}
	private void evaluateEDLValidationExistsWith(final Range argumentRange) {
		LiteralToken resultToken;
		if (argumentRange.count == 1) {
			LiteralTokenKind tokenKind = this.tokensCopy.get(argumentRange.start).kind;
			if (tokenKind == LiteralTokenKind.NUMBER_TOKEN
				|| tokenKind == LiteralTokenKind.STRING_TOKEN
				|| tokenKind == LiteralTokenKind.PARAMETER_TOKEN) {
				
				this.tokensCopy.remove(argumentRange.start);
				resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "TRUE");
				this.tokensCopy.set(argumentRange.start - 1, resultToken);
			} else {
				this.value = LiteralValue.ERROR;
			}
		} else {
			resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
			this.tokensCopy.set(argumentRange.start - 1, resultToken);
		}
	}
	
	private void evaluateBinaryFunctionWith(final Range leftArgRange, 
											final Range rightArgRange) {
		if (leftArgRange.count == 0 || rightArgRange.count == 0) {
			this.tokensCopy.clear();
			this.value = LiteralValue.FALSE;
			this.tokensCopy.add(new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE"));
			return;
		}
		
		if (this.tokensCopy.get(leftArgRange.start - 1).value.compareTo("edlValidation_strIsEqual") == 0) {
			evaluateEDLValidationStrcmpWith(leftArgRange, rightArgRange);
			
		} else if (this.tokensCopy.get(leftArgRange.start - 1).value.compareTo("edlValidation_biggerThan") == 0) {
			evaluateEDLValidationCompare(leftArgRange, rightArgRange, LiteralComparisonOperator.BIGGERTHAN);
			
		} else if (this.tokensCopy.get(leftArgRange.start - 1).value.compareTo("edlValidation_equalOrBiggerThan") == 0) {
			evaluateEDLValidationCompare(leftArgRange, rightArgRange, LiteralComparisonOperator.EQUAL_OR_BIGGERTHAN);
			
		} else if (this.tokensCopy.get(leftArgRange.start - 1).value.compareTo("edlValidation_lowerThan") == 0) {
			evaluateEDLValidationCompare(leftArgRange, rightArgRange, LiteralComparisonOperator.LOWERTHAN);
			
		} else if (this.tokensCopy.get(leftArgRange.start - 1).value.compareTo("edlValidation_equalOrLowerThan") == 0) {
			evaluateEDLValidationCompare(leftArgRange, rightArgRange, LiteralComparisonOperator.EQUAL_OR_LOWERTHAN);
			
		}
	}
	private void evaluateEDLValidationStrcmpWith(final Range leftArgRange, 
												 final Range rightArgRange) {
		LiteralToken leftToken  = this.tokensCopy.get(leftArgRange.start);
		LiteralToken rightToken = this.tokensCopy.get(rightArgRange.start);
		
		if ((leftToken.kind == LiteralTokenKind.PARAMETER_TOKEN || leftToken.kind == LiteralTokenKind.STRING_TOKEN)
			&& (rightToken.kind == LiteralTokenKind.PARAMETER_TOKEN || rightToken.kind == LiteralTokenKind.STRING_TOKEN)) {
			// Both function parameters are either a string or a rule parameter.
			
			LiteralToken resultToken;
			if (leftToken.value.compareTo(rightToken.value) == 0) {
				resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "TRUE");
			} else {
				resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
			}
			this.tokensCopy.set(leftArgRange.start - 1, resultToken);
			removePair(leftArgRange.start, rightArgRange.start);
		} else {
			this.value = LiteralValue.ERROR;
		}
	}
	private void evaluateEDLValidationCompare(final Range leftArgRange,
											  final Range rightArgRange,
											  final LiteralComparisonOperator op) {
		LiteralToken leftToken  = this.tokensCopy.get(leftArgRange.start);
		LiteralToken rightToken = this.tokensCopy.get(rightArgRange.start);
		
		if ((leftToken.kind == LiteralTokenKind.PARAMETER_TOKEN || leftToken.kind == LiteralTokenKind.NUMBER_TOKEN)
			&& (rightToken.kind == LiteralTokenKind.PARAMETER_TOKEN || rightToken.kind == LiteralTokenKind.NUMBER_TOKEN)) {
			// Both function parameters are either a number or a rule parameter.
			
			LiteralToken resultToken;
			
			try {
				double leftValue  = Double.parseDouble(leftToken.value);
				double rightValue = Double.parseDouble(rightToken.value);
				if ((leftValue > rightValue && op == LiteralComparisonOperator.BIGGERTHAN)
			        || (leftValue >= rightValue && op == LiteralComparisonOperator.EQUAL_OR_BIGGERTHAN)
			        || (leftValue < rightValue && op == LiteralComparisonOperator.LOWERTHAN)
			        || (leftValue <= rightValue && op == LiteralComparisonOperator.EQUAL_OR_LOWERTHAN)) {
			        resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "TRUE");
			    } else {
			        resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
			    }
			} catch (NumberFormatException e) {
				resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
			}
			
			this.tokensCopy.set(leftArgRange.start - 1, resultToken);
			removePair(leftArgRange.start, rightArgRange.start);
		}
	}

	private void evaluateUnaryMinus(final int minusIndex) {
		if (minusIndex < (this.tokensCopy.size() - 1)) {
			LiteralToken operandToken = this.tokensCopy.get(minusIndex + 1);
			if (operandToken.kind == LiteralTokenKind.NUMBER_TOKEN
				|| operandToken.kind == LiteralTokenKind.PARAMETER_TOKEN) {
				
				LiteralToken resultToken;
				try {
					double operandValue = Double.parseDouble(operandToken.value);
					operandValue *= -1.0;
					resultToken = new LiteralToken(LiteralTokenKind.NUMBER_TOKEN, new Double(operandValue).toString());
				} catch (NumberFormatException e) {
					resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
				}
				
				this.tokensCopy.set(minusIndex, resultToken);
				this.tokensCopy.remove(minusIndex + 1);
			} else {
				this.value = LiteralValue.ERROR;
			}
		} else {
			this.value = LiteralValue.ERROR;
		}
	}
	private void evaluateBinaryOperation(final int index,
			 							 final LiteralArithmeticOperation op) {
		if (index > 0 && index < (this.tokensCopy.size() - 1)) {
			
			LiteralToken resultToken;
			try {
				double leftValue  = Double.parseDouble(this.tokensCopy.get(index - 1).value);
				double rightValue = Double.parseDouble(this.tokensCopy.get(index + 1).value);
				
				if (op == LiteralArithmeticOperation.MULTIPLICATION) {
					leftValue *= rightValue;
				} else if (op == LiteralArithmeticOperation.DIVISION) {
					leftValue /= rightValue;
				} else if (op == LiteralArithmeticOperation.ADDITION) {
					leftValue += rightValue;
				} else if (op == LiteralArithmeticOperation.SUBTRACTION) {
					leftValue -= rightValue;
				}
				
				resultToken = new LiteralToken(LiteralTokenKind.NUMBER_TOKEN, new Double(leftValue).toString());
			} catch (NumberFormatException e) {
				resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
			}
			
			
			
			this.tokensCopy.set(index - 1, resultToken);
			removePair(index, index + 1);
			
		} else {
			this.value = LiteralValue.ERROR;
		}
	}
	private void evaluateEquals(final int equalsIndex) {
		if (equalsIndex > 0 && equalsIndex < this.tokensCopy.size() - 1) {
			LiteralToken leftOperand  = this.tokensCopy.get(equalsIndex - 1);
			LiteralToken rightOperand = this.tokensCopy.get(equalsIndex + 1);
			if ((leftOperand.kind == LiteralTokenKind.PARAMETER_TOKEN || leftOperand.kind == LiteralTokenKind.NUMBER_TOKEN)
				&& (rightOperand.kind == LiteralTokenKind.PARAMETER_TOKEN || rightOperand.kind == LiteralTokenKind.NUMBER_TOKEN)) {
				
				LiteralToken resultToken;
				try {
					double leftValue  = Double.parseDouble(leftOperand.value);
					double rightValue = Double.parseDouble(rightOperand.value);
					if (leftValue == rightValue) {
						resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "TRUE");
					} else {
						resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
					}
				} catch (NumberFormatException e) {
					resultToken = new LiteralToken(LiteralTokenKind.BOOLEAN_TOKEN, "FALSE");
				}
				
				this.tokensCopy.set(equalsIndex - 1, resultToken);
				removePair(equalsIndex, equalsIndex + 1);
			} else {
				this.value = LiteralValue.ERROR;
			}
		} else {
			this.value = LiteralValue.ERROR;
		}
	}
	
	/*=====================================*/
	/*===== Helper classes and enums. =====*/
	/*=====================================*/
	
	private class LiteralToken {
		
		private LiteralTokenKind kind;
		private String value;
		
		LiteralToken(final LiteralTokenKind kind,
				     final String value) {
			this.kind = kind;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return "LiteralToken{" + this.kind + ", " + this.value + "}";
		}
	}
	
	private enum LiteralTokenKind {
		NULL_TOKEN,
		BOOLEAN_TOKEN,
		NUMBER_TOKEN,
		STRING_TOKEN,
		PARAMETER_TOKEN,
		WORD_TOKEN,
		SYMBOL_TOKEN;
	}
	
	private class Range {
		int start = -1;
		int count = 0;
		
		Range(final int start, final int count) {
			this.start = start;
			this.count = count;
		}
	}
	
	private enum LiteralComparisonOperator {
	    BIGGERTHAN,
	    EQUAL_OR_BIGGERTHAN,
	    LOWERTHAN,
	    EQUAL_OR_LOWERTHAN;
	}
	private enum LiteralArithmeticOperation {
	    MULTIPLICATION,
	    DIVISION,
	    ADDITION,
	    SUBTRACTION;
	}
	
}
