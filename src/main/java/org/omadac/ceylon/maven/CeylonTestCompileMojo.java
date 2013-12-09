/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.omadac.ceylon.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Compiles Ceylon and Java test source code using the "ceylon compile" command.
 * @goal testCompile
 */
public class CeylonTestCompileMojo extends CeylonCompileMojo {
    

    /**
     * The modules to compile (without versions).
     * 
     * @parameter expression="${ceylon.testModules}"
     * @required
     */
    protected List<String> testModules;
    
    /**
     * The directory containing ceylon source code. 
     * Equivalent to the <code>--source</code> option of "ceylon compile".
     * 
     * @parameter expression="${ceylon.testSource}" default-value="${project.build.testSourceDirectory}"
     */
    protected File testSource;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (testModules == null || testModules.isEmpty()) {
            getLog().info("No test modules to compile");
            return;
        }
    	
        String[] args = buildOptions();
        
        getLog().debug("ceylon.home = " + home);
        getLog().debug("Invoking 'ceylon compile' for test sources");
        
        int sc = 0;
		try {
			System.setProperty(Constants.PROP_CEYLON_HOME_DIR, home);
			sc = Launcher.run(args);
		} catch (Throwable e) {
            throw new MojoExecutionException("The compiler returned an unexpected result", e);
		}
        if (sc == 1) {
            getLog().info("-------------------------------------------------------------");
            getLog().error("COMPILATION ERRORS (see above)");
            getLog().info("-------------------------------------------------------------");
            if (failOnError) {
                throw new MojoFailureException("Compilation Error");
            }
        } else if (sc != 0) {
            throw new MojoExecutionException("The compiler returned an unexpected result");
        }
    }

    private String[] buildOptions() throws MojoExecutionException {
        List<String> args = new ArrayList<String>();
        args.add("compile");
        args.add("--out");
        args.add( out);
        
        args.add("--source");
        args.add(testSource.getPath());
        
        args.add("--resource");
        args.add(resource.getPath());
        
        if (disableDefaultRepos) {
            args.add("--no-default-repositories");
        }
        
        if (verbose) {
            args.add("--verbose");
        }
               
        if (username != null) {
            args.add("--user");
            args.add(username);
        }
        
        if (password != null) {
            args.add("--pass");
            args.add(password);
        }
        
        if (repositories != null) {
            for (String rep : repositories) {
                args.add("--rep");
                args.add(rep);
            }
        }
        
        if (encoding != null) {
            args.add("--encoding");
            args.add(encoding);
        }
        
        for (String module : testModules) {
        	args.add(module);
        }
        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());
        
        return args.toArray(new String[args.size()]);
    }

}
