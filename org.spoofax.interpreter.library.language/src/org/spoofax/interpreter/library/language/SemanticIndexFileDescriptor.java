package org.spoofax.interpreter.library.language;

import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermTuple;

import java.io.File;
import java.net.URI;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class SemanticIndexFileDescriptor {
	private final URI uri;
	
	private final String subfile;
	
	public URI getURI() {
		return uri;
	}
	
	public String getSubfile() {
		return subfile;
	}
	
	public SemanticIndexFileDescriptor(URI uri, String subfile) {
		this.uri = uri;
		this.subfile = "".equals(subfile) ? null : subfile;
	}
	
	public IStrategoTerm toTerm(ITermFactory factory) {
		IStrategoString uriString = factory.makeString(toString());
		IStrategoString descriptorName = factory.makeString(subfile == null ? "" : subfile);
		return factory.makeTuple(uriString, descriptorName);
	}
	
	/**
	 * Converts a term file representation to a SemanticIndexFile,
	 * using the  {@link IOAgent} to create an absolute path.
	 * 
	 * @param agent  The agent that provides the current path and file system access,
	 *               or null if the path should be used as-is.
	 * @param term   A string or (string, string) tuple with the filename
	 *               or the filename and subfilename
	 */
	public static SemanticIndexFileDescriptor fromTerm(IOAgent agent, IStrategoTerm term) {
		String name;
		String descriptor;
		if (isTermTuple(term)) {
			name = asJavaString(term.getSubterm(0));
			descriptor = asJavaString(term.getSubterm(1));
		} else {
			name = asJavaString(term);
			descriptor = null;
		}
		File file = new File(name);
		if (!file.isAbsolute() && agent != null)
			file = new File(agent.getWorkingDir(), name);
		return new SemanticIndexFileDescriptor(file.toURI(), descriptor);
	}
	
	@Override
	public String toString() {
		return "file".equals(uri.getScheme()) ? new File(uri).getAbsolutePath().replace("\\", "/") : uri.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SemanticIndexFileDescriptor) {
			if (obj == this) return true;
			SemanticIndexFileDescriptor other = (SemanticIndexFileDescriptor) obj;
			return other.uri.equals(uri)
					&& (subfile == null ? other.subfile == null : subfile.equals(other.subfile));
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((subfile == null) ? 0 : subfile.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}
}