RDF Diff and Patch Routines
===========================

These routines calculate the difference between two RDFConnections and provides a mechanism to create an UpdateRequest to cause them to be isomorphic.

These routines should not be confused with the <a href='https://afs.github.io/rdf-patch/'>RDF-Patch file format</a>.   


Patch Extraction
================

The `PatchFactory` is used to create a patch from various types of RDF stores.  It creates an order list of Quads for both arguments and then performs a diff to determine which quads need to be inserted or deleted.  This is returned a a `Patch<Quad>` object.

The `UpdateFactory` is used to convert the `Patch<Quad>` object into an `UpdateRequest` that can be passed to an RDFConnection to perform the update to make the contained graphs isomorphic. 

Example Usage
=============

The following code snippet creates two RDFConnections an (original and a changed), finds the differences between them and updates the original to match the changed. 

    RDFConnection origC = RDFConnectionFactory.connect( ... );
    RDFConnection chgdC = RDFConnectionFactory.connect( ... );
    
    Patch<Quad> patch = PatchFactory.patch( origC, chgdC );
    UpdateRequest req = UpdateFactory.asUpdate( patch );
    origC.update( req );
	
