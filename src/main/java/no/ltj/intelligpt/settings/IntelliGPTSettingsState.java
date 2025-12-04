package no.ltj.intelligpt.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

@State(
        name = "LTJIntelliGPTSettings",
        storages = @Storage("LTJIntelliGPTSettings.xml")
)
public class IntelliGPTSettingsState implements PersistentStateComponent<IntelliGPTSettingsState> {

    public String backendUrl = "http://localhost:8080";

    @Override
    public @Nullable IntelliGPTSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@Nullable IntelliGPTSettingsState state) {
        if (state != null) {
            this.backendUrl = state.backendUrl;
        }
    }

    public static IntelliGPTSettingsState getInstance() {
        return com.intellij.openapi.application.ApplicationManager
                .getApplication()
                .getService(IntelliGPTSettingsState.class);
    }
}
