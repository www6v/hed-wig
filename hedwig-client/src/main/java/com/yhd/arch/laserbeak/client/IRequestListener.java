package com.yhd.arch.laserbeak.client;

import java.lang.reflect.Method;

/**
 * Created by root on 9/8/15.
 */
public interface IRequestListener {

    public void before(Object proxy, Method method, Object[] args) throws Exception;

    public void after(Object proxy, Method method, Object[] args) throws Exception;

}
