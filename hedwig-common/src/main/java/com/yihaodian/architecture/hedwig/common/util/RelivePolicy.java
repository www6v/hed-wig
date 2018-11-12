/**
 * 
 */
package com.yihaodian.architecture.hedwig.common.util;

/**
 * @author root
 *
 */
public interface RelivePolicy {

	public boolean tryRelive();

	public void reset();
}
