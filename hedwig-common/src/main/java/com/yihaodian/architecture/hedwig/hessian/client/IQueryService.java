/**
 * 
 */
package com.yihaodian.architecture.hedwig.hessian.client;

import java.util.List;

/**
 * @author jianglie
 *
 */
public interface IQueryService {

	public Result<List<String>> queryStrings(Long userId,String condition) throws Exception;
}
