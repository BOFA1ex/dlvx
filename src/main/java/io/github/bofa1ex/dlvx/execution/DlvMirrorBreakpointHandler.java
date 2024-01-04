package io.github.bofa1ex.dlvx.execution;

import com.goide.dlv.breakpoint.DlvBreakpointProperties;
import com.goide.dlv.breakpoint.DlvBreakpointType;
import com.goide.dlv.protocol.DlvApi;
import com.goide.dlv.protocol.DlvRequest;
import com.goide.i18n.GoBundle;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import io.github.bofa1ex.dlvx.DlvMirrorDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

public class DlvMirrorBreakpointHandler extends DlvBreakpointProxyHandler<XLineBreakpoint<DlvBreakpointProperties>> {
    public DlvMirrorBreakpointHandler(DlvMirrorDebugProcess debugProcess) {
        super(DlvBreakpointType.class, debugProcess);
    }

    @Override
    public Promise<DlvApi.Breakpoint> sendSetBreakpoint(@NotNull XLineBreakpoint<DlvBreakpointProperties> breakpoint, int line, @Nullable String condition, @NotNull String path) {
        return mirrorDebugProcess.send(new DlvRequest.CreateBreakpoint(path, line + 1, condition))
                .onSuccess((b) -> {
                    mirrorDebugProcess.breakpoints().put(breakpoint, b.id);
                    mirrorDebugProcess.getSession().setBreakpointVerified(breakpoint);
                }).onError((t) -> {
                    String message = t == null ? null : t.getMessage();
                    if (message != null && message.equals("could not find file " + path)) {
                        message = GoBundle.message("go.debugger.no.debug.information.for.file", path);
                    }
                    mirrorDebugProcess.getSession().setBreakpointInvalid(breakpoint, message);
                });
    }
}
