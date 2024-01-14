package io.github.bofa1ex.dlvx;

import com.goide.dlv.DlvCommandProcessor;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.DlvVm;
import com.goide.dlv.breakpoint.DlvBreakpointProperties;
import com.goide.dlv.protocol.DlvApi;
import com.goide.dlv.protocol.DlvRequest;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XDropFrameHandler;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.frame.XValueMarkerProvider;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import io.github.bofa1ex.dlvx.execution.DlvFunctionBreakpointHandler;
import io.github.bofa1ex.dlvx.execution.DlvMirrorBreakpointHandler;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;
import org.jetbrains.debugger.ProcessHandlerWrapper;
import org.jetbrains.debugger.connection.VmConnection;
import org.jetbrains.jsonProtocol.Request;

import javax.swing.event.HyperlinkListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"UnstableApiUsage"})
public final class DlvMirrorDebugProcess extends XDebugProcess implements Disposable {
    private final DlvDebugProcess debugProcess;
    private final XBreakpointHandler<?>[] breakpointHandlers;
    private final ExecutionResult executionResult;
    private final VmConnection<?> connection;

    public DlvMirrorDebugProcess(XDebugSession session, VmConnection<?> connection, ExecutionResult executionResult, @NotNull InetSocketAddress socketAddress) {
        super(session);
        this.connection = connection;
        this.executionResult = executionResult;
        this.breakpointHandlers = new XBreakpointHandler<?>[] {new DlvMirrorBreakpointHandler(this), new DlvFunctionBreakpointHandler(this)};
        this.debugProcess = new DlvDebugProcess(session, connection, executionResult, true);
        this.debugProcess.connect(socketAddress);
    }

    @Override
    public void dispose() {
        debugProcess.dispose();
    }

    @Override
    public @NotNull XDebuggerEditorsProvider getEditorsProvider() {
        return debugProcess.getEditorsProvider();
    }

