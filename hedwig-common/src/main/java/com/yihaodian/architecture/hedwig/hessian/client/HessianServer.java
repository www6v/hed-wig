/**
 * 
 */
package com.yihaodian.architecture.hedwig.hessian.client;

/**
 * @author jianglie
 *
 */
public interface HessianServer<R,P> {

	public R execute(P p);
}
