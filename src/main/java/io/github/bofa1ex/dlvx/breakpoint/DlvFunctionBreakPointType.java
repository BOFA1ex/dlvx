package io.github.bofa1ex.dlvx.breakpoint;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import io.github.bofa1ex.dlvx.utils.DlvxUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DlvFunctionBreakPointType extends XLineBreakpointType<DlvFunctionBreakpointProperties> {
    public static final @NonNls String ID = "DlvFunctionBreakpoint";

    public DlvFunctionBreakPointType() {
        super(ID, "GoFunction breakpoint");
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (line < 0) return false;
        return DlvxUtils.findFunction(project, file, line) != null;
    }

    @Override
    public @Nullable DlvFunctionBreakpointProperties createBreakpointProperties(@NotNull VirtualFile virtualFile, int line) {
        return new DlvFunctionBreakpointProperties();
    }

    @Override
    public String getDisplayText(XLineBreakpoint<DlvFunctionBreakpointProperties> breakpoint) {
        return "Dlvx Function breakpoint";
    }
}
