package org.xenei.rdf_diff_patch;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class TransformingList<F, T> extends AbstractList<T>
{
	final List<F> fromList;
	final Function<? super F, ? extends T> function;

	public static <T> T checkNotNull(T reference)
	{
		if (reference == null)
		{
			throw new NullPointerException();
		}
		return reference;
	}

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
