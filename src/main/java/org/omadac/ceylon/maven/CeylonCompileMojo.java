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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Compiles Ceylon and Java source code using the "ceylon compile" command.
 * @goal compile
 */
public class CeylonCompileMojo extends AbstractMojo {
    

    /**
     * Ceylon home directory.
     * 
     * @parameter expression="${ceylon.home}" default-value="${env.CEYLON_HOME}"
     */
    private String home;
    
    /**
     * The repository in which to create the output <code>.car</code> file. 
     * Equivalent to the <code>ceylonc</code>'s <code>-out</code> option. 
     *
     * @parameter expression="${ceylonc.out}" default-value="${project.build.directory}"
     */
    private String out;
    
    /**
     * The directory containing ceylon source code. 
     * Equivalent to the <code>ceylonc</code>'s <code>-src</code> option.
     * 
     * @parameter expression="${ceylonc.src}" default-value="${project.build.sourceDirectory}"
     */
    private File src;
    
    /**
     * If <code>true</code>, disables the default module repositories and source directory.
     * Equivalent to the <code>ceylonc</code>'s <code>-d</code> option.
     * 
     * @parameter expression="${ceylonc.disableDefaultRepos}" default="false"
     */
    private boolean disableDefaultRepos = false;
    
    /**
     * If <code>true</code>, the compiler generates verbose output
     * Equivalent to the <code>ceylonc</code>'s <code>-verbose</code> option.
     * 
     * @parameter expression="${ceylonc.verbose}" default="false"
     */
    private boolean verbose = false;

    /**
     * The module repositories containing dependencies.
     * Equivalent to the <code>ceylonc</code>'s <code>-rep</code> option.
     * 
     * @parameter expression="${ceylonc.repositories}"
     */
    private List<String> repositories;
    
    /**
     * The modules to compile (without versions).
     * 
     * @parameter expression="${ceylonc.modules}"
     */
    private List<String> modules;
    
    /**
     * Whether the build should fail if there are errors
     * @parameter expression="${ceylonc.failOnError}" default="${true}"
     */
    private boolean failOnError = true;
    
    /**
     * The user name to use for the output repository
     * @parameter expression="${ceylonc.username}" 
     */
    private String username;
    
    /**
     * The password to use for the output repository
     * @parameter expression="${ceylonc.password}" 
     */
    private String password;
    
    /**
     * The source file character encoding.
     * @parameter expression="${project.build.sourceEncoding}" default="${file.encoding}"
     */
    private String encoding;
        
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        String[] args = buildOptions();
        
        getLog().debug("Invoking ceylon compile");
        
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
        
        args.add("--src");
        args.add(src.getPath());
        
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
        
        if (modules != null && !modules.isEmpty()) {
            for (String module : modules) {
                args.add(module);
            }
        } else {
            throw new MojoExecutionException("No modules to compile. Specify these using <modules>");   
        }
        
        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());
        
        return args.toArray(new String[args.size()]);
    }

}
