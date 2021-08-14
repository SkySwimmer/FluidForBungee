package org.asf.cyan;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.cyan.fluid.implementation.CyanBytecodeExporter;
import org.asf.cyan.fluid.implementation.CyanReportBuilder;
import org.asf.cyan.fluid.implementation.CyanTransformer;
import org.asf.cyan.fluid.implementation.CyanTransformerMetadata;

public class FFBMainComponent extends CyanComponent {

	private static boolean prepared = false;
	private static FFBMainComponent impl;

	public static void simpleInit() {
		if (prepared)
			return;

		impl = new FFBMainComponent();
		impl.assignImplementation();

		prepared = true;
	}

	private static boolean init = false;

	public static boolean isInitialized() {
		return init;
	}

	protected static void initComponent() {
		init = true;
	}

	@Override
	protected void setupComponents() {
		if (init)
			throw new IllegalStateException("Cyan components have already been initialized.");
		if (LOG == null)
			initLogger();
	}

	@Override
	protected void preInitAllComponents() {
		trace("INITIALIZE all components, caller: " + CallTrace.traceCallName());
		trace("CREATE ConfigurationBuilder instance, caller: " + CallTrace.traceCallName());
	}

	@Override
	protected void finalizeComponents() {
	}

	@Override
	protected Class<?>[] getComponentClasses() {
		return new Class<?>[] { CyanTransformer.class, CyanTransformerMetadata.class, CyanBytecodeExporter.class,
				FFBMainComponent.class, CyanReportBuilder.class };
	}

	public static void initializeComponents() throws IllegalStateException {
		simpleInit();
		impl.initializeComponentClasses();
	}

	public static void enableLog() {
		simpleInit();
		if (LOG == null)
			initLogger();
		trace(CallTrace.traceCallName() + " set the log level to INFO.");
		Configurator.setLevel("CYAN", Level.INFO);
	}

}
