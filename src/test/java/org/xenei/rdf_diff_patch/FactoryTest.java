package org.xenei.rdf_diff_patch;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.test.helpers.ModelHelper;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;


public class FactoryTest
{
	@Test
	public void t() {


		final Model orig = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), "x P a; x P b; x R c");
		final Model revised = ModelHelper.modelAdd( ModelFactory.createDefaultModel(), "x P a; x P b; x R C");

		final RDFConnection origC = RDFConnectionFactory.connect( DatasetFactory.create(orig));
		final RDFConnection revC = RDFConnectionFactory.connect( DatasetFactory.create(revised));


		final RdfPatch patch = Factory.patch(origC, revC);
		System.out.println( patch.toString() );
		final UpdateRequest req= patch.asUpdate();
		System.out.println( req );
	}
}
