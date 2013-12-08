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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Tests a Ceylon module using the "ceylon test" command.
 * @requiresProject
 * @goal test
 */
public class CeylonTestMojo extends AbstractMojo {
    

    /**
     * Ceylon home directory.
     * 
     * @parameter expression="${ceylon.home}" default-value="${env.CEYLON_HOME}"
     */
    private String home;
    
    /**
     * Build directory.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private String targetDir;
    
    /**
     * The module repositories containing dependencies.
     * Equivalent to the <code>ceylonc</code>'s <code>-rep</code> option.
     * 
     * @parameter expression="${ceylonc.repositories}"
     */
    private List<String> repositories;
    
    /**
     * If <code>true</code>, disables the default module repositories and source directory.
     * Equivalent to the <code>ceylonc</code>'s <code>-d</code> option.
     * 
     * @parameter expression="${ceylonc.disableDefaultRepos}" default="false"
     */
    private boolean disableDefaultRepos = false;
    
    /**
     * @parameter 
     */
    private boolean offline;
    
    /**
     * @parameter 
     */
    private String run;
    
    
    /**
     * @parameter 
     */
    private String sysrep;
    
    /**
     * If <code>true</code>, the compiler generates verbose output
     * Equivalent to the <code>ceylonc</code>'s <code>-verbose</code> option.
     * 
     * @parameter expression="${ceylonc.verbose}" default="false"
     */
    private boolean verbose;

    /**
     * The modules to test (without versions).
     * 
     * @parameter 
     */
    private List<String> modules;
    
    /**
     * Whether the build should fail if there are errors
     * @parameter expression="${ceylonc.failOnError}" default="${true}"
     */
    private boolean failOnError = true;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (modules == null || modules.isEmpty()) {
            getLog().info("No modules to test");
            return;
        }
    	
        String[] args = buildOptions();
        
        getLog().debug("Invoking ceylon test");
        
        int sc = 0;
		try {
			System.setProperty(Constants.PROP_CEYLON_HOME_DIR, home);
			sc = Launcher.run(args);
		} catch (Throwable e) {
            throw new MojoExecutionException("The Ceylon runtime returned an unexpected result", e);
		}
        if (sc == 1) {
            getLog().info("-------------------------------------------------------------");
            getLog().error("EXECUTION ERRORS (see above)");
            getLog().info("-------------------------------------------------------------");
            if (failOnError) {
                throw new MojoFailureException("Compilation Error");
            }
        } else if (sc != 0) {
            throw new MojoExecutionException("The Ceylon runtime returned an unexpected result");
        }
    }

    private String[] buildOptions() throws MojoExecutionException {
        List<String> args = new ArrayList<String>();
        args.add("test");
        
        if (disableDefaultRepos) {
            args.add("--no-default-repositories");
        }
        
        if (verbose) {
            args.add("--verbose");
        }
               
        if (sysrep != null) {
        	args.add("--sysrep");
        	args.add(sysrep);
        }
        
        if (repositories.isEmpty()) {
        	args.add("--rep");
        	args.add(targetDir);
        }
        else {
        	for (String repository : repositories) {
            	args.add("--rep");
            	args.add(repository);        		
        	}
        }
        
        
        if (modules != null && !modules.isEmpty()) {
        	for (String module : modules) {
        		args.add(module);
        	}
        } else {
            getLog().info("No modules to test");   
        }
        
        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());
        
        return args.toArray(new String[args.size()]);
    }

}
