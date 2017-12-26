package org.xenei.rdf_diff_patch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.WrappedIterator;

import difflib.Chunk;
import difflib.Delta;
import difflib.Patch;

public class UpdateFactory
{
	public UpdateRequest asUpdate(Patch<Quad> patch) {
		QuadList del = new QuadList();
		QuadList add = new QuadList();
		UpdateRequest req = new UpdateRequest();
		for (Delta<Quad> delta : patch.getDeltas())
		{
			switch (delta.getType())
			{
				case CHANGE:
					delReq( req, del, delta.getOriginal());
					addReq( req, add, delta.getRevised());
					break;
				case DELETE:
					del.addAll( delta.getOriginal().getLines());
					break;
				case INSERT:
					add.addAll( delta.getRevised().getLines());
					break;
			}
		}
		if (! del.isEmpty())
		{
			req.add( new UpdateDataDelete( new QuadDataAcc( del )));
		}
		if (! add.isEmpty())
		{
			req.add(new UpdateDataInsert( new QuadDataAcc( add )));
		}
		return req;
	}
	
	private boolean hasBlank( Triple t )
	{
		return t.getSubject().isBlank() || t.getPredicate().isBlank() || t.getObject().isBlank();
	}
	
	private void delReq( UpdateRequest req, QuadList lst, Chunk<Quad> chunk)
	{
		
		List<Node> blanks = new ArrayList<Node>( extractBlanks( chunk ));
		if (blanks.isEmpty())
		{
			lst.addAll( chunk.getLines() );
		}
		else
		{
			DeleteBlankMapper mapper = new DeleteBlankMapper( blanks );
			List<Triple> tLst = new TransformingList<Quad,Triple>( 
			    		chunk.getLines(), q -> q.asTriple() );
			
			UpdateBuilder mod = new UpdateBuilder();
			
			/* TODO if anonymous exists as subject for multiple clauses that we are not deleting
			 * then we need to delete the entire block.  Here we are only deleting the initial link.
			 */
			WrappedIterator.create(tLst.iterator()).filterKeep( t -> hasBlank( t ))
				.mapWith(  mapper ).forEachRemaining( t -> mod.addWhere(t));
			
			
			WrappedIterator.create(tLst.iterator()).mapWith( mapper )
					.forEachRemaining( t -> mod.addDelete(t));
			
			req.add( mod.build() );
			
		}
	}
	
	private void addReq( UpdateRequest req, QuadList lst, Chunk<Quad> chunk)
	{
		
		List<Node> blanks = new ArrayList<Node>( extractBlanks( chunk ));
		if (blanks.isEmpty())
		{
			lst.addAll( chunk.getLines() );
		}
		else
		{
			AddBlankMapper mapper = new AddBlankMapper( blanks );
			List<Triple> tLst = new TransformingList<Quad,Triple>( 
			    		chunk.getLines(), q -> q.asTriple() );
			Graph g = new CollectionGraph( tLst);
			
			UpdateBuilder mod = new UpdateBuilder();
			
			g.find().mapWith(  mapper )
				.forEachRemaining( t -> mod.addInsert(t));
			
			
			req.add( mod.build() );
			
		}
	}
	
	private Set<Node> extractBlanks(Chunk<Quad> chunk) {
		Set<Node> blanks = new HashSet<Node>();
		for (Quad q : chunk.getLines())
		{
			if (q.getSubject().isBlank())
			{
				blanks.add( q.getSubject());
			}
			if (q.getPredicate().isBlank()) {
				blanks.add( q.getPredicate());
			}
			if (q.getObject().isBlank()) {
				blanks.add( q.getObject());
			}			
		}
		return blanks;
		
	}
	
	private class DeleteBlankMapper implements Function<Triple,Triple>
	{
		List<Node> blanks;
		
		DeleteBlankMapper(List<Node> blanks)
		{
			this.blanks = blanks;
		}
		
		Node map(Node node)
		{
			if (node.isBlank())
			{
				return NodeFactory.createVariable( "v"+blanks.indexOf( node ));
			}
			return node;
		}

		@Override
		public Triple apply(Triple t)
		{
			return new Triple( map(t.getSubject()), map(t.getPredicate()), 
					map(t.getObject()));
		}
	}
	
	private class AddBlankMapper extends DeleteBlankMapper {
		AddBlankMapper(List<Node> blanks)
		{
			super(blanks);
		}

		Node map(Node node)
		{
			if (node.isBlank())
			{
				return NodeFactory.createBlankNode( "_:"+blanks.indexOf( node ));
			}
			return node;
		}
	}

}
