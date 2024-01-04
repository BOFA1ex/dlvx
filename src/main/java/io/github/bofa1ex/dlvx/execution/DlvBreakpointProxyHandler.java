package io.github.bofa1ex.dlvx.execution;

import com.goide.dlv.protocol.DlvRequest;
import com.goide.i18n.GoBundle;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import io.github.bofa1ex.dlvx.DlvMirrorDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

public abstract class DlvBreakpointProxyHandler<B extends XLineBreakpoint<?>> extends XBreakpointHandler<B> {
    protected DlvMirrorDebugProcess mirrorDebugProcess;
    protected DlvBreakpointProxyHandler(@NotNull Class<? extends XBreakpointType<B, ?>> breakpointTypeClass, DlvMirrorDebugProcess mirrorDebugProcess) {
        super(breakpointTypeClass);
        this.mirrorDebugProcess = mirrorDebugProcess;
    }

    abstract Promise<?> sendSetBreakpoint(B breakpoint, int line, String condition, String debuggerPath);

    @Override
    public void registerBreakpoint(@NotNull B breakpoint) {
        final XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
        if (breakpointPosition == null) return;

        final VirtualFile file = breakpointPosition.getFile();
        final XExpression expression = breakpoint.getConditionExpression();
        final String condition = expression != null ? expression.getExpression() : null;
        final String debuggerPath = mirrorDebugProcess.getDebuggerPath(file);

        if (debuggerPath == null) {
            mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, GoBundle.message("go.debugger.cannot.find.debugger.path", file.getPath()));
        } else {
            mirrorDebugProcess.pauseIfNeededAndProcess(() -> this.sendSetBreakpoint(breakpoint, breakpointPosition.getLine(), condition, debuggerPath));
        }
    }

    @Override
    public void unregisterBreakpoint(@NotNull B breakpoint, boolean temp) {
        XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
        if (breakpointPosition == null) return;

        mirrorDebugProcess.pauseIfNeededAndProcess(() -> {
            final Integer id = mirrorDebugProcess.breakpoints().remove(breakpoint);
            return id != null ? mirrorDebugProcess.send(new DlvRequest.ClearBreakpoint(id)) : Promises.resolvedPromise();
        });
    }
}
