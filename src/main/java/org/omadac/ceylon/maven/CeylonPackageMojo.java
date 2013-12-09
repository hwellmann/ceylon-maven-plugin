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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Installs a Ceylon module archive in the Maven repository
 * 
 * @goal package
 * @requiresProject
 */
public class CeylonPackageMojo extends AbstractMojo {

    /**
     * Adds a Ceylon archive as default artifact of the current Maven project. 
     * 
     * @parameter expression="${ceylon.out}" default-value="${project.build.directory}"
     */
    protected String out;

    /**
     * The modules to compile (without versions).
     * 
     * @parameter expression="${ceylon.modules}"
     * @required
     */
    protected List<String> modules;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Packaging artifact");
        if (modules == null || modules.isEmpty()) {
            getLog().info("No module defined");
            return;
        }

        File carFile = getCarFile();
        if (carFile.exists()) {
            project.getArtifact().setFile(carFile);
        }
    }

    private File getCarFile() {
        String module = modules.get(0);
        String modulePath = module.replaceAll("\\.", File.separator);
        File moduleDir = new File(out, modulePath);
        File versionDir = new File(moduleDir, project.getVersion());
        String fileName = String.format("%s-%s.car", module, project.getVersion());
        return new File(versionDir, fileName);
    }
}
