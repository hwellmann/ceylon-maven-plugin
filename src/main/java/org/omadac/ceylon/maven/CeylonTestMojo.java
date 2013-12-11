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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.redhat.ceylon.common.Constants;
import com.redhat.ceylon.launcher.Launcher;

/**
 * Tests one or more Ceylon modules using the "ceylon test" command.
 */
@Mojo(name = "test", requiresProject = true)
public class CeylonTestMojo extends AbstractMojo {

    /**
     * Ceylon home directory.
     */
    @Parameter(property = "ceylon.home", defaultValue = "${env.CEYLON_HOME}")
    private String home;

    /**
     * Build directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private String targetDir;

    /**
     * The module repositories containing dependencies. Equivalent to the <code>ceylon</code>'s
     * <code>-rep</code> option.
     */
    @Parameter(property = "ceylon.repositories")
    private List<String> repositories;

    /**
     * If <code>true</code>, disables the default module repositories and source directory.
     * Equivalent to the <code>ceylon</code>'s <code>-d</code> option.
     */
    @Parameter(property = "ceylon.disableDefaultRepos", defaultValue = "false")
    private boolean disableDefaultRepos = false;

    /**
     * Enables offline mode that will prevent connections to remote repositories. Equivalent to the
     * <code>--offline</code> option of "ceylon run".
     */
    @Parameter(property = "ceylon.offline", defaultValue = "false")
    private boolean offline;

    /**
     * Specifies which tests will be run.
     */
    @Parameter(property = "ceylon.test")
    private String test;

    @Parameter
    private String sysrep;

    /**
     * If <code>true</code>, the compiler generates verbose output Equivalent to the
     * <code>ceylonc</code>'s <code>--verbose</code> option.
     */
    @Parameter(property = "ceylon.verbose", defaultValue = "false")
    private boolean verbose;

    /**
     * The modules to test (without versions).
     */
    @Parameter
    private List<String> testModules;

    /**
     * Whether the build should fail if there are errors
     */
    @Parameter(property = "ceylon.failOnError", defaultValue = "true")
    private boolean failOnError = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (testModules == null || testModules.isEmpty()) {
            getLog().info("No modules to test");
            return;
        }

        String[] args = buildOptions();

        getLog().debug("Invoking ceylon test");

        int sc = 0;
        try {
            System.setProperty(Constants.PROP_CEYLON_HOME_DIR, home);
            sc = Launcher.run(args);
        }
        catch (Throwable e) {
            throw new MojoExecutionException("The Ceylon runtime returned an unexpected result", e);
        }
        if (sc == 1) {
            getLog().info("-------------------------------------------------------------");
            getLog().error("EXECUTION ERRORS (see above)");
            getLog().info("-------------------------------------------------------------");
            if (failOnError) {
                throw new MojoFailureException("Compilation Error");
            }
        }
        else if (sc != 0) {
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
            // arguments are not handled correctly, see
            // https://github.com/ceylon/ceylon-runtime/issues/18
            args.add("--verbose");
        }

        if (offline) {
            args.add("--offline");
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

        if (test != null) {
            args.add("--test");
            args.add(test);
        }

        for (String module : testModules) {
            args.add(module);
        }

        getLog().debug("Command line options to ceylon:");
        getLog().debug(args.toString());

        return args.toArray(new String[args.size()]);
    }

}
