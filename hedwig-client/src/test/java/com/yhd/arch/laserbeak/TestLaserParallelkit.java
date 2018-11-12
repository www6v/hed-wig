/**
 *
 */
package com.yhd.arch.laserbeak;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import scala.concurrent.Future;

import com.yhd.arch.photon.common.parallel.MultiCall;
import com.yhd.arch.photon.common.parallel.ParallelToolkit;
import com.yhd.arch.photon.common.parallel.SingleCall;
import com.yhd.arch.photon.core.RemoteResponse;
import com.yihaodian.architecture.hedwig.hessian.client.ExampleService;
import com.yihaodian.architecture.hedwig.hessian.client.IQueryService;
import com.yihaodian.architecture.hedwig.hessian.client.Parameters;
import com.yihaodian.architecture.hedwig.hessian.client.Result;

/**
 * @author root
 */
public class TestLaserParallelkit extends TestBase {

	public static void testSingle() throws Throwable {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "client_context.xml" });
		final ExampleService<Result, Parameters> service = (ExampleService<Result, Parameters>) context.getBean("exampleHttpService");
		final IQueryService queryService = (IQueryService) context.getBean("queryProxy");
		Future<RemoteResponse> f = ParallelToolkit.createScalaFuture(new SingleCall() {

			@Override
			public void call() throws Throwable {
				queryService.queryStrings(1000l, s);
			}

		});

		Result r1 = (Result) ParallelToolkit.getResult(f, 100);
		System.out.println("###-----------" + r1.getValue());

	}

	public static void testMultiCall() throws Throwable {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "client_context.xml" });
		final ExampleService<Result, Parameters> service = (ExampleService<Result, Parameters>) context.getBean("exampleHttpService");
		final IQueryService queryService = (IQueryService) context.getBean("queryProxy");
		final Parameters p = createParam();
		final Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, List<Future<RemoteResponse>>> fMap = ParallelToolkit.createScalaFuture(new MultiCall<RemoteResponse>() {

			@Override
			public void call() throws Throwable {
				service.execute(p);
				service.execute(p);
				String resultLocal = requestLocalService();
				resultMap.put("requestLocalService", resultLocal);
				queryService.queryStrings(1000l, s);
			}

		});
		System.out.println(fMap.keySet());
		Result r1 = (Result) ParallelToolkit.getResult(fMap.get("baseHessianService.execute_Object").get(0), 1000);
		System.out.println("====================Result1:" + r1.getValue());
		Result r2 = (Result) ParallelToolkit.getResult(fMap.get("baseHessianService.execute_Object").get(1), 1000);
		System.out.println("====================Result2:" + r2.getValue());
		Result r3 = (Result) ParallelToolkit.getResult(fMap.get("queryService.queryStrings_long_string").get(0), 1000);
		System.out.println("====================Result3:" + r3.getValue());
	}

	public static void testMixCall() throws Throwable {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "client_context.xml" });
		final ExampleService<Result, Parameters> service = (ExampleService<Result, Parameters>) context.getBean("exampleHttpService");
		final IQueryService queryService = (IQueryService) context.getBean("queryProxy");
		final Parameters p = createParam();
		Future<RemoteResponse> f = ParallelToolkit.createScalaFuture(new SingleCall() {

			@Override
			public void call() throws Throwable {
				queryService.queryStrings(1000l, s);
			}

		});
		Map<String, List<Future<RemoteResponse>>> fMap = ParallelToolkit.createScalaFuture(new MultiCall<RemoteResponse>() {

			@Override
			public void call() throws Throwable {
				service.execute(p);
				queryService.queryStrings(1000l, s);
			}

		});
		System.out.println("keys=" + fMap.keySet());
		Result r1 = (Result) ParallelToolkit.getResult(f, 100);
		System.out.println(r1.getValue());

		Result r3 = (Result) ParallelToolkit.getResult(fMap.get("baseHessianService.execute_Object").get(0), 100);
		System.out.println(r3.getValue());
		Result r2 = (Result) ParallelToolkit.getResult(fMap.get("queryService.queryStrings_long_string").get(0), 100);
		System.out.println(r2.getValue());
	}

	private static String requestLocalService() {
		return "hello world!!";
	}
}
