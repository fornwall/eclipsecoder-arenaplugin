package net.fornwall.eclipsecoder.arena;

import javax.swing.JPanel;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.Renderer;

/**
 * The entry point used by the TopCoder competition applet.
 * 
 * The TopCoder arena ContestApplet loads this class and invokes certain methods
 * using reflection. This interface is thus only used for documentary purposes.
 * 
 * Below follows documentation obtained from:
 * http://www.topcoder.com/contest/classes/How%20to%20create%20an%20Editor%20Plugin%20v3.htm
 * 
 * Life Cycle of a Plugin
 * 
 * When the plugin is constructed depends on whether the user has specified to
 * construct at startup or �lazy�(ie when needed). If the user has specified at
 * startup � please see the startup discussion below. Otherwise � the plugin
 * will be constructed when first needed. When the user opens a problem � the
 * applet will check it's internal cache to see if the plugin has already been
 * constructed (either at startup time or from a prior problem and the plugin is
 * cacheable). If the plugin does exist in the internal cache, it will be
 * removed from the cache and step #3 is executed below. If it does NOT exist in
 * the internal cache � the following will occur.
 * 
 * 1) The constructor is called.
 * 
 * 2) The setName() method is called.
 * 
 * The following methods are called in the order below.
 * 
 * 1) The startUsing() method is called to notify the plugin is about to be
 * used.
 * 
 * 2) The getEditorPanel() method is called to construct the coding frame
 * 
 * 3) The setLanguage() method (DEPRECATED) is called to set the currently
 * chosen language.
 * 
 * 4) The setProblem() method (DEPRECATED) is called to set the problem
 * description.
 * 
 * 5) The setSignature() method (DEPRECATED) is called to set the problem�s
 * signature.
 * 
 * 6) The setProblemComponent() is called to set the problem component model
 * 
 * 7) The setSource() is called to set the initial source (if no initial source,
 * the setSource will be called with an empty string).
 * 
 * At this point � the user will work with the editor plugin and the following
 * methods may be called in response to user actions (ie pressing the buttons):
 * 
 * 1) The getSource() to retrieve the current source 2) The clear() to clear the
 * current source
 * 
 * If the user has either changed a setting (such as the selected language or
 * chosen editor) OR has opened the coding frame again (note: these methods will
 * NOT be called if the coding frame has been closed):
 * 
 * 1) The stopUsing() method to tell the plugin it will no longer be used.
 * 
 * 2) The isCacheable() method to determine if the plugin should be cached or
 * re-created for the next usage. If true, the plugin object is put into the
 * internal cache for future use. If false is returned, the dispose() method is
 * called and the object is dereferenced.
 * 
 * At this point � if the user has opened the coding frame again � the process
 * repeats with the startUsing() method mentioned above (or the constructor
 * point if the plugin wants to be recreated each time � ie returns false for
 * the isCacheable method).
 * 
 * <h1>Life Cycle at Startup</h1>
 * 
 * If the user has specified that the plugin should be constructed at startup �
 * then the following steps are executed immediately (in it�s own thread)
 * following the showing of the main frame:
 * 
 * 1) The plugin is constructed
 * 
 * 2) The setName() method is called
 * 
 * 3) The startUsing() method is called
 * 
 * 4) The stopUsing() method is called
 * 
 * 5) The isCacheable() method is called
 * 
 * If the isCacheable() method return true � then the plugin object is stored in
 * an internal cache for use when a problem is opened. If false is returned, the
 * dispose() method is called and the object is dereferenced.
 * 
 * <h1>Life Cycle Various Times</h1>
 * 
 * There are several other scenarios where the plugin will be called
 * (installing, configuring, verifying, and uninstalling) that generally follow
 * these steps:
 * 
 * 1) The plugin is constructed
 * 
 * 2) The setName() method is called
 * 
 * 3) The install()/configure()/uninstall() method may be called (or this step
 * can be skipped if the applet is simply verifying the plugin can be
 * instantiated)
 * 
 * 4) The dispose() method is called and the object is dereferenced.
 */
public abstract class EntryPoint {
	protected String name;

	/**
	 * This method is called to clear the current source code from the editor.
	 * This method will be called between opening problems to clear the prior
	 * problems source code.
	 */
	public void clear() {
		// do nothing as default
	}

	/**
	 * From the TopCoder entry point API.
	 * 
	 * Should bring up a modal configuration dialog to set options (if plugin
	 * supports options).
	 * 
	 * This method of configuration is not supported in EclipseCoder, instead
	 * all configuration should be done through Eclipse preference pages.
	 */
	public void configure() {
		// do nothing by default
	}

	/**
	 * Tells the plugin that the plugin object will no longer be called or
	 * referenced (ie can be garbage collected). See plugin life-cycle below for
	 * information as to when this method is being called.
	 */
	public void dispose() {
		// do nothing as default
	}

	/**
	 * Return the JPanel which will be embedded in the TopCoder contest applet.
	 * 
	 * @return The editor panel.
	 */
	public abstract JPanel getEditorPanel();

	/**
	 * Return the source code of the solution.
	 * 
	 * @return The source code that is to be submitted.
	 */
	public abstract String getSource();

	/**
	 * This method is called ONCE (per instance) when the plugin is being
	 * installed. This gives the plugin a chance to setup (or request) the
	 * plugin when it is installed. The plugin can throw an runtime exception to
	 * prevent the installation of the plugin if a critical error occurs. See
	 * plugin life-cycle below for information as to when this method is being
	 * called.
	 */
	public void install() {
		// do nothing by default
	}

	/**
	 * @return Whatever the plugin is cacheable, or if a new instance is to be
	 *         created at every opened problem.
	 */
	public abstract boolean isCacheable();

	/**
	 * Tells the plugin the UNIQUE name that was given to it. This is useful to
	 * the plugin to denote each unique instance of the plugin that the user has
	 * setup (so that plugin configuration information can be kept unique per
	 * instance). Is called just after the construction time.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param componentModel
	 *            The problem statement.
	 * @param language
	 *            The programming language used by the competant.
	 * @param renderer
	 *            Interface for rendering an element (like a problem component)
	 *            to HTML or plain text. The underlying renderer has already
	 *            been linked to the appropriate element. Simply call the toHTML
	 *            method with the appropriate language to render to.
	 */
	public abstract void setProblemComponent(
			ProblemComponentModel componentModel, Language language,
			Renderer renderer);

	/**
	 * Called when a new problem is opened and the TopCoder server has stored
	 * old solution code.
	 * 
	 * @param source
	 *            The source supplied by the TopCoder (could be previous code).
	 */
	public abstract void setSource(String source);

	/**
	 * Tells the plugin that the plugin is about to be used. See plugin
	 * life-cycle above for information as to when this method is being called.
	 */
	public abstract void startUsing();

	/**
	 * Tells the plugin that the plugin will not be used until the next
	 * startUsing() method call. See plugin life-cycle below for information as
	 * to when this method is being called.
	 */
	public abstract void stopUsing();

	/**
	 * From the TopCoder entry point API.
	 * 
	 * This method is called ONCE (per instance) when the plugin is being
	 * uninstalled. This gives the plugin a chance to cleanup any resources that
	 * were used. See plugin life-cycle below for information as to when this
	 * method is being called.
	 */
	public void uninstall() {
		// do nothing by default
	}

}