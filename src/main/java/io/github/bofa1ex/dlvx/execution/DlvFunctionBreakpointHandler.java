package io.github.bofa1ex.dlvx.execution;

import com.goide.dlv.protocol.DlvApi;
import com.goide.dlv.protocol.DlvRequest;
import com.goide.i18n.GoBundle;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import io.github.bofa1ex.dlvx.DlvMirrorDebugProcess;
import io.github.bofa1ex.dlvx.DlvxBundle;
import io.github.bofa1ex.dlvx.breakpoint.DlvFunctionBreakPointType;
import io.github.bofa1ex.dlvx.breakpoint.DlvFunctionBreakpointProperties;
import io.github.bofa1ex.dlvx.protocol.DlvCreateFunctionBreakpoint;
import io.github.bofa1ex.dlvx.utils.DlvxUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

import java.util.List;
import java.util.Objects;

public class DlvFunctionBreakpointHandler extends DlvBreakpointProxyHandler<XLineBreakpoint<DlvFunctionBreakpointProperties>> {
    public DlvFunctionBreakpointHandler(DlvMirrorDebugProcess debugProcess) {
        super(DlvFunctionBreakPointType.class, debugProcess);
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<DlvFunctionBreakpointProperties> breakpoint) {
        final XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
        if (breakpointPosition == null) return;

        final GoFunctionOrMethodDeclaration function = DlvxUtils.findFunction(
                mirrorDebugProcess.getSession().getProject(),
                Objects.requireNonNull(VirtualFileManager.getInstance().findFileByUrl(breakpoint.getFileUrl())),
                breakpointPosition.getLine()
        );

        if (function == null) {
            mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, "go function not found.");
            return;
        }

        if (StringUtils.isBlank(function.getName())) {
            mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, "go function name empty.");
            return;
        }

        breakpoint.getProperties().functionName = function.getQualifiedName();
        final XExpression expression = breakpoint.getConditionExpression();
        final String condition = expression != null ? expression.getExpression() : null;

        mirrorDebugProcess.pauseIfNeededAndProcess(() -> this.sendSetBreakpoint(breakpoint, breakpointPosition.getLine(), condition, null));
    }

    @Override
    public Promise<DlvApi.Breakpoint> sendSetBreakpoint(@NotNull XLineBreakpoint<DlvFunctionBreakpointProperties> breakpoint, int line, @Nullable String condition, @Nullable String path) {
        final Promise<List<DlvApi.Location>> findLocationPromise = mirrorDebugProcess.send(new DlvRequest.FindLocation(breakpoint.getProperties().functionName));
        return findLocationPromise.thenAsync(locations -> {
            if (locations.isEmpty()) {
                mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, DlvxBundle.message("go.debugger.no.location.information.for.function", breakpoint.getProperties().functionName));
                return Promises.resolvedPromise();
            }
            if (locations.size() > 1) {
                mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, DlvxBundle.message("go.debugger.duplicate.location.information.for.function", breakpoint.getProperties().functionName));
                return Promises.rejectedPromise();
            }

            return mirrorDebugProcess.send(new DlvCreateFunctionBreakpoint(locations.get(0).pc));
        }).onSuccess((b) -> {
            mirrorDebugProcess.refreshSources();
            mirrorDebugProcess.breakpoints().put(breakpoint, b.id);
            mirrorDebugProcess.getSession().setBreakpointVerified(breakpoint);
        }).onError((t) -> {
            String message = t == null ? null : t.getMessage();
            if (message != null && message.contains("not found")) {
                message = GoBundle.message("go.debugger.no.debug.information.for.file", breakpoint.getProperties().functionName);
            }
            if (message != null && message.contains("Breakpoint exists")) {
                message = DlvxBundle.message("go.debugger.exists.breakpoint", message);
            }
            if (message != null && message.equals("could not find file " + path)) {
                message = GoBundle.message("go.debugger.no.debug.information.for.file", path);
            }
            mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, message);
        });
    }
}