    public void refreshSources() {
        this.send(new DlvRequest.ListSources()).onSuccess(sourceList -> {
            final Method method;
            try {
                method = DlvDebugProcess.class.getDeclaredMethod("setPositionConverter", List.class);
                method.setAccessible(true);
                method.invoke(debugProcess, sourceList);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public XBreakpointHandler<?> @NotNull [] getBreakpointHandlers() {
        final ConnectionStatus status = connection.getState().getStatus();
        return switch (status) {
            case DISCONNECTED, DETACHED, CONNECTION_FAILED -> XBreakpointHandler.EMPTY_ARRAY;
            default -> breakpointHandlers;
        };
    }

    @Override
    public @NotNull ExecutionConsole createConsole() {
        return debugProcess.createConsole();
    }

    @Override
    public void sessionInitialized() {
        debugProcess.sessionInitialized();
    }

    @Override
    public void startPausing() {
        debugProcess.startPausing();
    }

    @Override
    public void startStepOver(@Nullable XSuspendContext context) {
        debugProcess.startStepOver(context);
    }

    @Override
    public void startForceStepInto(@Nullable XSuspendContext context) {
        debugProcess.startForceStepInto(context);
    }

    @Override
    public void startStepInto(@Nullable XSuspendContext context) {
        debugProcess.startStepInto(context);
    }

    @Override
    public void startStepOut(@Nullable XSuspendContext context) {
        debugProcess.startStepOut(context);
    }

    @Override
    public @Nullable XSmartStepIntoHandler<?> getSmartStepIntoHandler() {
        return debugProcess.getSmartStepIntoHandler();
    }

    @Override
    public @Nullable XDropFrameHandler getDropFrameHandler() {
        return debugProcess.getDropFrameHandler();
    }

    @Override
    public void stop() {
        debugProcess.stop();
    }

    @Override
    public @NotNull Promise<Object> stopAsync() {
        return debugProcess.stopAsync();
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        debugProcess.resume(context);
    }

    @Override
    public void runToPosition(@NotNull XSourcePosition position, @Nullable XSuspendContext context) {
        debugProcess.runToPosition(position, context);
    }

    @Override
    public boolean checkCanPerformCommands() {
        return debugProcess.checkCanPerformCommands();
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        return debugProcess.checkCanInitBreakpoints();
    }

    @Override
    protected @NotNull ProcessHandler doGetProcessHandler() {
        ProcessHandler handler = executionResult != null ? executionResult.getProcessHandler() : null;
        return handler == null ? new DefaultDebugProcessHandler() {
            public boolean isSilentlyDestroyOnClose() {
                return true;
            }
        } : new ProcessHandlerWrapper(this, handler);
    }

    @Override
    public @Nullable XValueMarkerProvider<?, ?> createValueMarkerProvider() {
        return debugProcess.createValueMarkerProvider();
    }

    @Override
    public void registerAdditionalActions(@NotNull DefaultActionGroup leftToolbar, @NotNull DefaultActionGroup topToolbar, @NotNull DefaultActionGroup settings) {
        debugProcess.registerAdditionalActions(leftToolbar, topToolbar, settings);
    }

    @Override
    public @Nls String getCurrentStateMessage() {
        return debugProcess.getCurrentStateMessage();
    }

    @Override
    public @Nullable HyperlinkListener getCurrentStateHyperlinkListener() {
        return debugProcess.getCurrentStateHyperlinkListener();
    }

    @Override
    public @NotNull XDebugTabLayouter createTabLayouter() {
        return debugProcess.createTabLayouter();
    }

    @Override
    public boolean isValuesCustomSorted() {
        return debugProcess.isValuesCustomSorted();
    }

    @Override
    public @Nullable XDebuggerEvaluator getEvaluator() {
        return debugProcess.getEvaluator();
    }

    @Override
    public boolean isLibraryFrameFilterSupported() {
        return debugProcess.isLibraryFrameFilterSupported();
    }

    @Override
    public void logStack(@NotNull XSuspendContext suspendContext, @NotNull XDebugSession session) {
        debugProcess.logStack(suspendContext, session);
    }

    @Override
    public boolean dependsOnPlugin(@NotNull IdeaPluginDescriptor descriptor) {
        return debugProcess.dependsOnPlugin(descriptor);
    }

    @SuppressWarnings("unchecked")
    public Map<XBreakpoint<? extends DlvBreakpointProperties>, Integer> breakpoints() {
        try {
            final Field field = DlvDebugProcess.class.getDeclaredField("myBreakpoints");
            field.setAccessible(true);
            return (Map<XBreakpoint<? extends DlvBreakpointProperties>, Integer>) field.get(debugProcess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @Nullable String getDebuggerPath(@NotNull VirtualFile file) {
        try {
            final Method method = DlvDebugProcess.class.getDeclaredMethod("getDebuggerPath", VirtualFile.class);
            method.setAccessible(true);
            return (String) method.invoke(debugProcess, file);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void pauseIfNeededAndProcess(@NotNull Computable<Promise<?>> computable) {
        try {
            final Method method = DlvDebugProcess.class.getDeclaredMethod("pauseIfNeededAndProcess", Computable.class);
            method.setAccessible(true);
            method.invoke(debugProcess, computable);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> @NotNull Promise<T> send(@NotNull Request<T> request) {
        final DlvCommandProcessor processor = getProcessor();
        if (processor == null) {
            request.getBuffer().release();
            return Promises.rejectedPromise();
        }

        return processor.send(request).onError((t) -> {
            String message = t.getMessage();
            if (isConditionEvaluationFailed(message)) {
                consumeStateWithError(message);
            } else if (SystemInfo.isMac && "bad access".equals(message)) {
                this.consumeStateWithError("bad access: nil dereference");
            } else {
                DlvVm.LOG.info(t);
            }
        });
    }

    private @Nullable DlvCommandProcessor getProcessor() {
        try {
            final Method method = DlvDebugProcess.class.getDeclaredMethod("getProcessor");
            method.setAccessible(true);
            return (DlvCommandProcessor) method.invoke(debugProcess);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private @NotNull Function<DlvApi.DebuggerState, Promise<Void>> myStateConsumer() {
        try {
            final Field field = DlvDebugProcess.class.getDeclaredField("myStateConsumer");
            field.setAccessible(true);
            return (Function<DlvApi.DebuggerState, Promise<Void>>) field.get(debugProcess);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isConditionEvaluationFailed(@NonNls @Nullable String s) {
        return s != null && (s.startsWith("error evaluating expression:") || s.startsWith("condition expression unreadable:") || s.startsWith("condition expression not boolean"));
    }

    void consumeStateWithError(@NotNull String message) {
        this.send(new DlvRequest.State()).thenAsync((s) -> {
            s.err = message;
            return consumeDebuggerState(s);
        });
    }

    @NotNull Promise<Void> consumeDebuggerState(@NotNull DlvApi.@NotNull DebuggerState state) {
        Promise<DlvApi.DebuggerState> promise = Promises.resolvedPromise(state);
        Function<DlvApi.DebuggerState, Promise<Void>> stateConsumer = myStateConsumer();
        return promise.thenAsync(stateConsumer::apply);
    }
}
