package org.spoofax.interpreter.library.language;

import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.interpreter.core.Tools.isTermTuple;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LANG_index_add extends AbstractPrimitive {

	private static String NAME = "LANG_index_add";
	
	private final SemanticIndexManager index;
	
	public LANG_index_add(SemanticIndexManager index) {
		super(NAME, 0, 2);
		this.index = index;
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (isTermAppl(tvars[0]) && (isTermTuple(tvars[1]) || isTermString(tvars[1]))) {
			IStrategoAppl entry = (IStrategoAppl) tvars[0];
			ISemanticIndex ind = index.getCurrent();
			SemanticIndexFileDescriptor fileDescriptor = ind.getFileDescriptor(tvars[1]);
			ind.add(entry, fileDescriptor);
			return true;
		} else {
			return false;
		}
	}
}