package com.u2apple.testman.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.u2apple.testman.util.StringUtils;

public class TestCaseRefactor {
	private static final int TEST_CASE_COUNT = 4;

	public void extractMethodByBrand() throws JavaModelException,
			MalformedTreeException, BadLocationException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("shuame");
		IJavaProject javaProject = JavaCore.create(project);
		IFolder folder = project.getFolder("src/test/java");
		IPackageFragmentRoot srcFolder = javaProject
				.getPackageFragmentRoot(folder);
		IPackageFragment packageFragment = srcFolder
				.getPackageFragment("com.shuame.api.store");

		ICompilationUnit icu = packageFragment
				.getCompilationUnit("DeviceStoreTest.java");
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);

		CompilationUnit compilationUnit = (CompilationUnit) parser
				.createAST(null);

		// Get the first class.
		TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit
				.types().get(0);

		ASTRewrite rewrite = ASTRewrite.create(compilationUnit.getAST());
		ListRewrite listRewrite = rewrite.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		Map<String, List<MethodDeclaration>> methodsByBrand = methodsGroupByBrand(typeDeclaration);

		for (Entry<String, List<MethodDeclaration>> entry : methodsByBrand
				.entrySet()) {
			if (entry.getValue().size() >= TEST_CASE_COUNT) {
				try {
					IPackageFragment devicePackageFragment = srcFolder
							.getPackageFragment("com.shuame.api.store.device");
					newClass(parser, devicePackageFragment, entry);
				} catch (JavaModelException | MalformedTreeException
						| BadLocationException e) {
					e.printStackTrace();
				}

				// delete original method.
				for (MethodDeclaration method : entry.getValue()) {
					listRewrite.remove(method, null);
				}

			}
		}

		// get the current document source
		final Document document = new Document(icu.getSource());
		TextEdit edits = rewrite.rewriteAST(document, icu.getJavaProject()
				.getOptions(true));

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		// update of the compilation unit
		icu.getBuffer().setContents(newSource);
	}

	private Map<String, List<MethodDeclaration>> methodsGroupByBrand(
			TypeDeclaration typeDeclaration) {
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		Map<String, List<MethodDeclaration>> methodsByBrand = new HashMap<>();
		for (MethodDeclaration method : methods) {
			String methodName = method.getName().toString();
			if (methodName.contains("_")) {
				String[] names = methodName.split("_");
				if (names.length == 2) {
					String brand = names[0];
					if (methodsByBrand.containsKey(brand)) {
						methodsByBrand.get(brand).add(method);
					} else {
						List<MethodDeclaration> methodList = new ArrayList<>();
						methodList.add(method);
						methodsByBrand.put(brand, methodList);
					}
				}
			}
		}
		return methodsByBrand;
	}

	private void newClass(ASTParser parser,
			IPackageFragment devicePackageFragment,
			Entry<String, List<MethodDeclaration>> entry)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		String src = createSource(createClassName(entry.getKey()),
				entry.getValue());
		devicePackageFragment.createCompilationUnit(
				createClassFileName(entry.getKey()), src, false, null);
	}

	private String createSource(String className,
			List<MethodDeclaration> methods) {
		StringBuilder sourceBuilder = new StringBuilder();
		sourceBuilder.append("package com.shuame.api.store.device;");
		sourceBuilder.append("\n");
		sourceBuilder.append("import org.junit.Assert;");
		sourceBuilder.append("\n");
		sourceBuilder.append("import org.junit.Test;");
		sourceBuilder.append("\n");
		sourceBuilder.append("import com.shuame.api.domain.DeviceInitParam;");
		sourceBuilder.append("\n");
		sourceBuilder.append("import com.shuame.api.store.DeviceStore;");
		sourceBuilder.append("\n");
		sourceBuilder.append("public class ");
		sourceBuilder.append(className);
		sourceBuilder.append("{");
		for (MethodDeclaration method : methods) {
			sourceBuilder.append(method.toString());
		}
		sourceBuilder.append("}");
		return sourceBuilder.toString();
	}

	private String createClassFileName(String brand) {
		return StringUtils.toCapital(brand) + "DeviceStoreTest.java";
	}

	private String createClassName(String brand) {
		return StringUtils.toCapital(brand) + "DeviceStoreTest";
	}

}
