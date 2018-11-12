package com.yhd.arch.spi;

public interface IWriteService<D> {

	public void writeData(D data);
	
	public boolean writeAndResponse(D data); 
}
