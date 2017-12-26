package org.xenei.rdf_diff_patch;

import static org.junit.Assert.*;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.test.helpers.ModelHelper;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

import difflib.Patch;


public class FactoryTest
{
	
	protected void testPatch(Model orig, Model revised)
	{
		final RDFConnection origC = RDFConnectionFactory.connect( DatasetFactory.create(orig));
		final RDFConnection revC = RDFConnectionFactory.connect( DatasetFactory.create(revised));
		UpdateFactory updateFactory = new UpdateFactory();

		final Patch<Quad> patch = Factory.patch(origC, revC);
		final UpdateRequest req= updateFactory.asUpdate(patch);
		
		origC.update(req);
		
		assertTrue( revised.isIsomorphicWith(orig));
	}
	
	@Test
	public void testSimplePatch() {
		String initModelA = "x P a; x P b; x R c";
		String initModelB = "x P a; x P b; x R C";


		final Model orig = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), initModelA );
		final Model revised = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), initModelB );
		
		
		testPatch( orig, revised );
		assertTrue( orig.contains( ModelHelper.statement( "x R C") ));
	}
	
	
	/**
	 * Test that a simple blank node change works
	 */
	@Test
	public void testSimplePatchWithBlankNode() {
		String initModelA = "s p _:a ; _:a p o; _:a p2 o2 ";
		String initModelB = "s p _:b ; _:b p o; _:b p2 o3 ";

		final Model orig = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), initModelA);
		final Model revised = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), initModelB);

		testPatch( orig, revised );
		
		
	}
}
