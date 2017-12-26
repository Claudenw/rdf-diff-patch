package org.xenei.rdf_diff_patch;

import java.util.List;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.WrappedIterator;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.MyersDiff;

/**
 * Factory to create diff and patch files.
 *
 */
public class PatchFactory
{
	private final static Var G = Var.alloc("g"); 
	private final static Var S = Var.alloc("s"); 
	private final static Var P = Var.alloc("p"); 
	private final static Var O = Var.alloc("o"); 

	/**
	 * Create a patch from RDFConnections.
	 * @param orig the RDFConnection that contains the original data.
	 * @param revised the RDFConnection that contains the revised data.
	 * @return the Patch for the difference.
	 */
	public static Patch<Quad> patch( RDFConnection orig, RDFConnection revised)
	{

		final Function<QuerySolution, Quad> toQuad = new Function<QuerySolution, Quad>(){

			@Override
			public Quad apply(QuerySolution qs)
			{
				return new Quad( qs.get(G.getName()).asNode(), qs.get(S.getName()).asNode(),
						qs.get(P.getName()).asNode(), qs.get(O.getName()).asNode());
			}};

			final SelectBuilder inner = new SelectBuilder();
			final ExprFactory factory = inner.getExprFactory();
			inner.addVar(G).addVar(S).addVar(P).addVar(O)
			.addWhere( S, P, O ).addBind( factory.asExpr(Quad.defaultGraphIRI), G);
			final SelectBuilder outer = new SelectBuilder().addVar(G).addVar(S).addVar(P).addVar(O)
					.addGraph( G,  S, P, O ).addUnion(inner).addOrderBy(G)
					.addOrderBy(S).addOrderBy(P).addOrderBy(O);

			final List<Quad> origQ = WrappedIterator.create(orig.query(outer.build()).execSelect())
					.mapWith( toQuad ).toList();

			final List<Quad> revQ = WrappedIterator.create(revised.query(outer.build()).execSelect())
					.mapWith( toQuad ).toList();		

			return DiffUtils.diff(origQ, revQ, new MyersDiff<Quad>());		
	}



}
