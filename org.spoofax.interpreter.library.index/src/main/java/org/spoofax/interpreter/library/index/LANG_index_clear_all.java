package org.spoofax.interpreter.library.index;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_clear_all extends AbstractPrimitive {
	private static String NAME = "LANG_index_clear_all";

	public LANG_index_clear_all() {
		super(NAME, 0, 0);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		IndexManager.getInstance().getCurrent().clearAll();
		return true;
	}
}
