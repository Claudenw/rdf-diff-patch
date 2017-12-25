package org.xenei.rdf_diff_patch;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.commons.collections4.list.AbstractLinkedList;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;


import difflib.Chunk;

public class QuadList implements List<Quad> {
	protected final List<Chunk<Quad>> lst;
	protected Span span;


	public QuadList() {
		this(new ArrayList<Chunk<Quad>>(), 0 );
	}
	
	protected QuadList( final List<Chunk<Quad>> lst, final int first)
	{
        if (first < 0) {
            throw new IndexOutOfBoundsException("first = " + first);
        }
		this.lst = lst;
		
		this.span = first==0?new ListSpan(): new ListSpan(){

			@Override
			public int getStart()
			{
				return first;
			}};
				
	}

	 protected void rangeCheck(final int index) {
         if (!span.contains(index)) {
             throw new IndexOutOfBoundsException("Index '" + index + "' out of bounds for '" + span + "'");
         }
     }
	 
	public boolean add(Quad e)
	{
		Quad[] qa = {e};
		return addAll( Arrays.asList(qa) );
	}
	
	public void add(int index, Quad element)
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends Quad> c)
	{
		if (c.isEmpty())
		{
			return false;
		}
		List<Quad> l = (c instanceof List)? (List<Quad>) c : new ArrayList<Quad>( c );
		
		if (lst.isEmpty())
		{
			return lst.add( new Chunk<Quad>( 0, l ));
		}
		else
		{
			Chunk<Quad> chk = lst.get( lst.size()-1);
			int offset = chk.getPosition() + chk.size();
			chk = new Chunk<Quad>( offset, l); 
			return lst.add( new Chunk<Quad>( offset, l));
		}
	}

	public boolean addAll(int index, Collection<? extends Quad> c)
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		lst.clear();
	}

	private ExtendedIterator<Chunk<Quad>> chunkIterator()
	{
		return WrappedIterator.create(lst.iterator());
	}
	
	public boolean contains(Object o)
	{
		if (o instanceof Quad)
		{
			Quad q = (Quad)o;
			Iterator<Quad> iter = iterator();
			while (iter.hasNext())
			{
				if (q.equals( iter.next()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsAll(Collection<?> c)
	{
		for (Object o : c)
		{
			if (!contains(o))
			{
				return false;
			}
		}
		return true;
	}

	public Quad get(int index)
	{
		int pos = index+span.getStart();
		if (!span.contains(pos))
		{
			throw new IndexOutOfBoundsException( index+"<0" );
		}
				
		for (Chunk<Quad> l : lst)
		{		
			ChunkSpan cSpan = new ChunkSpan( l );
			if (cSpan.contains( pos ))
			{
				pos = Span.Util.positionOf( cSpan, pos );
				return l.getLines().get(pos);
			}
		}
		throw new IllegalStateException( "Position "+index+" not found");
	}

	public int indexOf(Object o)
	{			
		int idx;
		for (Chunk<Quad> l : lst)
		{
			ChunkSpan cSpan = new ChunkSpan( l );
			if (span.overlap( cSpan ))
			{
				int fromIndex = 0;
				int toIndex = cSpan.getLength();
				if (span.getStart()>cSpan.getStart())
				{
					fromIndex = span.getStart()-cSpan.getStart();
				}
				if (span.getEnd()<cSpan.getEnd())
				{
					toIndex -= (cSpan.getEnd()-span.getEnd());
				}
				List<Quad> lq = l.getLines().subList(fromIndex, toIndex);
				idx = lq.indexOf( o );
				if (idx >-1)
				{
					idx += fromIndex;
					return idx - span.getStart();
				}
			}
		}				
		return -1;
	}

	public boolean isEmpty()
	{
		return lst.isEmpty();
	}

	public Iterator<Quad> iterator()
	{
		return listIterator();
	}

	public int lastIndexOf(Object o)
	{
		for (int i=lst.size(); i>0;i--)
		{
			Chunk<Quad> chk = lst.get(i-1);
			ChunkSpan cSpan = new ChunkSpan( chk );
			if (span.overlap( cSpan ))
			{
				int fromIndex = 0;
				int toIndex = cSpan.getLength();
				if (span.getStart()>cSpan.getStart())
				{
					fromIndex = span.getStart()-cSpan.getStart();
				}
				if (span.getEnd()<cSpan.getEnd())
				{
					toIndex -= (cSpan.getEnd()-span.getEnd());
				}
				List<Quad> lq = chk.getLines().subList(fromIndex, toIndex);
				int idx = lq.lastIndexOf( o );
				if (idx >-1)
				{
					idx += fromIndex;
					return idx - span.getStart();
				}
			}			
		}
		return -1;
	}

	public ListIterator<Quad> listIterator()
	{
		return new QuadIterator();
	}

	public ListIterator<Quad> listIterator(int index)
	{
		return new QuadIterator(index);
	}

	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	public Quad remove(int index)
	{
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();		}

	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	public Quad set(int index, Quad element)
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		if (lst.isEmpty())
		{
			return 0;
		}
		return span.getLength();
	}

	public List<Quad> subList(int fromIndex, int toIndex)
	{
		return new SubList( lst, fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		Quad[] retval = new Quad[size()];
		for (int i=0;i<size();i++)
		{
			retval[i] = get(i);
		}
		return retval;
	}

	public <T> T[] toArray(T[] a)
	{
		if (a.length < size())
		{
			return (T[])toArray();
		}
		for (int i=0;i<size();i++)
		{
			a[i] = (T)get(i);
		}
		return a;
	}
	

	private class QuadIterator implements ListIterator<Quad> {
		
		private int idx;
		private ListIterator<Quad> iter;


		public QuadIterator(int index) {
			this.idx = -1;
			iter = null;
			if (index < span.getLength())
			{
				int cIdx = index+span.getStart();
				for (int i=0;i<lst.size();i++)
				{
					Chunk<Quad> cnk = lst.get(i);
					ChunkSpan cSpan = new ChunkSpan(cnk);					
					if (cSpan.contains( cIdx ))
					{
						this.iter = cnk.getLines().listIterator( cIdx );
						this.idx = i;
						return;
					}
				}
			}
		}
		
		public QuadIterator() {
			this( 0 );
		}

		public void forEachRemaining(Consumer action)
		{
			if (iter == null)
			{
				return;
			}
			ExtendedIterator<Quad> eIter = WrappedIterator.create( iter );
			for (int i=idx+1;i<lst.size();i++)
			{
				eIter.andThen( lst.get(i).getLines().listIterator());
			}
			
		}

		public boolean hasNext()
		{
			if (iter==null)
			{
				return false;
			}
			if (iter.hasNext())
			{
				return true;
			}
			idx++;
			iter = idx<lst.size()?lst.get(idx).getLines().listIterator():null;
			return hasNext();
		}

		public Quad next()
		{
			if (hasNext())
			{
				return iter.next();
			}
			throw new NoSuchElementException();
		}

		 public void remove() {
		        throw new UnsupportedOperationException("remove() is not supported");
		    }

		    public void set(Quad obj) {
		        throw new UnsupportedOperationException("set() is not supported");
		    }

		    public void add(Quad obj) {
		        throw new UnsupportedOperationException("add() is not supported");
		    }
			
			@Override
			public boolean hasPrevious()
			{
				
				if (iter != null)
				{
					if (iter.hasPrevious())
					{
						return true;
					}
				}
								
				if (lst.isEmpty())
				{
					return false;
				}
				for (int i=idx;i>0;i--)
				{
					Chunk<Quad> chk = lst.get(i);
					if (! chk.getLines().isEmpty())
					{
						return true;
					}
				}					
				return false;
			}

			@Override
			public int nextIndex()
			{
				if (iter==null)
				{
					return size();
				}
				if (iter.hasNext())
				{
					Chunk<Quad> chk = lst.get(idx);
					return chk.getPosition()+iter.nextIndex();
				} 
				if (idx+1 < lst.size())
				{
					Chunk<Quad> chk = lst.get(idx+1);
					chk.getPosition();
				}
				return size();
			}

			@Override
			public Quad previous()
			{
				if (hasPrevious())
				{
					if (iter != null)
					{
						return iter.previous();
					}
														
					for (int i=idx;i>0;i--)
					{
						Chunk<Quad> chk = lst.get(i);
						if (! chk.getLines().isEmpty())
						{
							iter = lst.get(i).getLines().listIterator();
							idx = i;
							while (iter.hasNext())
							{
								iter.next();
							}
							return iter.previous();
						}
					}					
				}
				throw new NoSuchElementException();
			}

			@Override
			public int previousIndex()
			{
				if (iter != null)
				{
					if (iter.hasPrevious())
					{
						return iter.previousIndex();
					}
				}
								
				if (lst.isEmpty())
				{
					return -1;
				}
				for (int i=idx;i>0;i--)
				{
					Chunk<Quad> chk = lst.get(i);
					if (! chk.getLines().isEmpty())
					{
						return chk.getPosition()+chk.size()-1;
					}
				}					
				return -1;
			}

	}
	
	//-----------------------------------------------------------------------
    /**
     * The sublist implementation for AbstractLinkedList.
     */
    protected static class SubList extends QuadList {
       
        protected SubList(final List<Chunk<Quad>> parent, final int fromIndex, final int toIndex) {
        	super( parent, fromIndex );
        	 if (toIndex > span.getEnd()) {
                 throw new IndexOutOfBoundsException("toIndex = " + toIndex);
             }
        	 if (fromIndex > toIndex) {
                 throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
             }
        	 this.span = SpanFactory.fromLength( span.getStart(), toIndex-fromIndex );
        }

        @Override
        public void add(final int index, final Quad obj) {
        	throw new UnsupportedOperationException();
        }

        @Override
        public Quad remove(final int index) {
        	throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends Quad> coll) {
        	throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends Quad> coll) {
        	throw new UnsupportedOperationException();
        }

        @Override
        public Quad set(final int index, final Quad obj) {
        	throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
        	throw new UnsupportedOperationException();
        }      

        @Override
        public List<Quad> subList(final int fromIndexInclusive, final int toIndexExclusive) {
            return new SubList(lst, fromIndexInclusive + span.getStart(), toIndexExclusive + span.getStart());
        }
       

//        protected void checkModCount() {
//            if (parent.modCount != expectedModCount) {
//                throw new ConcurrentModificationException();
//            }
//        }
    }
    
    private class ListSpan extends AbstractSpan {
    	
		@Override
		public int getStart()
		{
			return 0;
		}

		@Override
		public int getLength()
		{
			return Span.Util.calcLength(this);
		}

		@Override
		public int getEnd()
		{
			return lst.get( lst.size()-1).last();
		}
    }
    
    private class ChunkSpan extends AbstractSpan {
    	private Chunk<?> chunk;
    	
    	public ChunkSpan(Chunk<?> chunk)
    	{
    		this.chunk = chunk;
    	}
		@Override
		public int getStart()
		{
			return chunk.getPosition();
		}
		@Override
		public int getLength()
		{
			return Span.Util.calcLength(span);
		}
		@Override
		public int getEnd()
		{
			return chunk.last();
		}
    }
}
