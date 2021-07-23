package br.ufes.inf.haasim.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
 
public class Activator implements BundleActivator {
 
    private static BundleContext context;
    @SuppressWarnings("rawtypes")
	private ServiceTracker httpServiceTracker;
 
    static BundleContext getContext() {
        return context;
    }
 
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
         
        httpServiceTracker = new HttpServiceTracker(context);
        httpServiceTracker.open();
    }
 
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
         
        if (httpServiceTracker != null) {
            httpServiceTracker.close();
        }
        httpServiceTracker = null;
    }
 
}
