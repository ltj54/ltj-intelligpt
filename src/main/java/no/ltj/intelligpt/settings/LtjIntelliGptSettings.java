package no.ltj.intelligpt.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "LtjIntelliGptSettings",
        storages = @Storage("ltj-intelligpt.xml")
)
public class LtjIntelliGptSettings implements PersistentStateComponent<LtjIntelliGptSettings.State> {

    public static class State {
        public String apiKey = "";
        public String model = "gpt-4.1-mini";
        public boolean sendEditorSelectionOnly = false;
    }

    private State state = new State();

    public static LtjIntelliGptSettings getInstance() {
        return ApplicationManager.getApplication().getService(LtjIntelliGptSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getApiKey() {
        return state.apiKey;
    }

    public void setApiKey(String apiKey) {
        state.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    public String getModel() {
        return state.model;
    }

    public void setModel(String model) {
        state.model = (model == null || model.isBlank())
                ? "gpt-4.1-mini"
                : model.trim();
    }

    public boolean isSendEditorSelectionOnly() {
        return state.sendEditorSelectionOnly;
    }

    public void setSendEditorSelectionOnly(boolean value) {
        state.sendEditorSelectionOnly = value;
    }
}
