package org.asf.cyan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipInputStream;

import org.asf.cyan.api.common.CyanComponent;
import org.asf.cyan.fluid.Fluid;
import org.asf.cyan.fluid.FluidAgent;
import org.asf.cyan.fluid.Transformer.AnnotationInfo;
import org.asf.cyan.fluid.api.FluidTransformer;
import org.asf.cyan.fluid.bytecode.FluidClassPool;
import org.objectweb.asm.tree.ClassNode;

public class FFBMain extends CyanComponent {

	public static void main(String[] args) throws IOException, ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		File proxy = new File("proxy.jar");
		if (!proxy.exists()) {
			System.err.println("Missing proxy.jar!");
			System.err.println(
					"Please download a 'Waterfall', 'BungeeCord' or 'Hexagon BungeeCord' jar file and name it proxy.jar.");
			System.err.println("Nothing to do, exiting.");
			System.exit(1);
		}

		FFBMainComponent.enableLog();

		info("Starting Fluid For Bungee... Initializing CyanComponents...");
		FFBMainComponent.initializeComponents();

		HashMap<URL, FluidClassPool> pools = new HashMap<URL, FluidClassPool>();
		info("Discovering transformers...");
		info("Scanning plugin and module files...");

		File plugins = new File("plugins");
		File modules = new File("modules");
		scan(pools, plugins);
		scan(pools, modules);

		info("Scanning loaded jars, finding @FluidTransformer classes...");
		pools.forEach((url, pool) -> {
			for (ClassNode nd : pool.getLoadedClasses()) {
				debug("Scanning " + nd.name + "...");
				if (AnnotationInfo.isAnnotationPresent(FluidTransformer.class, nd)) {
					try {
						Fluid.registerTransformer(nd.name.replace("/", "."), url);
					} catch (IllegalStateException | ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		info("Initializing the FLUID agent...");
		FluidAgent.initialize();

		info("Starting bungeecord...");
		JarFile file = new JarFile(proxy);
		Manifest mf = file.getManifest();
		Attributes attr = mf.getMainAttributes();
		String main = attr.getValue("Main-Class");
		file.close();
		FluidAgent.addToClassPath(proxy);
		Class<?> mainCls = Class.forName(main);
		mainCls.getMethod("main", String[].class).invoke(null, new Object[] { args });
	}

	private static void scan(HashMap<URL, FluidClassPool> pools, File plugins) throws IOException {
		info("Scanning folder: " + plugins.getPath());
		for (File f : plugins.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				FluidClassPool pool = FluidClassPool.createEmpty();

				info("Importing " + f.getPath() + "...");
				FileInputStream strm = new FileInputStream(f);
				ZipInputStream zip = new ZipInputStream(strm);
				pool.importArchive(zip);
				zip.close();
				strm.close();

				pools.put(f.toURI().toURL(), pool);
			}
		}
		for (File f : plugins.listFiles()) {
			if (f.isDirectory()) {
				scan(pools, f);
			}
		}
	}

}
