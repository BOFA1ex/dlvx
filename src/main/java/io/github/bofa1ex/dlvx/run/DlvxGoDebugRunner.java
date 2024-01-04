package io.github.bofa1ex.dlvx.run;

import com.goide.execution.GoBuildingRunner;
import com.goide.execution.GoRunningState;
import com.goide.util.GoHistoryProcessListener;
import com.intellij.execution.ExecutionResult;
import com.intellij.xdebugger.XDebugProcessStarter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

@Deprecated
public class DlvxGoDebugRunner extends GoBuildingRunner {

    @Override
    public @NotNull String getRunnerId() {
        return "DlvxGoDebugRunner";
    }

    @Override
    protected @NotNull GoStarter createStarter(@NotNull GoRunningState<?> state, @NotNull GoHistoryProcessListener historyProcessListener, @NotNull Supplier<@Nullable String> outputFilePath, int compilationExitCode) {
        if (state.isDebug()) return new DlvxGoDebugStarter(outputFilePath, historyProcessListener, compilationExitCode);
        return new GoStarter(outputFilePath, historyProcessListener, compilationExitCode);
    }

    public class DlvxGoDebugStarter extends GoDebugStarter {
        public DlvxGoDebugStarter(@NotNull Supplier<@Nullable String> outputFilePath, @NotNull GoHistoryProcessListener historyListener, int compilationExitCode) {
            super(outputFilePath, historyListener, compilationExitCode);
        }

        @Override
        protected @NotNull XDebugProcessStarter createDebugProcessStarter(@Nullable ExecutionResult executionResult, boolean remote, @Nullable InetSocketAddress socket) {
            return super.createDebugProcessStarter(executionResult, remote, socket);
        }
    }
}
