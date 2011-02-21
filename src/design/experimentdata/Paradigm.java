package design.experimentdata;

/** Bind for /experimentData/paradigm */
public class Paradigm {
	public long ignoreScansAtStart; // unsigned int
	public DesignStruct design = null;
	
	Paradigm(final long ignoreScansAtStart) {
		this.ignoreScansAtStart = ignoreScansAtStart;
	}
	Paradigm(final long ignoreScansAtStart, final DesignStruct design) {
		this.ignoreScansAtStart = ignoreScansAtStart;
		this.design = design;
	}
}
