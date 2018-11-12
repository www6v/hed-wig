/**
 * 
 */
package com.yhd.laserbeak.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yhd.arch.laserbeak.provider.SpringServiceExporter;

/**
 * @author root
 * 
 */
public class TestLaserServerExportMultiService {
	// private static String envPath =
	// "/home/root/cloudDriver/source/work/envConfig";

	private static String envPath = "/Users/root/Applications/work/envConfig";

	public static void main(String[] args) throws Exception {
		System.setProperty("global.config.path", "D:\\root\\cloudDriver\\source\\work");
		final ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "laserbeak-MultiExporter.xml" });
		SpringServiceExporter r = (SpringServiceExporter) context.getBean("queryService");

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					SpringServiceExporter r = (SpringServiceExporter) context.getBean("queryService");
					r.destroy();
					SpringServiceExporter w = (SpringServiceExporter) context.getBean("writeService");
					w.destroy();
					System.out.println("Trigger shutdown hook!!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}));
		// while (true) {
		// Thread.currentThread().sleep(30000);
		// Random random = new Random();
		// int weight = random.nextInt(10);
		// System.out.println("Change weight to :" + weight);
		// r.changeServWeight(weight);
		// }
	}
}
