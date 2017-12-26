package org.xenei.rdf_diff_patch;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

/**
 * Class to that uses a function to transform a list to a list of a differnt type 
 * of objec.t
 *
 * @param <F> the from object as found in the original list.
 * @param <T> the to object that this list holds.
 */
public class TransformingList<F, T> extends AbstractList<T>
{
	final List<F> fromList;
	final Function<? super F, ? extends T> function;

	/**
	 * Check that a reference is not null.
	 * @param reference the reference to check.
	 * @return the reference.
	 * @throws NullPointerException if the reference is null.
	 */
	public static <T> T checkNotNull(T reference)
	{
		if (reference == null)
		{
			throw new NullPointerException();
		}
		return reference;
	}

	/**
	 * Constructor.
	 * @param fromList the original list.
	 * @param function the function to transform objects.
	 */
	public TransformingList(List<F> fromList,
			Function<? super F, ? extends T> function)
	{
		this.fromList = checkNotNull(fromList);
		this.function = checkNotNull(function);
	}

	@Override
	public T get(int index)
	{
		return function.apply(fromList.get(index));
	}

	@Override
	public int size()
	{
		return fromList.size();
	}
}
