package net.fornwall.eclipsecoder.arena;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.fornwall.eclipsecoder.languages.CreatedProject;
import net.fornwall.eclipsecoder.languages.LanguageSupport;
import net.fornwall.eclipsecoder.languages.LanguageSupportFactory;
import net.fornwall.eclipsecoder.stats.ProblemStatement;
import net.fornwall.eclipsecoder.util.Utilities;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.CPPLanguage;
import com.topcoder.shared.language.CSharpLanguage;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.language.PythonLanguage;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Renderer;
import com.topcoder.shared.problem.TestCase;

public class EclipseCoderEntryPoint extends EntryPoint {

	private static final Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();

	static {
		classMap.put("char", Character.class);
		classMap.put("char[]", Character[].class);
		classMap.put("int", Integer.class);
		classMap.put("int[]", Integer[].class);
		classMap.put("long", Long.class);
		classMap.put("long[]", Long[].class);
		classMap.put("double", Double.class);
		classMap.put("double[]", Double[].class);
		classMap.put("String", String.class);
		classMap.put("String[]", String[].class);
	}

	/** Used in the conversion to ProblemStatement */
	private static Class<?> toClass(DataType dataType) throws Exception {
		String className = dataType.getDescriptor(JavaLanguage.class.newInstance());

		if (classMap.containsKey(className)) {
			return classMap.get(className);
		}

		throw new IllegalArgumentException("Unknown datatype: " + className);
	}

	/**
	 * Transforms a TopCoder {@link ProblemComponentModel} to an EclipseCoder
	 * {@link ProblemStatement}.
	 * 
	 * @param problem
	 *            the TopCoder problem statement
	 * @param renderer
	 *            the TopCoder Renderer
	 * @return the converted ProblemStatement object
	 */
	private static ProblemStatement transformToProblemStatement(ProblemComponentModel problem, Language language,
			Renderer renderer) throws Exception {
		ProblemStatement result = new ProblemStatement();
		result.setSolutionClassName(problem.getClassName());
		result.setSolutionMethodName(problem.getMethodName());
		Class<?> returnType = toClass(problem.getReturnType());
		result.setReturnType(returnType);

		List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
		for (DataType dataType : problem.getParamTypes()) {
			parameterTypes.add(toClass(dataType));
		}
		result.setParameterTypes(parameterTypes);
		result.setParameterNames(Arrays.asList(problem.getParamNames()));

		List<ProblemStatement.TestCase> testCases = new ArrayList<ProblemStatement.TestCase>();
		for (TestCase testCase : problem.getTestCases()) {
			Object output = ProblemStatement.parseType(returnType, testCase.getOutput());
			Object[] input = new Object[testCase.getInput().length];
			for (int i = 0; i < input.length; i++) {
				input[i] = ProblemStatement.parseType(parameterTypes.get(i), testCase.getInput()[i]);
			}
			testCases.add(new ProblemStatement.TestCase(output, input));
		}
		result.setTestCases(testCases);

		String html = renderer.toHTML(language);
		if (html.indexOf("<title>") == -1) {
			html = html.replaceFirst("<html>", "<html><head><title>" + result.getClassName() + "</title></head>");
		}
		result.setHtmlDescription(html);

		result.setContestName(problem.getProblem().getRound().getContestName() + " - " + problem.getPoints().intValue()
				+ " points");

		return result;
	}

	/**
	 * The JPanel given to the ContestApplet in getEditorPanel(). It is the
	 * panel that is displayed inside the CodingFrame.
	 */
	JPanel editorPanel = new JPanel(new GridBagLayout());

	LanguageSupport languageSupport;

	JTextArea logArea = new JTextArea();

	/**
	 * Due to the isCacheable() method returning true, only one instance of this
	 * class should ever be created by the TopCoder contest applet.
	 */
	public EclipseCoderEntryPoint() {
		editorPanel.setForeground(Color.WHITE);
		editorPanel.setBackground(Color.BLACK);

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = gc.weighty = 1.0;

		logArea.setEditable(false);
		logArea.setForeground(Color.WHITE);
		logArea.setBackground(Color.BLACK);
		editorPanel.add(new JScrollPane(logArea), gc);
	}

