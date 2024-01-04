package io.github.bofa1ex.dlvx.utils;

import com.goide.GoLanguage;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.DocumentUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DlvxUtils {

    public static @Nullable GoFunctionOrMethodDeclaration findFunction(@NotNull Project project, @NotNull VirtualFile file, int line) {
        if (LanguageUtil.getLanguageForPsi(project, file) != GoLanguage.INSTANCE) return null;

        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return null;

        final Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document == null) return null;

        final Ref<GoFunctionOrMethodDeclaration> res = Ref.create();
        XDebuggerUtil.getInstance().iterateLine(project, document, line, psiElement -> {
            PsiElement parent = psiElement;
            while (psiElement != null) {
                final int offset = psiElement.getTextOffset();
                if (!DocumentUtil.isValidOffset(offset, document) || document.getLineNumber(offset) != line) break;
                parent = psiElement;
                psiElement = psiElement.getParent();
            }

            if (parent instanceof GoFunctionOrMethodDeclaration) {
                res.set(((GoFunctionOrMethodDeclaration) parent));
                return false;
            }

            return true;
        });

        return res.get();
    }
}
