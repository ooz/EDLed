package de.mpg.cbs.edled.util;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.mpg.cbs.edled.util.Recent;


public class TestRecent {
	
	private final static String FST = "first";
	private final static String SND = "second";
	private final static String LST = "last";
	
	private final static String SEP = ",";
	
	private final static String FORWARD  = FST + SEP + SND + SEP + LST;
	private final static String BACKWARD = LST + SEP + SND + SEP + FST;
	
	private final static String INS = "insert";
	
	private List<String> items;
	
	private Recent<String> recents;
	private Recent<String> recentsBackward;
	
	@Before
	public void setUp() throws Exception {
		this.items = new LinkedList<String>();
		this.items.add("first");
		this.items.add("second");
		this.items.add("last");
		
		this.recents = new Recent<String>(items, true);
		this.recentsBackward = new Recent<String>(items, false);
	}

	@Test
	public void testMostRecent() {
		Assert.assertEquals(FST, this.recents.getMostRecent());
		Assert.assertEquals(LST, this.recentsBackward.getMostRecent());
	}
	
	@Test
	public void testAddMostRecent() {
		this.recents.addMostRecent(INS);
		Assert.assertEquals(INS, this.recents.getMostRecent());
	}
	
	@Test
	public void testRemove() {
		this.recents.remove(FST);
		Assert.assertEquals(SND, this.recents.getMostRecent());
		
		this.recents.remove(LST);
		Assert.assertEquals(SND, this.recents.getMostRecent());
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(this.items.size(), this.recents.size());
	}
	
	@Test
	public void testAsString() {
		Assert.assertEquals(FORWARD, this.recents.asString(SEP, true));
		Assert.assertEquals(BACKWARD, this.recents.asString(SEP, false));
		
		Assert.assertEquals(FORWARD, this.recentsBackward.asString(SEP, false));
		Assert.assertEquals(BACKWARD, this.recentsBackward.asString(SEP, true));
	}
	
	@Test
	public void testAsList() {
		List<String> actItems = this.recents.asList(true);
		Assert.assertEquals(this.items.size(), actItems.size());
		
		for (int i = 0; i < this.items.size(); i++) {
			Assert.assertEquals(this.items.get(i), actItems.get(i));
		}
	}
}
