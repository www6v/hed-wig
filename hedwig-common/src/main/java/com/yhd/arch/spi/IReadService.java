package com.yhd.arch.spi;

import java.util.Collection;

public interface IReadService<D,P> {

	public D readObject(P p);
	
	public Collection<D> readCollection(P p,Integer size);
}
