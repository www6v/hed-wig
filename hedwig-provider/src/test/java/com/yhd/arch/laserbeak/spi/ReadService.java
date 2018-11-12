/**
 * 
 */
package com.yhd.arch.laserbeak.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yhd.arch.spi.DataCreator;
import com.yhd.arch.spi.IReadService;

/**
 * @author root
 *
 */
public class ReadService implements IReadService<String, Integer> {

	Map<Integer, String> sizeObjMap = new HashMap<Integer, String>();

	@Override
	public String readObject(Integer p) {
		checkParameter("size", p);
		// try {
		// Thread.sleep(1);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return getString(p);
	}

	@Override
	public Collection<String> readCollection(Integer p, Integer limit) {
		checkParameter("size", p);
		checkParameter("limit", limit);
		List<String> l = new ArrayList<String>();
		String s = getString(p);
		for (int i = 0; i < limit; i++) {
			l.add(s);
		}
		return l;
	}

	private String getString(Integer size) {
		String v = null;
		if (sizeObjMap.containsKey(size)) {
			v = sizeObjMap.get(size);
		} else {
			v = DataCreator.createByteObject(size);
			sizeObjMap.put(size, v);
		}
		return v;
	}

	private void checkParameter(String comments, Integer p) {
		if (p == null || p.intValue() <= 0) {
			throw new IllegalArgumentException(comments + ":" + p + "");
		}
	}

}
