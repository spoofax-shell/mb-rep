package org.spoofax.interpreter.library.language.spxlang;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jdbm.PrimaryMap;
import jdbm.RecordListener;
import jdbm.SecondaryHashMap;
import jdbm.SecondaryKeyExtractor;

import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpxPrimarySymbolTable implements INamespaceResolver , IPackageDeclarationRecordListener,IModuleDeclarationRecordListener {
	private final String SRC = this.getClass().getSimpleName();
	private final ISpxPersistenceManager _manager; // Persistence Manager
	private final PrimaryMap <NamespaceUri,INamespace> namespaces;
	private final SecondaryHashMap <IStrategoList,NamespaceUri,INamespace> namespaceByStrategoId;
	private transient INamespace _activeNamespace ;
	
	//TODO implement remove package and remove module event handlers
	public SpxPrimarySymbolTable (SpxSemanticIndexFacade facade){
		assert facade != null  : "SpxSemanticIndexFacade  is expected to non-null" ;
		_manager = facade.persistenceManager();

		String tableName = _manager.getProjectName() + "primary_symbol_table.idx";
		
		namespaces  = _manager.loadHashMap(tableName + "namespaces.idx");
		namespaceByStrategoId = namespaces.secondaryHashMap(tableName+ ".namespaceByStrategoId.idx", 
				new SecondaryKeyExtractor<IStrategoList,NamespaceUri,INamespace>()
				{
					public IStrategoList extractSecondaryKey(NamespaceUri k,INamespace v) {
						return k.id(); 
					}
				});
		
		addGlobalNamespace(facade);
	}
	
	/**
	 * Adding Global Namespace in symbol-table by default.
	 * @param facade
	 */
	public void addGlobalNamespace(SpxSemanticIndexFacade facade){
		
		this.defineNamespace(GlobalNamespace.createInstance(facade));
	}
	
	public void defineNamespace(INamespace namespace) {
		// if not already defined, defining this namespace
		if (!containsNamespace(namespace))
			this.namespaces.put(namespace.namespaceUri(), namespace);
	}
	
	NamespaceUri toNamespaceUri(IStrategoList spoofaxId) {
		NamespaceUri uri = getNamespaceUri(spoofaxId);
		if(uri == null) {
			uri = new NamespaceUri(spoofaxId);
		}
		return uri;
	}
	
	public INamespace resolveNamespace(IStrategoList id){
		Iterator<INamespace> resolvedNamespaces = namespaceByStrategoId.getPrimaryValues(id).iterator();
		if(resolvedNamespaces.hasNext())
			return resolvedNamespaces.next();
		else
			return null;
	}
	
	public INamespace removeNamespace(IStrategoList id){
		INamespace resolveNamespace  = resolveNamespace(id) ;
		
		if(resolveNamespace != null){
			this.namespaces.remove(resolveNamespace.namespaceUri());
		}
		
		return resolveNamespace;
	}
	
	public INamespace resolveNamespace(NamespaceUri id) {
		return namespaces.get(id); 
	}
	
	public NamespaceUri getNamespaceUri(IStrategoList id) {
		Iterable<NamespaceUri> uriIterator = namespaceByStrategoId.get(id);
		if(uriIterator != null)
			for( NamespaceUri uri : uriIterator)
				return uri;
		
		return null;
	}
	
	public boolean containsNamespace(IStrategoList id) { return namespaceByStrategoId.containsKey(id);}
	
	public boolean containsNamespace(NamespaceUri namespaceId) { return namespaces.containsKey(namespaceId);}
	
	public boolean containsNamespace(INamespace namespace) { return this.containsNamespace(namespace.namespaceUri());}

	public void clear(){  namespaces.clear();  }
	
	public int size() { return namespaces.size();}
	 
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() { return "SpxPrimarySymbolTable ( defined namespaces : " + namespaces.keySet() + ")"; 	}
	
	public Set<NamespaceUri> getAllNamespaces() { return namespaces.keySet() ; }

	public void defineSymbol(SpxSemanticIndexFacade facade, IStrategoList namespaceId ,  SpxSymbolTableEntry symTableEntry) throws SpxSymbolTableException {
		
		ensureActiveNamespaceLoaded(namespaceId);
	
		_activeNamespace.define(symTableEntry, facade.persistenceManager()); 
	}
	
	private void ensureActiveNamespaceUnloaded(IStrategoList namespaceId){
	
		if(_activeNamespace.namespaceUri().equalSpoofaxId(namespaceId)){
			_activeNamespace = null;
		}
	}
	private void ensureActiveNamespaceLoaded(IStrategoList namespaceId) throws SpxSymbolTableException{
		if(_activeNamespace== null ||!_activeNamespace.namespaceUri().equalSpoofaxId(namespaceId)){
			//Keeping a transient reference to the current/active Namespace
			//More likely that there are other symbols to be defined in the
			//current and active namespace. In that case, it will imporve 
			//performance as namespace resolving avoided by means of extra 
			//caching
			_activeNamespace = this.resolveNamespace(namespaceId);
			if(_activeNamespace ==null){
				throw new SpxSymbolTableException("Unknown namespaceId: "+ namespaceId+". Namespace can not be resolved from symbol-table") ;
			}
		}
	}

	public Iterable<SpxSymbol> resolveSymbols(SpxSemanticIndexFacade spxSemanticIndexFacade, IStrategoList namespaceId, IStrategoTerm symbolId , IStrategoConstructor symbolType) throws SpxSymbolTableException {
		_manager.logMessage(SRC, "resolveSymbols | Resolving symbols with the following criteria :  search origin " + namespaceId +  " with Key : "+ symbolId + "of Type : "+ symbolType.getName());
		
		ensureActiveNamespaceLoaded(namespaceId);
		List<SpxSymbol> resolvedSymbols = (List<SpxSymbol>)_activeNamespace.resolveAll(symbolId, symbolType ,spxSemanticIndexFacade);
		
		_manager.logMessage(SRC, "resolveSymbols | Resolved Symbols : " + resolvedSymbols);
		return resolvedSymbols;
	}
	
	

	public SpxSymbol resolveSymbol(SpxSemanticIndexFacade spxSemanticIndexFacade, IStrategoList namespaceId, IStrategoTerm symbolId , IStrategoConstructor symbolType) throws SpxSymbolTableException {
		_manager.logMessage(SRC, "resolveSymbol | Resolving symbol with the following criteria :  search origin " + namespaceId +  " with Key : "+ symbolId + "of Type : "+ symbolType.getName());
		
		ensureActiveNamespaceLoaded(namespaceId);
		
		SpxSymbol  resolvedSymbol = _activeNamespace.resolve(symbolId, symbolType ,_activeNamespace ,spxSemanticIndexFacade);
		
		_manager.logMessage(SRC, "resolveSymbol | Resolved Symbol : " + resolvedSymbol );
		
		return resolvedSymbol;
	}
	
	
	public INamespace newAnonymousNamespace(SpxSemanticIndexFacade spxSemanticIndexFacade, IStrategoList enclosingNamespaceId) throws SpxSymbolTableException{
		_manager.logMessage(SRC, "newAnonymousNamespace | Inserting a Anonymous Namespace in following enclosing namespace : "  + enclosingNamespaceId);
		ensureActiveNamespaceLoaded(enclosingNamespaceId);
		
		INamespace localNamespace = LocalNamespace.createInstance(spxSemanticIndexFacade, _activeNamespace); 
		
		_manager.logMessage(SRC, "newAnonymousNamespace | Folloiwng namesapce is created : "  + localNamespace);
		
		_activeNamespace = localNamespace;
		
		return _activeNamespace ;
	}
	
	
	/**
	 * Destroying Namespace with following namespaceId
	 * 
	 * @param spxSemanticIndexFacade
	 * @param enclosingNamespaceId
	 * @return
	 * @throws SpxSymbolTableException
	 */
	public INamespace destroyNamespace(SpxSemanticIndexFacade spxSemanticIndexFacade, IStrategoList namespaceId) throws SpxSymbolTableException{
		_manager.logMessage(SRC, "destroyNamespace | Removing the following namespace : "  + namespaceId);
		
		INamespace ns = this.removeNamespace(namespaceId);
		
		ensureActiveNamespaceUnloaded(namespaceId);
		_manager.logMessage(SRC, "newAnonymousNamespace | Folloiwng namesapce is removed : "  + ns);
		return ns;
	} 
	
	public RecordListener<IStrategoList, PackageDeclaration> getPackageDeclarationRecordListener() {
		return new RecordListener<IStrategoList, PackageDeclaration>(){

			public void recordInserted(IStrategoList packageID,
					PackageDeclaration value) throws IOException {
				// do nothing
				
			}

			public void recordUpdated(IStrategoList packageID,
					PackageDeclaration oldValue, PackageDeclaration newValue)
					throws IOException {
				// do nothing 
			}

			public void recordRemoved(IStrategoList packageID,
					PackageDeclaration value) throws IOException {
				
				removeNamespace(packageID) ;
			}};
	}

	public RecordListener<IStrategoList, ModuleDeclaration> getModuleDeclarationRecordListener() {
		return new RecordListener<IStrategoList, ModuleDeclaration>() {

			public void recordInserted(IStrategoList key, ModuleDeclaration value) throws IOException {
				// do nothing 
				
			}

			public void recordUpdated(IStrategoList key,  ModuleDeclaration oldValue, ModuleDeclaration newValue)
					throws IOException {
				// do nothing 
				
			}

			public void recordRemoved(IStrategoList moduleId, ModuleDeclaration value)
					throws IOException {
				removeNamespace(moduleId) ;
				
			}
			
		};
	}
}
