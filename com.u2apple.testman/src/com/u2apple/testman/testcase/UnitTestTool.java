package com.u2apple.testman.testcase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.gson.Gson;
import com.u2apple.testman.constant.Constants;
import com.u2apple.testman.model.AndroidDevice;
import com.u2apple.testman.util.RefectUtils;

public class UnitTestTool {
	/**
	 * This ICompilationUnit is a working copy.
	 */
	private ICompilationUnit icomilationUnit;

	private static final List<String> EXCLUDED_FIELDS = Arrays
			.asList(new String[] { "vids", "vid", "productId",
					"returnProductId" });

	public UnitTestTool(ICompilationUnit icomilationUnit) {
		this.icomilationUnit = icomilationUnit;
	}

	public void generateTestCase() throws IOException, JavaModelException,
			MalformedTreeException, BadLocationException {
		Gson gson = new Gson();
		Path path = Paths.get(System.getProperty("user.home"),
				Constants.ANDROID_DEVICES_JSON_FILE);
		byte[] bytes = Files.readAllBytes(path);
		AndroidDevice[] androidDevices = gson.fromJson(new String(bytes),
				AndroidDevice[].class);
		if (androidDevices != null) {
			for (AndroidDevice androidDevice : androidDevices) {
				if (hasMethod(productIdToMethodName(androidDevice
						.getProductId()))) {
					updateTestCase(androidDevice);
				} else {
					addTestCase(androidDevice);
				}

			}
			// Commit changes
			icomilationUnit.commitWorkingCopy(false, null);
			// Destroy working copy
			icomilationUnit.discardWorkingCopy();
			Files.write(path, "".getBytes(),
					StandardOpenOption.TRUNCATE_EXISTING);
		}

	}

	/**
	 * Add test case method using ASTRewrite.
	 * 
	 * @param androidDevice
	 * @throws IOException
	 * @throws JavaModelException
	 * @throws MalformedTreeException
	 * @throws BadLocationException
	 */
	@SuppressWarnings("deprecation")
	private void addTestCase(AndroidDevice androidDevice) throws IOException,
			JavaModelException, MalformedTreeException, BadLocationException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(this.icomilationUnit);

		CompilationUnit compilationUnit = (CompilationUnit) parser
				.createAST(null);

		// Get the first class.
		TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit
				.types().get(0);

		AST ast = compilationUnit.getAST();
		// creation of ASTRewrite
		ASTRewrite rewrite = ASTRewrite.create(ast);

		MethodDeclaration methodDeclaration = createMethodDeclaration(
				androidDevice, ast, rewrite);

		// Insert methodDeclaration.
		ListRewrite listRewrite = rewrite.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertLast(methodDeclaration, null);

