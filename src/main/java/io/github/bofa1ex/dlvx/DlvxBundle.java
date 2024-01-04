package io.github.bofa1ex.dlvx;

import com.goide.i18n.GoBundle;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class DlvxBundle {
    private static final DynamicBundle INSTANCE = new DynamicBundle(GoBundle.class, "messages.DlvxBundle");

    public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = "messages.DlvxBundle") String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