	public void appendLogMessage(final String message) {
		Runnable runnable = new Runnable() {
			public void run() {
				// logArea.append(dateFormat.format(new Date()) + " " + message
				// + "\n");
				logArea.setText(message + "\n");
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

	/**
	 * @see net.fornwall.eclipsecoder.arena.EntryPoint#getEditorPanel()
	 */
	@Override
	public JPanel getEditorPanel() {
		return editorPanel;
	}
	@Override
	public String getSource() {
		try {
			if (languageSupport == null) {
				return null;
			}

			String source = null;
			try {
				source = languageSupport.getSubmissionOutsideThread();
			} catch (Exception e) {
				Utilities.showException(e);
			}
			// do not return null as compile dialog stays in that case
			return source == null ? "" : source;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isCacheable() {
		return true;
	}

	// called using reflection from core eclipse plug-in
	public void loadClasses() {
		LanguageSupport.class.getName();
		LanguageSupportFactory.class.getName();
		ProblemStatement.class.getName();
		ProblemStatement.TestCase.class.getName();
		Utilities.class.getName();
	}

	/**
	 * @see net.fornwall.eclipsecoder.arena.EntryPoint#setProblemComponent(com.topcoder.client.contestant.ProblemComponentModel,
	 *      com.topcoder.shared.language.Language,
	 *      com.topcoder.shared.problem.Renderer)
	 */
	@Override
	public void setProblemComponent(ProblemComponentModel componentModel, Language language, Renderer renderer) {
		try {
			logArea.setText("");
			appendLogMessage("Sending problem statement to Eclipse");

			String languageName = null;
			if (language instanceof CPPLanguage) {
				languageName = LanguageSupport.LANGUAGE_NAME_CPP;
			} else if (language instanceof JavaLanguage) {
				languageName = LanguageSupport.LANGUAGE_NAME_JAVA;
			} else if (language instanceof CSharpLanguage) {
				languageName = LanguageSupport.LANGUAGE_NAME_CSHARP;
			} else if (language instanceof PythonLanguage) {
				languageName = LanguageSupport.LANGUAGE_NAME_PYTHON;
			} else {
				languageSupport = null;
				appendLogMessage("EclipseCoder does not support the "
						+ language.getName()
						+ " programming language.\n"
						+ "Change to one of the supported languages if you want to use EclipseCoder or switch to another editor.");
				return;
			}

			languageSupport = LanguageSupportFactory.createLanguageSupport(languageName);

			if (languageSupport == null) {
				appendLogMessage("No plugin support for the "
						+ language.getName()
						+ " programming language found.\n"
						+ "Install the appropriate plugin from the EclipseCoder update site if you want to use EclipseCoder with this language.");
				return;
			} else {
				final ProblemStatement problemStatement = transformToProblemStatement(componentModel, language,
						renderer);

				Utilities.runInDisplayThread(new Runnable() {
					public void run() {
						final StringBuffer logString = new StringBuffer();

						final CreatedProject description = languageSupport.createProject(problemStatement);
						if (description == null) {
							logString.append("The project could not be generated");
						} else {
							logString.append("The submission is available in the file " + description.getDescription());
						}
						appendLogMessage(logString.toString());
					}
				});
			}
		} catch (Exception e) {
			Utilities.showException(e);
		}
	}

	/**
	 * @see net.fornwall.eclipsecoder.arena.EntryPoint#setSource(java.lang.String)
	 */
	@Override
	public void setSource(String source) {
	}

	/**
	 * @see net.fornwall.eclipsecoder.arena.EntryPoint#startUsing()
	 */
	@Override
	public void startUsing() {
		SwingUtilities.updateComponentTreeUI(editorPanel);
	}

	/**
	 * @see net.fornwall.eclipsecoder.arena.EntryPoint#stopUsing()
	 */
	@Override
	public void stopUsing() {
	}

}