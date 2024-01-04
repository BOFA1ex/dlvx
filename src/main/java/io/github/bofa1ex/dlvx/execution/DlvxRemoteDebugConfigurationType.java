package io.github.bofa1ex.dlvx.execution;

import com.goide.GoIcons;
import com.goide.i18n.GoBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

public class DlvxRemoteDebugConfigurationType extends ConfigurationTypeBase {
    public DlvxRemoteDebugConfigurationType() {
        super("GoRemoteDebugConfigurationType",
                "Dlvx " + GoBundle.message("go.execution.remote.run.configuration.display.name"),
                "Dlvx " + GoBundle.message("go.execution.remote.run.configuration.description"),
                NotNullLazyValue.lazy(() -> GoIcons.GO_REMOTE_DEBUG_ICON)
        );

        this.addFactory(new ConfigurationFactory(this) {
            public @NotNull String getId() {
                return "Dlvx Go Remote";
            }

            public @NotNull RunConfigurationSingletonPolicy getSingletonPolicy() {
                return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
            }

            public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new DlvxRemoteDebugConfiguration(project, this, "");
            }

            public boolean isEditableInDumbMode() {
                return true;
            }
        });
    }
}
