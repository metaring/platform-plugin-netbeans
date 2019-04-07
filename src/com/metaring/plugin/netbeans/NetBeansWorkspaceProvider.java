/**
 *    Copyright 2019 MetaRing s.r.l.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.metaring.plugin.netbeans;

import com.metaring.plugin.ProjectProvider;
import com.metaring.plugin.WorkspaceProvider;
import com.metaring.framework.Resources;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroupChangeEvent;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;

public class NetBeansWorkspaceProvider extends WorkspaceProvider {

    private final FileChangeAdapter FILE_CHANGE_ADAPTER = new FileChangeAdapter() {
        @Override
        public void fileChanged(FileEvent fe) {
            update();
        }
    };

    private final List<NetBeansProject> list = new ArrayList<>();

    public NetBeansWorkspaceProvider() {
        super();
    }

    @Override
    protected void startUpdateCallback() {
        OpenProjects.getDefault().addPropertyChangeListener(event -> update());
        OpenProjects.getDefault().addProjectGroupChangeListener(new ProjectGroupChangeListener() {
            @Override
            public void projectGroupChanging(ProjectGroupChangeEvent pgce) {
            }

            @Override
            public void projectGroupChanged(ProjectGroupChangeEvent pgce) {
                update();
            }
        });
    }

    @Override
    public ProjectProvider[] listAllProjects() {
        list.forEach(NetBeansProject::stopListening);
        list.clear();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            list.add(new NetBeansProject(ProjectUtils.getInformation(project).getDisplayName(), FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath() + "/", project));
        }
        return list.toArray(new ProjectProvider[list.size()]);
    }

    @Override
    public final String getDefaultSysKBPath() {
        return "nbres:/main/resources/" + Resources.DEFAULT_SYSKB_FILE_NAME;
    }

    private final class NetBeansProject extends ProjectProvider {

        private final Project project;

        private NetBeansProject(String name, String path, Project project) {
            super(name, path);
            (this.project = project).getProjectDirectory().addFileChangeListener(FILE_CHANGE_ADAPTER);
        }

        public final void stopListening() {
            this.project.getProjectDirectory().removeFileChangeListener(FILE_CHANGE_ADAPTER);
        }
    }
}
