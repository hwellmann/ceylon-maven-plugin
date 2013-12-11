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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Compiles Ceylon and Java source code using the "ceylon compile" command.
 * 
 */
@Mojo(name = "compile")
public class CeylonCompileMojo extends AbstractMojo {

    /**
     * Ceylon home directory.
     */
    @Parameter(property = "ceylon.home", defaultValue = "${env.CEYLON_HOME}")
    protected String home;

    /**
     * The repository in which to create the output <code>.car</code> file. Equivalent to the
     * <code>--out</code> option of "ceylon compile".
     */
    @Parameter(property = "ceylon.out", defaultValue = "${project.build.directory}")
    protected String out;

    /**
     * The directory containing ceylon source code. Equivalent to the <code>--source</code> option
     * of "ceylon compile".
     */
    @Parameter(property = "ceylon.source", defaultValue = "${project.build.sourceDirectory}")
    protected File source;

    /**
     * The directory containing ceylon resource code. Equivalent to the <code>--resource</code>
     * option of "ceylon compile".
     */
    @Parameter(property = "ceylon.resource", defaultValue = "${project.build.resources[0].directory}")
    protected File resource;

    /**
     * If <code>true</code>, disables the default module repositories. Equivalent to the
     * <code>--no-default-repositories</code> option.
     */
    @Parameter(property = "ceylon.disableDefaultRepos", defaultValue = "false")
    protected boolean disableDefaultRepos = false;

    /**
     * If <code>true</code>, the compiler generates verbose output Equivalent to the
     * <code>--verbose</code> option of "ceylon compile".
     */
    @Parameter(property = "ceylon.verbose", defaultValue = "false")
    protected boolean verbose;

    /**
     * The module repositories containing dependencies. Equivalent to the <code>-rep</code> option
     * of "ceylon compile".
     */
    @Parameter(property = "ceylon.repositories")
    protected List<String> repositories;

    /**
     * The modules to compile (without versions).
     */
    @Parameter(property = "ceylon.modules", required = true)
    protected List<String> modules;

    /**
     * Whether the build should fail if there are errors
     */
    @Parameter(property = "ceylon.failOnError", defaultValue = "true")
    protected boolean failOnError = true;

    /**
     * The user name to use for the output repository. Corresponds to the <code>--user</code> option
     * of "ceylon compile".
     */
    @Parameter(property = "ceylon.username")
    protected String username;

    /**
     * The password to use for the output repository Corresponds to the <code>--pass</code> option
     * of "ceylon compile".
     */
    @Parameter(property = "ceylon.password")
    protected String password;

    /**
     * The source file character encoding.
     * 
     */
    @Parameter(property = "project.build.sourceEncoding", defaultValue = "${file.encoding}")
    protected String encoding;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (modules == null || modules.isEmpty()) {
            getLog().info("No modules to compile");
            return;
        }

        String[] args = buildOptions();

        getLog().debug("ceylon.home = " + home);
        getLog().debug("Invoking 'ceylon compile'");

        int sc = 0;
        try {
            System.setProperty(Constants.PROP_CEYLON_HOME_DIR, home);
            sc = Launcher.run(args);
        }
        catch (Throwable e) {
            throw new MojoExecutionException("The compiler returned an unexpected result", e);
        }
        if (sc == 1) {
            getLog().info("-------------------------------------------------------------");
            getLog().error("COMPILATION ERRORS (see above)");
            getLog().info("-------------------------------------------------------------");
            if (failOnError) {
                throw new MojoFailureException("Compilation Error");
            }
        }
        else if (sc != 0) {
            throw new MojoExecutionException("The compiler returned an unexpected result");
        }
    }

    private String[] buildOptions() throws MojoExecutionException {
        List<String> args = new ArrayList<String>();
        args.add("compile");
        args.add("--out");
        args.add(out);

        args.add("--source");
        args.add(source.getPath());

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

        if (modules != null && !modules.isEmpty()) {
            for (String module : modules) {
                args.add(module);
            }
        }
        else {
            throw new MojoExecutionException("No modules to compile. Specify these using <modules>");
        }

        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());

        return args.toArray(new String[args.size()]);
    }

}
