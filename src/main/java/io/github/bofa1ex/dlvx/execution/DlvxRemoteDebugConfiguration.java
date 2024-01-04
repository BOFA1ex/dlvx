package io.github.bofa1ex.dlvx.execution;

import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.DlvDisconnectOption;
import com.goide.dlv.DlvRemoteVmConnection;
import com.goide.execution.DlvRemoteDebugDisconnectOption;
import com.goide.execution.GoRemoteDebugConfigurationType;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import io.github.bofa1ex.dlvx.DlvMirrorDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class DlvxRemoteDebugConfiguration extends GoRemoteDebugConfigurationType.DlvRemoteDebugConfiguration {
    private final DlvRemoteDebugDisconnectOption myDisconnectOption;

    public DlvxRemoteDebugConfiguration(Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        this.myDisconnectOption = DlvRemoteDebugDisconnectOption.ASK;
    }

    @Override
    public @NotNull XDebugProcess createDebugProcess(@NotNull InetSocketAddress socketAddress, @NotNull XDebugSession session, @Nullable ExecutionResult executionResult, @NotNull ExecutionEnvironment environment) {
        DlvDisconnectOption disconnectOption = DlvDisconnectOption.LEAVE_RUNNING;
        if (myDisconnectOption == DlvRemoteDebugDisconnectOption.STOP) {
            disconnectOption = DlvDisconnectOption.DETACH;
        }

        return new DlvMirrorDebugProcess(session,
                new DlvRemoteVmConnection(disconnectOption),
                executionResult,
                socketAddress
        );
    }
}
