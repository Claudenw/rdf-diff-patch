package org.xenei.rdf_diff_patch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.WrappedIterator;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.MyersDiff;

public class Factory
{
	private final static Var G = Var.alloc("g"); 
	private final static Var S = Var.alloc("s"); 
	private final static Var P = Var.alloc("p"); 
	private final static Var O = Var.alloc("o"); 

	public static Patch<Quad> patch( RDFConnection orig, RDFConnection revised)
	{
		
		Function<QuerySolution, Quad> toQuad = new Function<QuerySolution, Quad>(){

			public Quad apply(QuerySolution qs)
			{
				return new Quad( qs.get(G.getName()).asNode(), qs.get(S.getName()).asNode(),
						qs.get(P.getName()).asNode(), qs.get(O.getName()).asNode());
			}};
		
		SelectBuilder inner = new SelectBuilder();
		ExprFactory factory = inner.getExprFactory();
		inner.addVar(G).addVar(S).addVar(P).addVar(O)
				.addWhere( S, P, O ).addBind( factory.asExpr(Quad.defaultGraphIRI), G);
		SelectBuilder outer = new SelectBuilder().addVar(G).addVar(S).addVar(P).addVar(O)
				.addGraph( G,  S, P, O ).addUnion(inner).addOrderBy(G)
				.addOrderBy(S).addOrderBy(P).addOrderBy(O);
		
		List<Quad> origQ = WrappedIterator.create(orig.query(outer.build()).execSelect())
				.mapWith( toQuad ).toList();
						
		List<Quad> revQ = WrappedIterator.create(revised.query(outer.build()).execSelect())
				.mapWith( toQuad ).toList();		
				
		return DiffUtils.diff(origQ, revQ, new MyersDiff<Quad>());		
	}
	
	

}