		// Get the current document source.
		final Document document = new Document(this.icomilationUnit.getSource());
		TextEdit edits = rewrite.rewriteAST(document, this.icomilationUnit
				.getJavaProject().getOptions(true));

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();
		// update of the compilation unit
		this.icomilationUnit.getBuffer().setContents(newSource);
		// Commit changes
		// icomilationUnit.commitWorkingCopy(false, null);
		// Destroy working copy
		// icomilationUnit.discardWorkingCopy();
	}

	@SuppressWarnings("unchecked")
	private MethodDeclaration createMethodDeclaration(
			AndroidDevice androidDevice, AST ast, ASTRewrite astRewrite) {
		// Construct method declaration.
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		// Add jUnit test annotation.
		MarkerAnnotation testAnnotation = ast.newMarkerAnnotation();
		testAnnotation.setTypeName(ast.newSimpleName("Test"));
		methodDeclaration.modifiers().add(testAnnotation);
		// Add public modifier.
		methodDeclaration.modifiers().add(
				ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		// Set method name.
		methodDeclaration.setName(ast
				.newSimpleName(productIdToMethodName(androidDevice
						.getProductId())));

		// Method body.
		Block block = ast.newBlock();
		methodDeclaration.setBody(block);

		int index = 0;
		// Test each VID.
		for (String vid : androidDevice.getVids()) {
			StringBuilder commentBuilder = new StringBuilder();
			commentBuilder.append("//When VID is ").append(vid).append(",");
			commentBuilder.append(" and ro.product.model is ")
					.append(androidDevice.getRoProductModel()).append(".");
			// Add comment.
			ListRewrite listRewrite = astRewrite.getListRewrite(block,
					Block.STATEMENTS_PROPERTY);
			Statement placeHolder = (Statement) astRewrite
					.createStringPlaceholder(commentBuilder.toString(),
							ASTNode.EMPTY_STATEMENT);
			listRewrite.insertLast(placeHolder, null);

			// Add test case per VID.
			addTestCaseByVid(ast, block, vid, androidDevice, astRewrite);

			// Add a blank line.
			if (index < androidDevice.getVids().length - 1) {

				Statement stringPlaceHolder = (Statement) astRewrite
						.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
				listRewrite.insertLast(stringPlaceHolder, null);
				index++;
			}
		}
		return methodDeclaration;
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	private void addTestCaseByVid(AST ast, Block block, String aVid,
			AndroidDevice device, ASTRewrite astRewrite) {
		// Add comment.
		ListRewrite listRewrite = astRewrite.getListRewrite(block,
				block.STATEMENTS_PROPERTY);
		StringBuilder deviceParamBuilder = new StringBuilder();
		deviceParamBuilder.append("param_").append(aVid).append("_")
				.append(format(device.getRoProductModel()));
		String deviceParamName = deviceParamBuilder.toString();
		
		StringBuilder productIdParamBuilder = new StringBuilder();
		productIdParamBuilder.append("productId_").append(aVid).append("_")
				.append(format(device.getRoProductModel()));
		String productIdParamName = productIdParamBuilder.toString();

		// Initialize DeviceInitParam.
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName(deviceParamName));
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		cic.setType(ast.newSimpleType(ast.newSimpleName("DeviceInitParam")));
		vdf.setInitializer(cic);
		VariableDeclarationStatement vds = ast
				.newVariableDeclarationStatement(vdf);
		vds.setType(ast.newSimpleType(ast.newSimpleName("DeviceInitParam")));
		// block.statements().add(vds);
		listRewrite.insertLast(vds, null);

		// setVid method invocation.
		MethodInvocation setVidMethodInvocation = ast.newMethodInvocation();
		setVidMethodInvocation
				.setExpression(ast.newSimpleName(deviceParamName));
		setVidMethodInvocation.setName(ast.newSimpleName("setVID"));
		StringLiteral vid = ast.newStringLiteral();
		vid.setLiteralValue(aVid);
		setVidMethodInvocation.arguments().add(vid);
		ExpressionStatement setVidExpressionStatement = ast
				.newExpressionStatement(setVidMethodInvocation);
		// block.statements().add(setVidExpressionStatement);
		listRewrite.insertLast(setVidExpressionStatement, null);

		// setRoProductModel method invocation.
		// addMethodInvocation(ast, block, deviceParamName, "setRoProductModel",
		// device.getRoProductModel(), listRewrite);

		conditionFields(ast, block, device, listRewrite, deviceParamName);

		// normalize method invocation.
		MethodInvocation mi = ast.newMethodInvocation();
		mi = ast.newMethodInvocation();
		mi.setExpression(ast.newSimpleName(deviceParamName));
		mi.setName(ast.newSimpleName("normalize"));
		ExpressionStatement es = ast.newExpressionStatement(mi);
		// block.statements().add(es);
		listRewrite.insertLast(es, null);

		// findProductId method invocation.
		VariableDeclarationFragment productIdVariableDeclarationFragment = ast
				.newVariableDeclarationFragment();
		productIdVariableDeclarationFragment.setName(ast
				.newSimpleName(productIdParamName));
		MethodInvocation findProductIdMethodInvocation = ast
				.newMethodInvocation();
		findProductIdMethodInvocation.setExpression(ast
				.newSimpleName("DeviceStore"));
		findProductIdMethodInvocation.setName(ast
				.newSimpleName("findProductId"));
		findProductIdMethodInvocation.arguments().add(
				ast.newName(deviceParamName));
		productIdVariableDeclarationFragment
				.setInitializer(findProductIdMethodInvocation);
		VariableDeclarationStatement findProductIdVariableDeclarationStatement = ast
				.newVariableDeclarationStatement(productIdVariableDeclarationFragment);
		findProductIdVariableDeclarationStatement.setType(ast.newSimpleType(ast
				.newSimpleName("String")));
		// block.statements().add(findProductIdVariableDeclarationStatement);
		listRewrite.insertLast(findProductIdVariableDeclarationStatement, null);

		// Assert method invocation.
		MethodInvocation assertMethodInvocation = ast.newMethodInvocation();
		assertMethodInvocation.setExpression(ast.newSimpleName("Assert"));
		assertMethodInvocation.setName(ast.newSimpleName("assertEquals"));
		StringLiteral expectedProductId = ast.newStringLiteral();
		expectedProductId.setLiteralValue(device.getProductId());
		assertMethodInvocation.arguments().add(expectedProductId);
		assertMethodInvocation.arguments().add(ast.newName(productIdParamName));
		// block.statements().add(
		// ast.newExpressionStatement(assertMethodInvocation));
		listRewrite.insertLast(
				ast.newExpressionStatement(assertMethodInvocation), null);
	}

	private String format(String model) {
		return model == null ? model : model.replaceAll("[-_\\s]", "");
	}

	private void conditionFields(AST ast, Block block, AndroidDevice device,
			ListRewrite listRewrite, String deviceParamName) {
		for (Field field : device.getClass().getDeclaredFields()) {
			// Exclude fields.
			if (!EXCLUDED_FIELDS.contains(field.getName())) {
				try {
					field.setAccessible(true);
					Object fieldValue = field.get(device);
					if (fieldValue != null) {
						addMethodInvocation(ast, block, deviceParamName,
								RefectUtils.setterName(field.getName()),
								fieldValue.toString(), listRewrite);
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addMethodInvocation(AST ast, Block block, String object,
			String methodName, String argument, ListRewrite listRewrite) {
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newSimpleName(object));
		methodInvocation.setName(ast.newSimpleName(methodName));
		StringLiteral vid = ast.newStringLiteral();
		vid.setLiteralValue(argument);
		methodInvocation.arguments().add(vid);
		ExpressionStatement expressionStatement = ast
				.newExpressionStatement(methodInvocation);
		// block.statements().add(setVidExpressionStatement);
		listRewrite.insertLast(expressionStatement, null);
	}

	@SuppressWarnings("deprecation")
	private void updateTestCase(AndroidDevice androidDevice)
			throws JavaModelException, MalformedTreeException,
			BadLocationException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(this.icomilationUnit);

		CompilationUnit compilationUnit = (CompilationUnit) parser
				.createAST(null);
		// start record of the modifications
		// compilationUnit.recordModifications();
		// modify the AST
		TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit
				.types().get(0);

		AST ast = compilationUnit.getAST();
		// creation of ASTRewrite
		ASTRewrite rewrite = ASTRewrite.create(ast);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration methodDeclaration = getMethodByName(methods,
				productIdToMethodName(androidDevice.getProductId()));

		for (String vid : androidDevice.getVids()) {
			ListRewrite listRewrite = rewrite.getListRewrite(
					methodDeclaration.getBody(), Block.STATEMENTS_PROPERTY);
			// Add a blank line.
			Statement stringPlaceHolder = (Statement) rewrite
					.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
			listRewrite.insertLast(stringPlaceHolder, null);
			// Building comment.
			StringBuilder commentBuilder = new StringBuilder();
			commentBuilder.append("//When VID is ").append(vid).append(",");
			commentBuilder.append(" and ro.product.model is ")
					.append(androidDevice.getRoProductModel()).append(".");
			// Add comment.

			Statement placeHolder = (Statement) rewrite
					.createStringPlaceholder(commentBuilder.toString(),
							ASTNode.EMPTY_STATEMENT);
			listRewrite.insertLast(placeHolder, null);
			addTestCaseByVid(ast, methodDeclaration.getBody(), vid,
					androidDevice, rewrite);
		}

		// get the current document source
		final Document document = new Document(this.icomilationUnit.getSource());
		// computation of the text edits
		// TextEdit edits = compilationUnit.rewrite(document,
		// this.icomilationUnit
		// .getJavaProject().getOptions(true));
		TextEdit edits = rewrite.rewriteAST(document, this.icomilationUnit
				.getJavaProject().getOptions(true));

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		// update of the compilation unit
		this.icomilationUnit.getBuffer().setContents(newSource);

		// Commit changes
		// icomilationUnit.commitWorkingCopy(false, null);

		// Destroy working copy
		// icomilationUnit.discardWorkingCopy();

	}

	private MethodDeclaration getMethodByName(MethodDeclaration[] methods,
			String methodName) {
		MethodDeclaration theMethod = null;
		for (MethodDeclaration method : methods) {
			if (method.getName().toString().equals(methodName)) {
				theMethod = method;
				break;
			}
		}
		return theMethod;
	}

	@SuppressWarnings("deprecation")
	private boolean hasMethod(String methodName) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(this.icomilationUnit);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astRoot.types()
				.get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration method : methods) {
			if (method.getName().toString().equalsIgnoreCase(methodName)) {
				return true;
			}
		}
		return false;
	}

	private static String productIdToMethodName(String productId) {
		if (productId == null) {
			throw new IllegalArgumentException("product id is null.");
		} else if (Pattern.matches("^\\d.*$", productId)) {
			return "test_" + productId.replace("-", "_");
		} else {
			return productId.replace("-", "_");
		}
	}

}
