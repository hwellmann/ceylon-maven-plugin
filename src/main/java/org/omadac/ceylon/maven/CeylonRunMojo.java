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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Runs a Ceylon module using the "ceylon run" command.
 * @goal run
 */
public class CeylonRunMojo extends AbstractMojo {
    

    /**
     * Ceylon home directory.
     * 
     * @parameter expression="${ceylon.home}" default-value="${env.CEYLON_HOME}"
     */
    private String home;
    
    /**
     * If <code>true</code>, disables the default module repositories.
     * Equivalent to the <code>--no-default-repositories</code> option of "ceylon run".
     * 
     * @parameter expression="${ceylon.disableDefaultRepos}" default="false"
     */
    private boolean disableDefaultRepos = false;
    
    
    /**
     * Ceylon working directory.
     * 
     * @parameter expression="${ceylon.cwd}"
     */
    private String workingDirectory;
    
    
    /**
     * @parameter expression="${ceylon.mavenOverrides}
     */
    private String mavenOverrides;
    
    /**
     * System properties.
     * 
     * @parameter
     */
    private Map<String, String> properties;
    
    /**
     * Enables offline mode that will prevent connections to remote repositories.
     * Equivalent to the <code>--offline</code> option of "ceylon test".
     * @parameter expression="${ceylon.offline} default="false"
     */
    private boolean offline;
    
    /**
     * Specifies the fully qualified name of a toplevel method or class with no parameters.
     * Equivalent to the <code>--run</code> option of "ceylon run".
     * @parameter 
     */
    private String run;
    
    
    /**
     * The module repositories containing dependencies.
     * Equivalent to the <code>--rep</code> option of "ceylon run".
     * 
     * @parameter expression="${ceylon.repositories}"
     */
    private List<String> repositories;
    
    /**
     * @parameter 
     */
    private String sysrep;
    
    /**
     * If <code>true</code>, the compiler generates verbose output
     * Equivalent to the <code>--verbose</code> option of "ceylon run".
     * 
     * @parameter expression="${ceylon.verbose}"
     */
    private String verbose;

    /**
     * The module to run (without versions).
     * 
     * @parameter 
     */
    private String module;
    
    /**
     * Whether the build should fail if there are errors
     * @parameter expression="${ceylon.failOnError}" default="${true}"
     */
    private boolean failOnError = true;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        String[] args = buildOptions();
        
        getLog().debug("Invoking 'ceylon run'");
        
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
        args.add("run");
        
        if (disableDefaultRepos) {
            args.add("--no-default-repositories");
        }
        
        if (verbose != null) {
        	// arguments are not handled correctly, see https://github.com/ceylon/ceylon-runtime/issues/18
            args.add("--verbose");
        }
               
        if (offline) {
            args.add("--offline");
        }
               
        if (run != null) {
        	args.add("--run");
        	args.add(run);
        }
        
        if (sysrep != null) {
        	args.add("--sysrep");
        	args.add(sysrep);
        }
        
        if (workingDirectory != null) {
        	args.add("--cwd");
        	args.add(workingDirectory);
        }
        
        if (mavenOverrides != null) {
        	args.add("--maven-overrides");
        	args.add(mavenOverrides);
        }
        
        if (repositories != null) {
        	for (String repository : repositories) {
            	args.add("--rep");
            	args.add(repository);        		
        	}
        }
        
        if (properties != null) {
        	for (Entry<String, String> entry : properties.entrySet()) {
        		args.add("-D");
        		args.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
        	}
        }
        
        
        
        if (module != null && !module.isEmpty()) {
        	args.add(module);
        } else {
            throw new MojoExecutionException("No module to run. Add <module> element");   
        }
        
        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());
        
        return args.toArray(new String[args.size()]);
    }

}
