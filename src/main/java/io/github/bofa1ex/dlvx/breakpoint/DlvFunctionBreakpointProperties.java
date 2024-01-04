package io.github.bofa1ex.dlvx.breakpoint;

import com.goide.dlv.breakpoint.DlvBreakpointProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DlvFunctionBreakpointProperties extends DlvBreakpointProperties {
    @Attribute("method")
    public String functionName;

    @Override
    public @Nullable DlvFunctionBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DlvBreakpointProperties state) {
        Objects.requireNonNull(state);
        if (state instanceof DlvFunctionBreakpointProperties) {
            final DlvFunctionBreakpointProperties castedState = (DlvFunctionBreakpointProperties) state;
            this.functionName = castedState.functionName;
        }
    }
}
