package org.xenei.rdf_diff_patch;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.test.helpers.ModelHelper;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class QuadListTest
{
	final Model orig = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), "x P a; x P b; x R c");
	final Model revised = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), "x P a; x P b; x R C");

	@Test
	public void standard() {
		Node g = NodeFactory.createURI("g");
		Node s = NodeFactory.createURI("s");
		Node p = NodeFactory.createURI("p");
		Node o = NodeFactory.createURI("o");
		Node o2 = NodeFactory.createURI("o2");
		Node o3 = NodeFactory.createURI("o3");
		
		Quad q1 = new Quad( g, s, p , o );
		QuadList ql = new QuadList();
		ql.add( q1 );
		Quad q2 = new Quad( g, s, p , o2 );
		ql.add( q2);
		
		assertEquals( 2, ql.size() );
		assertTrue( ql.contains( q1 ) );
		assertTrue( ql.contains( q2 ) );
		
		List<Quad> lst = new ArrayList<Quad>();
		lst.add( q1 );
		lst.add(q2);
		assertTrue( ql.containsAll( lst ) );
		
		lst.add( new Quad( g, s, p , o3 ));
		assertFalse( ql.containsAll( lst ) );
		
		assertEquals( q1,  ql.get(0) );
		assertEquals( q2, ql.get(1));
		
	}

	@Test
	public void subList() {
		Node g = NodeFactory.createURI("g");
		Node g2 = NodeFactory.createURI("g2");
		Node g3 = NodeFactory.createURI("g3");
		Node s = NodeFactory.createURI("s");
		Node p = NodeFactory.createURI("p");
		Node o = NodeFactory.createURI("o");
		Node o2 = NodeFactory.createURI("o2");
		Node o3 = NodeFactory.createURI("o3");
		
		QuadList ql = new QuadList();
		List<Quad> qlst = new ArrayList<Quad>();
		qlst.add( new Quad( g, s, p , o ));
		qlst.add( new Quad( g, s, p , o2 ));
		ql.addAll( qlst );
				
		qlst = new ArrayList<Quad>();
		qlst.add( new Quad( g2, s, p , o ));
		qlst.add( new Quad( g2, s, p , o2 ));
		ql.addAll( qlst );

		qlst = new ArrayList<Quad>();
		qlst.add( new Quad( g3, s, p , o ));
		qlst.add( new Quad( g3, s, p , o2 ));
		ql.addAll( qlst );

		qlst = new ArrayList<Quad>();
		qlst.add( new Quad( g, s, p , o2 ));
		qlst.add( new Quad( g2, s, p , o ));
		qlst.add( new Quad( g2, s, p , o2 ));
		qlst.add( new Quad( g3, s, p , o ));

		List<Quad> subList = ql.subList(1, 5);
		
		assertEquals( 4, subList.size() );
		assertTrue( subList.containsAll( qlst ) );
				
		assertFalse( subList.contains( new Quad( g, s, p , o ) ) );
	}

}
