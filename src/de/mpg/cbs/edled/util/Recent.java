package de.mpg.cbs.edled.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Recent<T> {
	
	private final List<T> items;
	
	public Recent(final List<T> recents, 
				  final boolean mostRecentFirst) {
		if (mostRecentFirst) {
			this.items = new LinkedList<T>(recents);
		} else {
			this.items = new LinkedList<T>();
			for (T recent : recents) {
				this.items.add(0, recent);
			}
		}
	}
	
	public void addMostRecent(final T newMostRecent) {
		this.items.add(0, newMostRecent);
	}
	
	public T getMostRecent() {
		return this.items.get(0);
	}
	
	public boolean remove(final T toRemove) {
		return this.items.remove(toRemove);
	}
	
	public int size() {
		return this.items.size();
	}
	
	public List<T> asList(final boolean mostRecentFirst) {
		List<T> result;
		
		if (mostRecentFirst) {
			result = this.items;
		} else {
			result = new LinkedList<T>();
			for (T item : this.items) {
				result.add(item);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Converts the Recent objects to a string using their toString() method.
	 * 
	 * @param separatorChar	  String used to separate items in the result
	 * 						  string. 
	 * @param mostRecentFirst Flag indicating whether the most recent item
	 * 						  should be at the beginning of the string.
	 * 						  True is faster than false.
	 * @return				  String representing the items in the 
	 * 						  Recent object.
	 */
	public String asString(final String separatorSeq, 
						   final boolean mostRecentFirst) {
		if (mostRecentFirst) {
			return asStringForward(separatorSeq);
		} else {
			return asStringBackward(separatorSeq);
		}
	}
	
	private String asStringForward(final String sep) {
		if (this.items.size() == 0) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		for (T item : this.items) {
			sb.append(item.toString());
			sb.append(sep);
		}
		
		return sb.substring(0, sb.length() - sep.length());
	}
	private String asStringBackward(final String sep) {
		if (this.items.size() == 0) {
			return "";
		}
		
		String result = "";
		for (T item : this.items) {
			result = sep + item.toString() + result;
		}
		return result.substring(sep.length());
	}

}
