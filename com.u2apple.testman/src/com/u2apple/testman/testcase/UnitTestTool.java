package com.u2apple.testman.testcase;

import java.io.IOException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.u2apple.testman.model.AndroidDevice;

public class UnitTestTool {
	ICompilationUnit icomilationUnit;

	public UnitTestTool(ICompilationUnit icomilationUnit) {
		this.icomilationUnit = icomilationUnit;
	}

	public void generateTestCase() {
		try {
			// addTestCase();
			// boolean hasMethod = hasMethod("samsung-t2556test");
			// System.out.println(hasMethod);
			AndroidDevice androidDevice = new AndroidDevice();
			androidDevice.setProductId("zzbao-t981");
			androidDevice.setRoProductModel("T981");
			androidDevice.setVids(new String[] { "1782", "18D1" });
			addTestCase(androidDevice);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private void addTestCase(AndroidDevice androidDevice) throws IOException,
			JavaModelException, MalformedTreeException, BadLocationException {
		// String source = readFile(SRC);
		// Document document = new Document(source);
		// creation of DOM/AST from a ICompilationUnit
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(this.icomilationUnit);

		CompilationUnit compilationUnit = (CompilationUnit) parser
				.createAST(null);
		// start record of the modifications
		compilationUnit.recordModifications();
		// modify the AST
		TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit
				.types().get(0);

		AST ast = compilationUnit.getAST();

		// List<TypeDeclaration> types = astRoot.types();

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
		typeDeclaration.bodyDeclarations().add(methodDeclaration);

		for (String vid : androidDevice.getVids()) {
			addTestCaseByVid(ast, block, vid, androidDevice);
		}

		// get the current document source
		final Document document = new Document(this.icomilationUnit.getSource());
		// computation of the text edits
		TextEdit edits = compilationUnit.rewrite(document, this.icomilationUnit
				.getJavaProject().getOptions(true));

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		// update of the compilation unit
		this.icomilationUnit.getBuffer().setContents(newSource);
		
		  // Commit changes
		icomilationUnit.commitWorkingCopy(false, null);
	    
	    // Destroy working copy
		icomilationUnit.discardWorkingCopy();

	}

	@SuppressWarnings("unchecked")
	private void addTestCaseByVid(AST ast, Block block, String aVid,
			AndroidDevice device) {
		String deviceParamName = new String("paramForVid" + aVid);
		String productIdParamName = new String("productIdFor" + aVid);

		// Initialize DeviceInitParam.
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName(deviceParamName));
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		cic.setType(ast.newSimpleType(ast.newSimpleName("DeviceInitParam")));
		vdf.setInitializer(cic);
		VariableDeclarationStatement vds = ast
				.newVariableDeclarationStatement(vdf);
		vds.setType(ast.newSimpleType(ast.newSimpleName("DeviceInitParam")));
		block.statements().add(vds);

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
		block.statements().add(setVidExpressionStatement);

		// setRoProductModel method invocation.
		addMethodInvocation(ast, block, deviceParamName, "setRoProductModel",
				device.getRoProductModel());

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
		block.statements().add(findProductIdVariableDeclarationStatement);

		// normalize method invocation.
		MethodInvocation mi = ast.newMethodInvocation();
		mi = ast.newMethodInvocation();
		mi.setExpression(ast.newSimpleName(deviceParamName));
		mi.setName(ast.newSimpleName("normalize"));
		ExpressionStatement es = ast.newExpressionStatement(mi);
		block.statements().add(es);

		// Assert method invocation.
		MethodInvocation assertMethodInvocation = ast.newMethodInvocation();
		assertMethodInvocation.setExpression(ast.newSimpleName("Assert"));
		assertMethodInvocation.setName(ast.newSimpleName("assertEquals"));
		StringLiteral expectedProductId = ast.newStringLiteral();
		expectedProductId.setLiteralValue(device.getProductId());
		assertMethodInvocation.arguments().add(expectedProductId);
		assertMethodInvocation.arguments().add(ast.newName(productIdParamName));
		block.statements().add(
				ast.newExpressionStatement(assertMethodInvocation));
	}

	@SuppressWarnings("unchecked")
	private void addMethodInvocation(AST ast, Block block, String object,
			String methodName, String argument) {
		MethodInvocation setVidMethodInvocation = ast.newMethodInvocation();
		setVidMethodInvocation = ast.newMethodInvocation();
		setVidMethodInvocation.setExpression(ast.newSimpleName(object));
		setVidMethodInvocation.setName(ast.newSimpleName(methodName));
		StringLiteral vid = ast.newStringLiteral();
		vid.setLiteralValue(argument);
		setVidMethodInvocation.arguments().add(vid);
		ExpressionStatement setVidExpressionStatement = ast
				.newExpressionStatement(setVidMethodInvocation);
		block.statements().add(setVidExpressionStatement);
	}

	private void updateTestCase(AndroidDevice androidDevice) {

	}

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
		}
		return productId.replace("-", "_");
	}

}
