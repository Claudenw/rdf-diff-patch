package org.xenei.rdf_diff_patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import difflib.Chunk;
import difflib.Delta;
import difflib.Patch;

public class RdfPatch extends Patch<Quad> 
{
	public RdfPatch( Patch<Quad> patch )
	{
		super( patch );
	}
	
	public UpdateRequest asUpdate() {
		QuadList del = new QuadList();
		QuadList add = new QuadList();
		for (Delta<Quad> delta : this.getDeltas())
		{
			switch (delta.getType())
			{
				case CHANGE:
					del.addAll( delta.getOriginal().getLines());
					add.addAll( delta.getRevised().getLines());
					break;
				case DELETE:
					del.addAll( delta.getOriginal().getLines());
					break;
				case INSERT:
					add.addAll( delta.getRevised().getLines());
					break;
			}
		}
		UpdateDataDelete delRq = new UpdateDataDelete( new QuadDataAcc( del ));
		UpdateDataInsert insRq = new UpdateDataInsert( new QuadDataAcc( add ));
		UpdateRequest req = new UpdateRequest( delRq ).add( insRq);
		return req;
	}
	
	
}
