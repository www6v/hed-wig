/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.hash;

import java.util.HashMap;
import java.util.Map;

import com.yihaodian.architecture.hedwig.common.constants.InternalConstants;
import com.yihaodian.architecture.hedwig.common.exception.HedwigException;
import com.yihaodian.architecture.hedwig.common.exception.InvalidParamException;

/**
 * @author root
 *
 */
public class HashFunctionFactory {

	private static HashFunctionFactory hfFactory = new HashFunctionFactory();

	public Map<String, HashFunction> functionMap = new HashMap<String, HashFunction>();

	private HashFunctionFactory() {
		super();
		functionMap.put(InternalConstants.HASH_FUNCTION_MUR2, new Murmur2());
	}

	public static HashFunctionFactory getInstance() {
		return hfFactory;
	}

	public HashFunction getMur2Function() {
		HashFunction f = null;
		try {
			f = getHashFunction(InternalConstants.HASH_FUNCTION_MUR2);
		} catch (HedwigException e) {
			e.printStackTrace();
		}
		return f;
	}

	public HashFunction getHashFunction(String key) throws HedwigException {
		if (key == null)
			throw new InvalidParamException("Hash function key must not null!!!");
		if (functionMap.containsKey(key)) {
			return functionMap.get(key);
		} else {
			throw new HedwigException("Hash function key:" + key + " is not support");
		}
	}
}
