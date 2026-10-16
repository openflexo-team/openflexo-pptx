/**
 *
 * Copyright (c) 2014-2025, Openflexo
 *
 * This file is part of Powerpointconnector, a component of the software infrastructure
 * developed at Openflexo.
 *
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or
 *          combining it with software containing parts covered by the terms
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.technologyadapter.powerpoint.fml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;

/**
 * A parameterized suite of unit tests iterating on FML-script files describing PowerPoint use cases.
 *
 * For each FML-script file, the whole script is parsed, syntactically checked and executed. Every
 * {@code assert} in the script must succeed.
 *
 * @author sylvain
 *
 */
@RunWith(Parameterized.class)
public class AutomatedTests extends FMLScriptParserTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("TestResourceCenter/AutomatedTests"), ".fmlscript");
	}

	private final Resource fmlResource;
	private FlexoEditor editor;
	private FMLScript script;
	private CommandInterpreter commandInterpreter;

	public AutomatedTests(Resource fmlResource, String name) throws ParseException, ModelDefinitionException, IOException {
		System.out.println("********* Launch FML-script " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
		initServiceManager();
	}

	@Test
	public void checkScript() throws ModelDefinitionException, ParseException, IOException, FMLCommandExecutionException {
		System.out.println("Parse script " + fmlResource.getRelativePath());
		script = parseFMLScript(fmlResource, commandInterpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}

	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
	}

}
