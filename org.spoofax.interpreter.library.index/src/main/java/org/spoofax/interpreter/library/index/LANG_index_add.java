package org.spoofax.interpreter.library.index;

import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.interpreter.core.Tools.isTermTuple;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_add extends AbstractPrimitive {
	private static String NAME = "LANG_index_add";

	public LANG_index_add() {
		super(NAME, 0, 2);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if(isTermAppl(tvars[0]) && (isTermTuple(tvars[1]) || isTermString(tvars[1]))) {
			IStrategoAppl entry = (IStrategoAppl) tvars[0];
			IIndex ind = IndexManager.getInstance().getCurrent();
			IndexPartitionDescriptor partitionDescriptor = ind.getPartitionDescriptor(tvars[1]);
			ind.add(entry, partitionDescriptor);
			return true;
		} else {
			return false;
		}
	}
}
