// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.wx.web_publisher_plugin.action;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Action class to demonstrate how to interact with the IntelliJ Platform.
 * The only action this class performs is to provide the user with a popup dialog as feedback.
 * Typically this class is instantiated by the IntelliJ Platform framework based on declarations
 * in the plugin.xml file. But when added at runtime this class is instantiated by an action group.
 */
public class PopupDeployAction extends AnAction {

    /**
     * This default constructor is used by the IntelliJ Platform framework to instantiate this class based on plugin.xml
     * declarations. Only needed in {@link PopupDeployAction} class because a second constructor is overridden.
     *
     * @see AnAction#AnAction()
     */
    public PopupDeployAction() {
        super();
    }

    /**
     * This constructor is used to support dynamically added menu actions.
     * It sets the text, description to be displayed for the menu item.
     * Otherwise, the default AnAction constructor is used by the IntelliJ Platform.
     *
     * @param text        The text to be displayed as a menu item.
     * @param description The description of the menu item.
     * @param icon        The icon to be used with the menu item.
     */
    public PopupDeployAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    /**
     * 封装執行cmd命令
     *
     * @param command
     * @throws IOException
     */
    public static String executeCmd(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
        String line = null;
        StringBuilder build = new StringBuilder();
        while ((line = br.readLine()) != null) {
            build.append(line);
        }
        return build.toString();
    }


    /**
     * Gives the user feedback when the dynamic action menu is chosen.
     * Pops a simple message dialog. See the psi_demo plugin for an
     * example of how to use {@link AnActionEvent} to access data.
     *
     * @param event Event received when the associated menu item is chosen.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Using the event, create and show a dialog
        Project currentProject = event.getProject();

        // env
        String finalEnv = "";
        String selectedEnv = event.getPresentation().getText();
        if (selectedEnv.indexOf("Production") > 0) {
            finalEnv = "Production";
        } else {
            finalEnv = "Test";
        }

        // current opened editor
        Document currentDoc = FileEditorManager.getInstance(currentProject).getSelectedTextEditor().getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(currentProject).getPsiFile(currentDoc);
        VirtualFile vFile = psiFile.getOriginalFile().getVirtualFile();
        String finalPath = vFile.getPath();

        // project view right click
        if (event.getPlace() == "ProjectViewPopup") {
            Object psiFileTree = ProjectView.getInstance(currentProject).getCurrentProjectViewPane().getSelectedElement();
            if (psiFileTree instanceof PsiDirectory) {
                finalPath = ((PsiDirectory) psiFileTree).getVirtualFile().getPath();
            } else if (psiFileTree instanceof PsiFile) {
                finalPath = ((PsiFile) psiFileTree).getVirtualFile().getPath();
            }
        }

        // cmd
        String finalCmd = "\"C:\\Program Files (x86)\\WangxuTech\\WX Web Publisher\\WXWebPublisher.exe\"";
        String cmdParameter_1 = "/Environment=" + finalEnv;
        finalPath = finalPath.replace("/", "\\");
        String cmdParameter_2 = "/Path=\"" + finalPath + "\"";

        // run
        finalCmd += " " + cmdParameter_1 + " " + cmdParameter_2;
        try {
            executeCmd(finalCmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Determines whether this menu item is available for the current context.
     * Requires a project to be open.
     *
     * @param e Event received when the associated group-id menu is chosen.
     */
    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

}
