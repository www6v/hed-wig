/**
 * 
 */
package com.yhd.arch.laserbeak.spi;

import com.yhd.arch.spi.IWriteService;

/**
 * @author root
 *
 */
public class WriteService implements IWriteService<String> {

	@Override
	public void writeData(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean writeAndResponse(String data) {
		return true;
	}

}
